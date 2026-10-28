= Backend Code Organization

== Introduction

This document introduces an organization for the backend component of the
Web-based systems developed in this course.

The organization principles presented in this document are not the only valid
way of organizing the type of system developed in this course, and it is not our
goal to present the "only true way" of designing and organizing software.

Regardless of the design principles used, it is important to be explicit about
the organization used on a project, as a way to help project development,
maintainability, and evolution:

* Identify the responsibilities of each type and function in the code base, via
the _group_ where it is contained. * Characterize the dependencies that can be
created in the code base, i.e., which types and functions each type or function
is allowed to use. * Help pinpoint the types and functions that need to be
changed or added, when a defect needs to be corrected, a new feature needs to be
added, or a domain rule needs to be changed.

== System organization

The overall system is composed of two main components:

* The _frontend_ component, running in the end users' devices, mainly
responsible for the user interface. * The _backend_ component, running outside
the end users' devices (e.g. in a data center), mainly responsible for storing
the relevant domain data and providing the domain functionality.
    ** E.g. Storing the state for each ongoing or completed game. ** E.g.
    Providing operations to apply turn plays to ongoing games.

The frontend component communicates with the backend component using the HTTP
protocol, namely by using an HTTP API (Application Programming Interface),
exposed by the backend. Initially, this communication follows a request-response
pattern, where requests are always produced by the frontend and responses are
always produced by the backend.

One of the used design principles is making the backend responsible for almost
(if not all) domain-related decisions in the system. The main goals of this
design are:

* Ensuring the correctness of the system, namely that the domain rules are
always applied, depends on the correctness of the backend component and not of
the frontend components. As an example, it should be impossible for a tampered
frontend instance to transition a game into an invalid state or execute an
invalid operation. * Improving the evolution of the system, where domain rules
need to be changed, by minimizing the amount of changes that need to be done on
the frontend components.

It is important to note that frontend components:

* Run in devices controlled by end users and therefore may execute malicious
actions. * Have update and versioning procedures which can take more time to
complete, such as the need to submit a new version to an application store, wait
for its approval, and wait for the end users to install the new versions. * May
be implemented by different teams using different technologies:
    ** E.g. browser-based application using TypeScript and React. ** E.g.
    Android-based native application using Kotlin and Jetpack Compose.

== Backend organization

The backend component is composed of two sub-components:

* A Relational Data Base Management System (RDBMS), holding a relational
database with the system's persisted information. * An _application_ running in
the Java Virtual Machine, exposing the HTTP API for consumption by the frontend
and performing interactions with the RDBMS.

The backend component may also include other sub-components, such as
reverse-proxies/gateways.

=== Backend application organization

The backend application is a JVM-based application, written using the Kotlin
Programming Language. It is composed of a set of classes and functions,
organized into *four main groups*:

* Repository group - contains functions and types to interact with the RDBMS and
its database. * Services group - contains functions and types providing domain
operations, such as creating a user, creating a game, or making a turn play on a
game. * Domain group - contains functions and types representing domain
concepts. * HTTP group - contains functions and types responsible for exposing
and implementing the HTTP API.

=== The domain group

The domain group is composed of types and functions that represent domain
concepts, such as a user, a stored token, or a game. One of its distinct
properties is that the domain group _does not_ depend on types and functions of
any of the other groups.

The types and functions of the domain group define a _domain language_ used by
all other groups.

=== The repository group

The repository group defines types and functions to interact with the persisted
data, namely the data managed by the RDBMS. It is composed of:

* Interfaces that define the signature of the interactions with the persisted
data. * Concrete classes that implement these interfaces, for one or more ways
of persisting, changing, and querying data.

The repository group classes and functions may use items from the domain group,
however they should not use anything from the other groups (services and HTTP).

The repository group is designed to support multiple implementations, such as:

* One implementation using JDBI to interact with a PostgreSQL RDBMS. * One
implementation using in-memory storage, for testing purposes.

