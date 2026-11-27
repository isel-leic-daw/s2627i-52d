# Scaling Server-Sent Events with PostgreSQL Pub/Sub

## Introduction

This lecture note introduces the PostgreSQL publish/subscribe (`LISTEN`/`NOTIFY`)
mechanism and shows how to use it to broadcast Server-Sent Events (SSE) across
multiple backend nodes in the `tic-tac-toe` project.

## The Multi-Node SSE Problem

In a single-node deployment, keeping active `EventEmitter` instances in a list
inside `ChatService` works because every `GET /api/chat/listen` request and
every `POST /api/chat/send` request is handled by the same JVM process.

In a horizontally scaled deployment, such as the one defined in
`tic-tac-toe/host/docker-compose.yml` and `tic-tac-toe/host/tests/nginx/nginx.conf`,
clients connect through an Nginx reverse proxy/load balancer that distributes
requests across multiple backend instances (e.g. `ttt-jvm-1` and `ttt-jvm-2`, or
multiple replicas of `ttt-jvm-dynamic`):

- Client A's `GET /api/chat/listen` connection may be routed to `ttt-jvm-1`, so
  its `SseEmitter` lives only in `ttt-jvm-1`'s memory.
- Client B's `GET /api/chat/listen` connection may be routed to `ttt-jvm-2`, so
  its `SseEmitter` lives only in `ttt-jvm-2`'s memory.
- A subsequent `POST /api/chat/send` request is routed by Nginx to a single node
  (for example, `ttt-jvm-1`).

Without inter-node coordination, only the emitters stored in the JVM that
received the `POST /api/chat/send` request receive the event, and `currentId`
counters diverge across nodes.

To broadcast chat messages to every connected client regardless of which node
accepted the `POST` or holds the SSE connection, nodes need a shared messaging
mechanism. Since all nodes already share a PostgreSQL instance
(`ttt-postgres-tests`), we can use PostgreSQL's built-in pub/sub feature as the
event bus.

## Multi-Node Deployment - Exercises

1. Inspect `tic-tac-toe/host/build.gradle.kts`,
   `tic-tac-toe/host/docker-compose.yml`, `tic-tac-toe/host/tests/nginx/nginx.conf`,
   and `tic-tac-toe/host/README.md`.
   - **Q.1.** Which Gradle tasks build the container images and start/stop the
     multi-node environment?
   - **Q.2.** What is the difference between the Nginx listeners on ports
     `8080`, `8088`, and `8090`?

2. Build all Docker images and start the multi-node environment from the
   `tic-tac-toe` folder:

   ```shell
   ./gradlew :host:buildImageAll
   ./gradlew :host:allUp
   ```

or, depending on your project folders and modules organization:

   ```shell
   ./gradlew -p tic-tac-toe :host:buildImageAll
   ./gradlew -p tic-tac-toe :host:allUp
   ```


3. Verify the running containers with `docker ps` and test load balancing by
   sending a few requests to `/api/status/hostname`:

   ```shell
   curl http://localhost:8080/api/status/hostname
   curl http://localhost:8080/api/status/hostname
   ```

   - **Q.3.** Observe the returned hostnames. How is Nginx distributing
     requests between `ttt-jvm-1` and `ttt-jvm-2`?

4. Open two separate terminals and start an SSE listener in each one:

   ```shell
   curl -N http://localhost:8080/api/chat/listen
   ```

   Then send a chat message in a third terminal:

   ```shell
   curl -X POST -H "Content-Type: text/plain" -d "hello" http://localhost:8080/api/chat/send
   ```

   - **Q.4.** Check the logs of both backend containers (`docker logs ttt-jvm-1`
     and `docker logs ttt-jvm-2`). Which listener(s) received the message, and
     why?

## PostgreSQL Pub/Sub (`LISTEN` and `NOTIFY`)

