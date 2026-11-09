# Server-Sent Events

## Introduction

This lecture note contains a brief introduction to the Server-Sent Events
specification and browser support.

## The Server-Sent Events specification

In the HTTP protocol, request messages are sent by clients.  
Response messages are sent back by servers, but always as a response to a client
request message.  
However, there are use cases where server systems may want to send data to a
client by their own initiative, and not as a response to a client request.  
The [Server-Sent Events
specification](https://html.spec.whatwg.org/multipage/server-sent-events.html)
defines a way for a server to use the payload of a single response as the
communication medium for *pushing* a sequence of *events* into the client.  

It all starts with a client creating a connection to the server or using an
existing one.  
Using this connection, the client then sends a request message using the `GET`
method.  
The server then initiates the sending of a response message through the same
connection.  
The server does that by sending a status line with the `200` status code and a
`Content-Type` header with the SSE `text/event-stream` type.  
This media type signals to the client that the server will use the response
payload to push events according to the SSE specification.  

The `text/event-stream` is a text-based line-oriented format, where an event is
transmitted as one or more sequential text lines:

- Empty lines serve as event delimiters. An event is only dispatched when the
  empty line that ends it is received.
- Lines *starting* with a `:` are considered comments and are ignored.
- Other lines define a *field*. The part before the first `:` is the *field
  name* and the part after it is the *field value*, with a single leading space
  removed if present (i.e., `data: x` and `data:x` both have the value `x`). A
  line without any `:` defines a field with that name and an empty value.

Example:

```text
HTTP/2 200
content-type: text/event-stream

event: ping
id: 1
data: a ping event

event: ping
id: 2
data: another ping event
data: with multiple lines in the data

```

The following field names are defined by the SSE specification:

- `event` - the field value defines the *event type*.
- `data` - the field value defines the *event data*. Multiple fields with this
  name accumulate for the *event data*.
- `id` - the field value defines the *event ID*. The browser remembers the last
  received ID and sends it in the `Last-Event-ID` request header when
  reconnecting, allowing the server to resume the event sequence.
- `retry` - if the field value represents an integer in base 10, then it is used
  as the [reconnection
  time](https://html.spec.whatwg.org/multipage/server-sent-events.html#concept-event-stream-reconnection-time),
  in milliseconds.
- Fields with other names are ignored.

Example:

```text
: this is a comment

data: event with a single-line data

data: event with
data: multiple lines

event: some-type
id: 123
data: event of type 'some-type' and ID '123'

```

Run the following shell command to see an example of an SSE response.

```shell
curl -i https://httpbun.com/sse
```

## The Server-Sent Events - Exercises

1. Copy `index.js` from the `lesson19-sse` directory in your classroom repository
   to the local `notes-14-server` directory in your individual labs repository.

2. Run the application with `node index.js` and check the following:
   1. In a browser, visit http://localhost:4000/.
   2. In a browser, visit http://localhost:4000/sse/listen.
   3. In a terminal, run `curl -N http://localhost:4000/sse/listen`.

3. **Q.1.** According to the Server-Sent Events specification, which parts of the
   `/sse/listen` implementation make it a valid SSE emitter?

4. Change the implementation of the `emitSse` function so that it emits up to
   a specified limit. Check that the client stops receiving events when the
   limit is reached. Do not forget to close the connection.
   * **Q.2.** What happens if the server does not close the connection after it
     has finished sending the events?

5. Change the `/sse/listen` route in such a way that it emits events starting
   after the event ID received in the `Last-Event-ID` header, if it exists.
   * Test it with the following curl command, replacing `1003` with any other valid event ID:

     ```shell
     curl -N -H "Last-Event-ID: 1003" http://localhost:4000/sse/listen
     ```

## Browser support

The [SSE specification](https://html.spec.whatwg.org/multipage/server-sent-events.html) also defines the browser API (Application Programming Interface) to use this media type.

The [`EventSource`](https://developer.mozilla.org/en-US/docs/Web/API/EventSource) constructor function performs a GET request to the target URI passed as argument, returning an object that represents that *SSE connection*.

```typescript
const eventSource = new EventSource("https://httpbun.com/sse");
```

The returned object (`eventSource` in the previous example) exposes the following functionality:

- A `readyState` property returning the state of the SSE connection as a number, with possible values being `CONNECTING` (0), `OPEN` (1), or `CLOSED` (2).
- A `close` method to close the SSE connection.
- The `addEventListener` and `removeEventListener` methods, allowing the registration and deregistration of event handlers for the following events:
  - `open` - the SSE connection was successfully established, meaning that the server responded with a 200 status code and a `text/event-stream` `Content-Type` header.
  - `error` - the SSE connection was not successfully established or it ended. If the connection ended, the browser automatically tries to reconnect after the reconnection time, unless `close` was called or the failure was fatal (e.g. a non-200 status code or a wrong `Content-Type`), in which case `readyState` becomes `CLOSED`.
  - `message` - an event without an `event` field was received.
  - *event-type* - an event with an `event` field equal to *event-type* was received.

The handlers for received events are called with a [`MessageEvent`](https://developer.mozilla.org/en-US/docs/Web/API/MessageEvent) object, containing the event data in the `data` property and the event ID in the `lastEventId` property.

## Browser support — Exercise

1. Copy the `lesson19-EventSource` directory in your classroom repository
   to the local `notes-14-client` directory in your individual labs repository.

2. Run the web application and observe its behaviour. It initially connects
   autonomously to `https://httpbun.com/sse`.

3. Observe the connection and received events in the browser developer tools
   and in the React page.

4. **Q.1.** Analyse `src/main.tsx` and identify the parts described in the Browser
   support section of this lecture note: the `EventSource` constructor,
   `readyState`, `open`, `error`, and message handlers, and `close`.

5. **Q.2.** Change the client to receive events from your local
   `http://localhost:4000/sse/listen` endpoint. The local emitter uses the
   custom event type `my-custom-event`, so register a handler for that exact
   type.
   
6. Run the local client and server and check the behaviour of the React app.

7. **Q.3.** Stop the local server and then restart it. What behaviour do you observe
   in the React app? What happens to the `error`, `open`, and message events?

8. **Q.4.** Remove the `Content-Type: "text/event-stream"` header from the local
   endpoint response. What changes in the `EventSource` connection?

9. **Q.5.** Remove the `Access-Control-Allow-Origin: "*"` header from the local
   endpoint response. What changes when the client is loaded from the Vite
   development server?