According to our experience, there is always a dependency between the database
persistence technology and the shape of the repository functions. The types of
interactions possible with a relational DBMS such as PostgreSQL, with DynamoDB,
or with MongoDB, are rather different. Defining a repository interface that only
uses functionalities common to all these systems means not taking advantage of
their distinctive features, resulting in a less than optimal usage.

Due to this, it is our current opinion that the design of the repository
interface should take into consideration the types of interactions allowed by
the database technology used in production. An example of this is having the
concept of transactions with an unbounded number and type of inner interactions.

On the other hand, it is also our current opinion that the repository group
should not expose types specific to the database technology, such as exposing a
JDBC `ResultSet`. That is, expose interaction patterns that depend on the
database technology but not specific types of those technologies.

In addition, the repository group should not make hidden domain level decisions.
That is, preferably all decisions should be explicitly communicated via function
arguments.

==== An illustrative example

Consider a domain rule stating that the number of active authentication tokens
associated with a user is limited:

* A user cannot have more than a pre-defined number of tokens. * The creation of
a new token may require removing an older token so that the maximum number is
never exceeded.

In the organization principles described in this document:

* It is allowed for the repository group to expose functions with this behavior,
i.e., functions that create a token and remove an old token if needed. This
allows for the creation and deletion to be done in a single interaction with the
database management system. * However, this behavior should be explicit in the
function documentation and signature and not be a private implementation detail
of the repository function. * The maximum number of tokens should not be
hardcoded in the repository group and always be defined by the external caller,
which is always a function from the services group.

=== The services group

The services group defines types and functions that provide domain
functionality, such as creating a user, creating a game, or making a turn play
on a game.

The services group uses items from the repository group and from the domain
group, however it does not use items from the HTTP group.

The distinction between the services group and the domain group is a subtle one:

* Functions from the services group can interact with the repository group and
therefore may have side-effects. The evaluation of a service function may result
in a state change in the persisted state.

* Functions from the domain group cannot interact with the repository group and
should be free from side-effects, i.e. they should be _pure functions_.

This division will help with testability, because functions from the domain
group are easier to test, since they are pure and do not require a database or
mocks.

=== The HTTP group

The HTTP group contains functions and types responsible for exposing and
implementing the HTTP API. It may use items from the domain and services groups,
however it does not use items from the repository group.

Types in this group can be dependent on the technology used to handle HTTP
requests, which is Spring MVC for this course. As an example, types in this
group can implement interfaces, extend classes, and use types from the Spring
MVC library.

Spring MVC allows the usage of complex types to represent input and output
information, namely:

* Map the payload of request messages into handler arguments with complex types
(e.g. data classes with multiple fields).

* Map values of complex types returned by handlers into the payload of response
messages.

The shape of these types influences the shape of the request and response
messages and therefore influence the HTTP API contract. Due to this, we use
special _input and output model_ types in these places, instead of using types
from the domain group. The main goal is to isolate the HTTP API from changes
outside the HTTP group.

== Testing

=== Testing the repository group

The test of the repository group uses the implementations used in production. In
the provided example, this means using the JDBI-based implementation that
interacts with the PostgreSQL RDBMS. No attempt is made to simulate or mock any
part of the repository group or of the RDBMS. That is, testing the repository
group requires a functional PostgreSQL.

The automated creation and disposal of functional PostgreSQL instances is
achieved by using Docker containers and Gradle tasks.

=== Testing the services group

The test of the services group is done by explicitly instantiating and using
types contained in this group. No dependency injection container is used.
Instead, explicit composition of the dependencies graph is used.

The test of the services group typically requires implementations of the
repository group interfaces. Two different routes can be taken for this:

* Use the production repository implementation, i.e. the one that requires a
functional PostgreSQL database, taking advantage of the automation put in place
for testing the repository group.

* Use an in-memory repository implementation.