PostgreSQL includes a lightweight, asynchronous publish/subscribe messaging
mechanism based on the `LISTEN`, `UNLISTEN`, and `NOTIFY` SQL commands:

- **Channels**: Notifications are sent to named channels (SQL identifiers such as
  `chat_messages`). Channels do not need to be declared or created in advance.
- **Subscribing (`LISTEN`)**: A database session subscribes to a channel by
  executing `LISTEN channel_name;`. It can unsubscribe later with
  `UNLISTEN channel_name;` or `UNLISTEN *;`.
- **Publishing (`NOTIFY` / `pg_notify`)**: A session publishes a notification on
  a channel with an optional text payload (up to 8000 bytes in standard
  configurations):

  ```sql
  NOTIFY chat_messages, 'hello';
  -- or, using the SQL function (convenient for bound parameters and triggers):
  SELECT pg_notify('chat_messages', 'hello');
  ```

### Important Semantics

1. **Transaction-aware delivery**: Notifications emitted inside a transaction
   are queued until the transaction executes `COMMIT`. If the transaction executes
   `ROLLBACK`, the notifications are discarded and never sent to listeners.
2. **Ephemeral (fire-and-forget)**: PostgreSQL does not persist `NOTIFY`
   messages in a queue for offline subscribers. Only connections that are
   actively `LISTEN`ing at commit time receive the notification.
3. **Idle between transactions**: A listening client only receives pending
   notifications when its connection is not inside an uncommitted transaction.
4. **Deduplication within a transaction**: If the exact same channel and payload
   are notified multiple times within a single transaction, PostgreSQL may
   collapse them into a single notification.

### Combining Persistence with Triggers

Because `pg_notify` participates in transactions, it pairs naturally with a
table and an `AFTER INSERT` trigger:

- Inserting a row into a table (e.g., `dbo.ChatMessages`) generates a globally
  ordered `id` via an `identity` column (or sequence).
- An `AFTER INSERT` trigger calls `pg_notify` with a serialized payload
  containing the generated `id` and message text (for example, using
  `json_build_object`).
- Listeners only receive the notification once the `INSERT` transaction commits.

## PostgreSQL Pub/Sub in `psql` - Exercises

1. Open two terminals connected to the PostgreSQL container running in Docker:

   ```shell
   docker exec -it ttt-postgres-tests psql -U dbuser -d db
   ```

2. In **Terminal 1**, register as a listener on the `chat_messages` channel:

   ```sql
   LISTEN chat_messages;
   ```

3. In **Terminal 2**, publish a notification:

   ```sql
   SELECT pg_notify('chat_messages', 'hello from psql');
   ```

   Note that `psql` in Terminal 1 prints asynchronous notifications when it
   executes a command (or checks the connection), such as running `SELECT 1;` or
   `\watch`.
   - **Q.1.** Run `SELECT 1;` in Terminal 1. What output does `psql` display?

4. Now test transactional behavior. In **Terminal 2**, run:

   ```sql
   BEGIN;
   SELECT pg_notify('chat_messages', 'inside transaction');
   ```

   Check Terminal 1 with `SELECT 1;`, then run `ROLLBACK;` in Terminal 2 and
   check Terminal 1 again. Repeat with `COMMIT;` instead of `ROLLBACK;`.
   - **Q.2.** When does the listener receive the notification in each case, and
     why is this property important when combining `NOTIFY` with table inserts?

## Implementing Pub/Sub in the Database Container

In `tic-tac-toe`, the PostgreSQL container image `tic-tac-toe-postgres-test` is
defined in `tic-tac-toe/host/tests/Dockerfile-postgres-test` (and `db-tests` in
`tic-tac-toe/repository-jdbi/tests/Dockerfile-db-test`). Both Dockerfiles copy:

- `repository-jdbi/src/sql/create-schema.sql` to
  `/docker-entrypoint-initdb.d/1_create.sql`
- `repository-jdbi/src/sql/insert-test-data.sql` to
  `/docker-entrypoint-initdb.d/2_insert-test-data.sql`

The official Postgres Docker image runs scripts in `/docker-entrypoint-initdb.d/`
when the database cluster is initialized for the first time.

### Database Implementation - Exercises

1. Edit `tic-tac-toe/repository-jdbi/src/sql/create-schema.sql` to add:
   - A `dbo.ChatMessages` table with:
     - `id`: `bigint generated always as identity primary key`
     - `message`: `text not null`
     - `created_at`: `bigint not null`
   - A function `dbo.notify_chat_message()` returning a `trigger` that
     calls `pg_notify` on channel `'chat_messages'` with a JSON payload
     containing `id` and `msg`:

     ```sql
     create or replace function dbo.notify_chat_message()
     returns trigger as $$
     begin
         perform pg_notify(
             'chat_messages',
             json_build_object('id', NEW.id, 'msg', NEW.message)::text
         );
         return NEW;
     end;
     $$ language plpgsql;
     ```

   - An `AFTER INSERT` trigger on `dbo.ChatMessages` that executes
     `dbo.notify_chat_message()` for each inserted row.

2. Rebuild the PostgreSQL test image and recreate the containers:

   ```shell
   ./gradlew :host:buildImagePostgresTest
   ./gradlew :host:allUp
   ```

3. Connect two `psql` sessions to `ttt-postgres-tests`:
   - In session 1, execute `LISTEN chat_messages;`.
   - In session 2, execute:

     ```sql
     insert into dbo.ChatMessages (message, created_at)
     values ('hello trigger', extract(epoch from now())::bigint);
     ```

   - In session 1, execute `SELECT 1;` and verify the JSON notification payload
     produced by the trigger.
   - **Q.1.** Why did we need to run both `buildImagePostgresTest` and `allUp`
     (which uses `--force-recreate`) after editing `create-schema.sql`?

## Integrating PostgreSQL `LISTEN`/`NOTIFY` into `tic-tac-toe`

In `tic-tac-toe`, the backend follows the four-group architecture described in
Lecture Notes 13:

- `domain`: pure domain types (`Event`, `EventEmitter`).
- `repository`: persistence and messaging abstractions (`Transaction`,
  `TransactionManager`, repositories), free of JDBC/JDBI/Postgres-specific types.
- `repository-jdbi`: JDBI and PostgreSQL implementations.
- `services`: domain services such as `ChatService`.
- `http`: Spring MVC controllers such as `ChatController`.

Notice how `ChatController` already delegates to `ChatService`:

- `GET /api/chat/listen` creates an `SseEmitter`, wraps it in
  `SseEmitterBasedEventEmitter`, and passes it to `chatService.addEventEmitter`.
- `POST /api/chat/send` calls `chatService.sendMessage(message)`.

We should keep this exact design between `ChatController` and `ChatService`,
changing only how `ChatService` publishes and receives chat messages:

1. **Publishing**: Instead of incrementing a local `currentId` and immediately
   calling `sendEventToAll` in memory, `ChatService.sendMessage` runs a
   transaction via `TransactionManager` to store the message in
   `dbo.ChatMessages`. When the transaction commits, the database trigger fires
   `pg_notify('chat_messages', ...)`.
2. **Subscribing**: Each backend node maintains **one** dedicated persistent
   connection to PostgreSQL executing `LISTEN chat_messages`. A background
   listener thread polls notifications and forwards each received
   `Event.Message` to `ChatService`, which acquires its lock and pushes the
   event to all local `EventEmitter` instances connected to that node.

### Receiving Notifications with PostgreSQL JDBC and JDBI

The PostgreSQL JDBC driver (`org.postgresql:postgresql`) exposes asynchronous
notifications through `org.postgresql.PGConnection`:

- A JDBI `Handle` (opened via `jdbi.open()`, not inside a short-lived
  `inTransaction` block) provides the underlying JDBC `Connection` via
  `handle.connection`.