The former may be slower to execute, due to the RDBMS interactions, however
better reflects what will happen in production. The latter can be faster to
execute, however requires the implementation or _mocking_ of the repository
group, and may have a behavior that doesn't exactly match what happens in
production.

=== Testing the domain group

The test of the domain group is the easiest to achieve, since it is mainly
composed of pure functions. It can be achieved without the need to use the
dependency injection container or a functional RDBMS.

=== Testing the HTTP group

The test of the HTTP group is achieved by starting the complete backend,
including the JVM-based application and the RDBMS, performing HTTP requests and
asserting properties of the HTTP responses. For that it uses:

* The PostgreSQL automation put in place for the repository group tests. * The
Spring test helpers that automatically launch the Spring application when a test
is run. * The Spring provided `WebTestClient` class, containing a fluent
interface to construct HTTP requests and asserting properties on HTTP responses.
Note that `WebTestClient` is part of Spring WebFlux, so using it to test a
Spring MVC application requires the `spring-webflux` dependency on the test
classpath. Alternatively, Spring Framework 7 provides `RestTestClient`, a
similar fluent client designed for Spring MVC applications.

== Exercises

=== Setting up the environment

* Checkout the latest commit in the `main` branch of the `s2627i-51d-51n`
repository. * Open one or more shells in the `code/jvm/tic-tac-toe` folder. *
Open the `tic-tac-toe` Gradle-based project in Intellij.
    ** Ensure project loading is successful.
* Ensure link:https://www.docker.com/products/docker-desktop/[Docker Desktop] is
installed.
    ** Check that Docker Desktop is running correctly by doing `docker ps`
        *** This command shows the running containers.
* Run `./gradlew dbTestsWait` to start a PostgreSQL instance in a container.
    ** Run `docker ps` to check that now there is a container running.
* Run `./gradlew check` to run all tests.
    ** Ensure the build is successful. ** Open the file located at
    `build/reports/tests/all/index.html` and visualize the tests that were
    executed and their outcome.
* Run `docker ps`
    ** Running the `check` task terminates the container at the end.

=== Adding tests in the _domain_ group

* Add a test in the _domain_ group.
    ** A `TokenEncoder` implementation is responsible for taking a token and
    producing validation information that can be stored in the database.
    Computing the token from this information should be computationally
    infeasible, however given a token it should be easy to check if the token
    matches the validation information. ** The test should empirically
        *** Verify that the validation information is not equal to the original
        token. *** Verify that the encoding process is deterministic.
    ** Run the test from inside IntelliJ and from the shell.

=== Extend the _repository_ functionality

* Add a new function to the `UserRepository`, which returns the number of tokens
for a given user that have been used in a given time interval and created in a
given interval.
    ** Implement that function in the `JdbiUserRepository`. ** Adds at least one
    test in `JdbiUserRepositoryTests` to test this new repository functionality.
        *** Ensure the test passes successfully, both when running from inside
        IntelliJ and from inside the shell.

=== Extend the _services_ functionality

* Add a new function to the `UsersService` interface to obtain the number of
valid tokens for a given user.
    ** Note the properties in the `UsersDomainConfig` class, belonging to the
    _domain_ group. ** Add at least one test in the `UsersServiceTests` to
    verify this new service functionality.
        *** Ensure the test passes successfully, both when running from inside
        IntelliJ and from inside the shell.

=== Extend the _http_ functionality

* Add to the HTTP API a way to obtain the number of valid tokens for the
authenticated user.
    ** Add at least one test in the `UserTests` to verify this new HTTP API
    functionality.
        *** Observe the use of the `@SpringBootTest` annotation in the
        `UserTests` class and research what is its purpose. *** Observe the use
        of the `WebTestClient` class in the tests present in `UserTests`. ***
        Ensure the test passes successfully, both when running from inside
        IntelliJ and from inside the shell.

* Make sure all the tests pass successfully when run from the shell via
`./gradlew check`.