- Unwrapping `handle.connection.unwrap(PGConnection::class.java)` gives access
  to `pgConnection.getNotifications(timeoutMillis)`.
- After executing `handle.execute("LISTEN chat_messages")`, calling
  `pgConnection.getNotifications(1000)` waits up to `1000` milliseconds for
  incoming notifications and returns `Array<PGNotification>?` (each element has
  `name` and `parameter`, where `parameter` holds the JSON payload).
- Using a finite timeout inside a `while (running)` loop allows the listener
  thread to exit cleanly when `ShutdownManager` calls `NeedsShutdown.shutdown()`.
- Keep `autoCommit = true` on this dedicated listener connection so PostgreSQL
  delivers notifications immediately instead of holding them behind an open
  transaction.

### Application Integration - Exercises

1. **Extend the `repository` module**:
   - Create a `ChatRepository` interface with a method to store/publish a chat
     message (e.g. `storeMessage(message: String, createdAt: Instant): Long`).
   - Add `val chatRepository: ChatRepository` to `Transaction`.
   - Define a technology-neutral listener interface `ChatMessageSubscriber` in
     `repository` that allows registering a callback `(Event.Message) -> Unit`
     and closing the subscription (`AutoCloseable` or `stop()`), without
     exposing JDBC or `PGConnection` types.

2. **Implement in the `repository-jdbi` module**:
   - Implement `JdbiChatRepository(handle)` and wire it in `JdbiTransaction`.
     `storeMessage` inserts into `dbo.ChatMessages` and returns the generated
     `id` (e.g. using `executeAndReturnGeneratedKeys("id")`).
   - Implement the PostgreSQL `LISTEN` subscriber in the
     `JdbiChatMessageSubscriber` class, annotated with `@Named` and receiving
     `Jdbi` through constructor injection. Its `subscribe` method should perform
     the following steps:
     1. Open a dedicated `Handle` (`jdbi.open()`), execute
       `LISTEN chat_messages`, and unwrap `PGConnection`:
        ```kotlin
        handle.connection.autoCommit = true
        handle.execute("LISTEN $CHANNEL_NAME")
        val pgConnection = handle.connection.unwrap(PGConnection::class.java)
        ```
     2. Start a background thread (e.g., a daemon or managed thread) that calls
       the `listenLoop` function, implemented as follows:
        ```kotlin
        private val running = AtomicBoolean(false) // Controls the listener loop's execution and allows it to be stopped gracefully.
        private val objectMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()
        ...
        private fun listenLoop( handle: Handle, pgConnection: PGConnection, onMessage: (Event.Message)-> Unit) {
            try {
                while (running.get()) {
                    val notifications = pgConnection.getNotifications(POLL_TIMEOUT_MS) ?: continue
                    for (notification in notifications) {
                        if (notification.name == CHANNEL_NAME) {
                          val event = objectMapper.readValue(notification.parameter, Event.Message::class.java)
                          onMessage(event)
                        }
                    }
                }
            } 
            catch (ex: Exception) {...} 
            finally { handle.close() }
        }
        ```
     * On close/shutdown, stop the loop and close the `Handle` as follows:
        ```kotlin
        override fun close() {
            if (running.compareAndSet(true, false)) {
                listenerThread?.join(SHUTDOWN_JOIN_TIMEOUT_MS)
                listenerThread = null
            }
        }
        ```
   - Add an integration test in `repository-jdbi` verifying that inserting a
     message via `JdbiChatRepository` inside a committed transaction triggers
     delivery to the listener, while a rolled-back transaction does not.
   - **Q.1.** Why must the listener hold a dedicated `Handle`/`Connection` open
     instead of using `transactionManager.run { ... }` on every poll iteration?
   - **Q.2.** What would happen if the listener connection left an uncommitted
     transaction open?

3. **Update `ChatService` in the `services` module**:
   - Keep `ChatController` unchanged (`ChatController` continues to use
     `ChatService.addEventEmitter` and `ChatService.sendMessage`).
   - Inject `TransactionManager` and the `ChatMessageSubscriber` into `ChatService`
     (alongside `Clock`).
   - Register a callback on the `ChatMessageSubscriber` so that whenever an `Event.Message`
     arrives from PostgreSQL, `ChatService` acquires `lock` and calls
     `sendEventToAll(event)` for the emitters registered on **this** node.
   - Update `sendMessage(msg: String)` to store the message through
     `transactionManager.run { it.chatRepository.storeMessage(msg, clock.now()) }`
     instead of directly calling `sendEventToAll` with a local `currentId`.
   - Update `shutdown()` (remember `ChatService` implements `NeedsShutdown`) to
     stop/close the listener in addition to notifying local emitters and shutting
     down `scheduler`.
   - **Q.3.** In `sendMessage`, should `lock` be held while calling
     `transactionManager.run { ... }`? Explain what happens if `ChatService`
     holds `lock` during the database call vs. only when mutating/iterating
     `emitters`.
   - **Q.4.** Why is `currentId` no longer needed in `ChatService`?

4. **Verify tests locally**:
   - Run the full test suite and linter from `tic-tac-toe`:

     ```shell
     ./gradlew check
     ```

   - Ensure `JdbiChatRepository` tests, service tests, HTTP tests, and `ktlint`
     checks all pass.

## Testing Broadcast Across Multiple Nodes - Exercises

1. Rebuild all Docker images and start the multi-node cluster:

   ```shell
   ./gradlew :host:buildImageAll
   ./gradlew :host:allUp
   ```

2. Open two (or more) terminals running `curl -N http://localhost:8080/api/chat/listen`.
   Check `docker logs ttt-jvm-1` and `docker logs ttt-jvm-2` to confirm that one
   SSE client is registered on `ttt-jvm-1` and another is registered on
   `ttt-jvm-2` (for example, by observing the `adding emitter` and `keepAlive`
   log lines).

3. In another terminal, send several messages:

   ```shell
   curl -X POST -H "Content-Type: text/plain" -d "message 1" http://localhost:8080/api/chat/send
   curl -X POST -H "Content-Type: text/plain" -d "message 2" http://localhost:8080/api/chat/send
   ```

   - **Q.1.** Do all SSE clients receive every message regardless of which node
     handled the `POST`? Compare the `id` and `data` fields received by each
     client.
   - **Q.2.** Inspect `tic-tac-toe/requests.http` and test the chat endpoints
     through port `8090` (`http://localhost:8090/api/chat/listen` and
     `http://localhost:8090/api/chat/send`), as well as the static frontend on
     `http://localhost:8090/`.

4. Test dynamic scaling on port `8088`:
   - From `tic-tac-toe/host`, scale `ttt-jvm-dynamic` to 3 instances:

     ```shell
     docker compose up -d --scale ttt-jvm-dynamic=3
     ```

   - Connect multiple SSE listeners to `http://localhost:8088/api/chat/listen`
     and send messages to `http://localhost:8088/api/chat/send`.
   - **Q.3.** In `tic-tac-toe/host/tests/nginx/nginx.conf`, notice that port
     `8090` configures `proxy_read_timeout 1h;`, whereas ports `8080` and `8088`
     do not. Why do SSE connections on `8080` and `8088` stay open past
     Nginx's default 60-second read timeout?
   - **Q.4.** What happens to an SSE client's `Last-Event-ID` if `ttt-jvm-1` is
     stopped while the client is listening through Nginx, and the browser's
     `EventSource` automatically reconnects to `ttt-jvm-2`? Given that
     `dbo.ChatMessages` persists messages, how could `ChatController` and
     `ChatService` be extended to replay missed messages on reconnection?

5. When finished, stop all containers:

   ```shell
   ./gradlew :host:allDown
   ```
