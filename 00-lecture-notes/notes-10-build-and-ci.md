# Build Automation and Continuous Integration

## Introduction

This lecture note presents a brief introduction to the development environment
and practices used in this course, with emphasis on the second part, namely:

- Build automation and Gradle.

- Style checking, also sometimes called linting.

- Continuous integration and continuous checking.

## Build automation

The process that takes the source code and external dependencies and produces
the project deliverables is called the *build process*. It takes as input:

- The source code and other files authored in the context of the project.

- External dependencies, such as the JDK and external libraries.

- A definition of the build process, sometimes called the *build script*.

The outputs of the build process are the project deliverables, which depend on
the project type:

- For Android applications, the deliverable is typically an APK file (Android
  Package) or an AAB file (Android App Bundle), the latter being required to
  publish new applications on the Play Store.

- For desktop applications, the deliverable can be an installer such as a
  [Windows MSI file](https://en.wikipedia.org/wiki/Windows_Installer) or a
  [macOS DMG file](https://en.wikipedia.org/wiki/Apple_Disk_Image).

- For a server application running on the JVM, the deliverable can be a JAR file
  or a Docker image.

Sometimes, the output of the build process is not only a set of files but also
includes the *publishing* of those files:

- To [Play Store](https://developer.android.com/studio/publish), for Android
  applications.

- To [Maven Central](https://central.sonatype.com), for JAR-based libraries.

- To [Docker Hub](https://hub.docker.com), for Docker images.

The build process can be composed of multiple steps, such as:

- Fetching external dependencies.

- Compiling the source code.

- Running tests and other checks.

- Packaging the compiled source code artifacts (e.g. `.class` files) into one or
  more files (e.g. a `JAR` file).

It is important that this build process is fully automated and also runnable
from different machines, namely machines other than the developer's machine. The
process automation provides:

- A way to perform the build process in a deterministic way without human
  intervention, which is important in continuous integration environments.

- Reduced probability of failure due to human operator error.

- A way to document the build process.

Integrated Development Environments (IDEs), such as IntelliJ, can also use
external build automation tools so that the activities performed by the IDE when
building the project or running the tests are delegated to the build automation
tools. This ensures the build process is always the same, whether it was
triggered from inside the IDE or whether it is being run in a continuous
integration environment.

### Gradle

For JVM-based projects, [Gradle](https://gradle.org/) is a commonly used build
automation tool. It can be called directly from the command line and also
integrates with IntelliJ, which, as described above, can be configured to
delegate all build steps, such as running the tests, to Gradle.

In [Gradle](https://gradle.org/), the build process is defined by:

- The *build script* located in the project's root folder and named
  `build.gradle` or `build.gradle.kts`. As described in the *Multi-module
  builds* section below, a project can also be divided into sub-projects, each
  one with its own build script.

  - `build.gradle` build scripts use the [Groovy](https://groovy-lang.org/)
    programming language.

  - `build.gradle.kts` build scripts use the [Kotlin](https://kotlinlang.org/)
    programming language. In this course we will always use Kotlin based build
    scripts.

- Plugins that are applied to the project by the build script.

The following code block exemplifies a *build script*.

``` kotlin
plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "pt.isel.daw"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-context:7.0.9")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
```

The first part defines the plugins that are used by the build process.

``` kotlin
plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}
```

- The `kotlin` plugin adds the tasks needed to compile and package projects
  using the Kotlin programming language.

- The `org.jlleitschuh.gradle.ktlint` plugin adds tasks to verify the source
  code style, via the [ktlint](https://ktlint.github.io/ktlint/latest/) tool.

An important part of the build script is the definition of the external
dependencies that are used.

``` kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-context:7.0.9")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(kotlin("test"))
}
```

- The `repositories` *block* defines the external repositories that Gradle will
  use to locate and fetch the dependencies. In this case the build script adds
  the [Maven Central](https://central.sonatype.com) repository. This is the most
  commonly used repository to store JVM-based libraries.


- The `dependencies` *block* defines the required external dependencies. For
  instance `implementation("org.springframework:spring-context:7.0.9")` states
  that:

  - There is a dependency identified by the triple:

    - Organization = `org.springframework`

    - Library = `spring-context`

    - Version = `7.0.9`

  - This dependency is an `implementation` dependency, required when compiling
    and running the main code.

Gradle organizes dependencies by *dependency configurations*, of which
`implementation` and `testImplementation` are two examples:

- `implementation` dependencies are used when compiling any *main* or *test*
  source code file and when running the project or tests. This means the
  dependency will be added to both the compiler's and JVM classpath.


- `testImplementation` dependencies are required when compiling any *test*
  source file and when running the tests.

When adding a new external dependency to a project it is always necessary to:

- Add a statement into the `dependencies` *block*, with the proper dependency
  configuration.

- Ensure the repository containing the dependency is included in the
  `repositories` *block*.

**IMPORTANT** It is important to be aware of the difference between:
>
> - The use of an `import` statement in a Kotlin file, such as `import
>   org.springframework.context.annotation.AnnotationConfigApplicationContext`
>
>
> - And the use of the
>   `implementation("org.springframework:spring-context:7.0.9")` statement in a
>   build script.
>
> The `import` statement just introduces an alias in the source file where it is
> located, allowing the
> `org.springframework.context.annotation.AnnotationConfigApplicationContext`
> class to be referenced by its `AnnotationConfigApplicationContext` simple
> name. The `import` statement does *not* result in locating and fetching the
> dependency from an external repository or adding a JAR into the classpath.
>
> The `implementation` statement instructs Gradle to locate, download, and make
> a JAR available in the classpath, namely the JAR file containing the
> `org.springframework.context.annotation.AnnotationConfigApplicationContext`
> class. Without this `implementation` statement compilation would fail because
> the class would not be found in the classpath, independently of the `import`
> statement being present in the source code. It is the `implementation`
> statement in the build script that promotes the inclusion of the JAR
> containing the required class in the build process and not the `import`
> statement in the source code.

### Gradle tasks

In Gradle, the build process is defined by *tasks*. A task has an input, which
is typically one or more files (e.g. Kotlin source files), and an output, which
is also typically one or more files (e.g. the `.class` files resulting from the
compilation process). Tasks can depend on other tasks: if task `t1` depends on
task `t2`, then task `t2` needs to be executed before task `t1` is executed. The
set of tasks forms a directed acyclic graph, where the nodes are the tasks and
the edges are the task dependencies. Gradle uses this graph to determine the
task execution order.

Executing a task can be done on the command line by `./gradlew <task-name>`.
This will run the task with the provided name, as well as all the tasks on which
it depends.

Tasks can be defined directly in a build script or can be added via a plugin.
For instance, the `kotlin` plugin adds tasks to compile and test Kotlin-based
projects, wiring them into the standard *lifecycle* tasks, which are provided by
Gradle's `base` plugin:

- The `compileKotlin` task compiles the *main* Kotlin source code files into
  `.class` files.

- The `compileTestKotlin` task compiles the *test* Kotlin source code files into
  `.class` files.

- The `assemble` lifecycle task creates the project deliverables (e.g. a JAR
  file).

- The `check` lifecycle task runs all the checks, including the tests.

- The `build` lifecycle task runs both `assemble` and `check`, i.e. it runs the
  tests and creates the project deliverables.

The list with all the tasks can be presented by running the `tasks` task -
`./gradlew tasks --all`.

Tasks are also implicitly executed when invoking certain commands from inside
IntelliJ.

Tasks added by plugins can be configured in the build script, by using a block
named after the task. For instance the following block configures how tests
should be executed, namely to use the JUnit Platform.

``` kotlin
tasks.test {
    useJUnitPlatform()
}
```

Plugins can also add *extensions* to the project, i.e. configuration blocks that
are not associated with a single task. An example is the `kotlin` extension,
used in the build script presented above to define the JVM *toolchain*, i.e. the
JDK version used to compile and run the project's code.

``` kotlin
kotlin {
    jvmToolchain(21)
}
```

Applying plugins is the most common way of adding tasks to a project. However,
it is also possible to define tasks explicitly in the build script file. The
following script illustrates such definition.

``` kotlin
tasks.register("demoTask") {
    group = "Demo"
    description = "Just a demo task to illustrate task definition and execution"
    println("demoTask definition")
    doLast {
        println("demoTask execution")
    }
}

tasks.register("anotherDemoTask") {
    group = "Demo"
    description = "Just another demo task to illustrate task definition and execution"
    dependsOn("demoTask")
    println("anotherDemoTask definition")
    doLast {
        println("anotherDemoTask execution")
    }
}
```

The `register` function registers a new task in the `tasks` container, to be
created only if the build needs it. The task *definition* is provided by the
lambda expression passed as the second argument. The `dependsOn` function adds a
dependency between the task being defined and another task. The `doLast`
function adds an action to be executed when the task runs; a task can have
multiple such actions.

Running `./gradlew tasks` will now show a `Demo tasks` group.

``` bash
Demo tasks
----------
anotherDemoTask - Just another demo task to illustrate task definition and execution
demoTask - Just a demo task to illustrate task definition and execution
```

Running `./gradlew demoTask` will run the first task.

``` bash
> Task :demoTask
demoTask execution
```

Running `./gradlew anotherDemoTask` will run both tasks, since `anotherDemoTask`
depends on `demoTask`.

``` bash
> Task :demoTask
demoTask execution

> Task :anotherDemoTask
anotherDemoTask execution
```

#### Task definition versus task execution

Running `./gradlew tasks` will print the following messages present in the outer
lambda, even if those tasks are not being asked to run.

``` bash
demoTask definition
anotherDemoTask definition
```

Running `./gradlew anotherDemoTask` will print all the messages.

``` bash
anotherDemoTask definition
demoTask definition

> Task :demoTask
demoTask execution

> Task :anotherDemoTask
anotherDemoTask execution

BUILD SUCCESSFUL in 531ms
2 actionable tasks: 2 executed
```

This behavior is due to a Gradle build being divided into [three
phases](https://docs.gradle.org/current/userguide/build_lifecycle.html).

- The initialization phase.

- The configuration phase.

- The execution phase.

In the configuration phase, the outer lambdas *defining* the tasks that Gradle
needs to configure are run (e.g. to get the task's group and description),
however the inner lambdas, passed to `doLast`, are not run. In this
configuration phase, Gradle collects the definition of those tasks, namely to
build the task graph. Note that `tasks.register` only runs a task's outer lambda
when that task is effectively needed: the `tasks` task forces the configuration
of all tasks, because it needs their group and description, while running
`anotherDemoTask` only configures the tasks required by that execution. The
inner lambdas, provided via the `doLast` call, are run in the execution phase,
and only for the tasks that effectively need to be executed.

### Multi-module builds

Gradle allows the division of a project into
[*sub-projects*](https://docs.gradle.org/current/userguide/multi_project_builds.html),
also called *modules*. Each sub-project has its own build script (i.e.
`build.gradle` or `build.gradle.kts` file) and produces its own deliverables.
Creation of a sub-project is achieved by:

- Creating a sub-folder with the project name and adding a build script to that
  sub-folder.

- Adding the following to the `settings.gradle.kts` (or `settings.gradle`) file
  located in the root folder.

``` kotlin
include("<sub-project-name>")
```

The argument of `include` is a *project path* and not a file system path: nested
sub-projects are expressed with the `:` separator. For instance,
`include("lib:core")` defines a sub-project located in the `lib/core` folder.
Since project names cannot contain the `/` character, passing a file system
path, such as `include("lib/core")`, results in an error.

Running a task for a specific sub-project is achieved by prefixing the task name
with the sub-project path, i.e. the same path used in the `include` statement:

``` bash
./gradlew :<sub-project-path>:<task-name>
```

Gradle sub-projects are useful to modularize a project, creating structure and
limiting the visibility of the produced code. An example would be a project with
the following sub-projects:

- `lib`, producing a library JAR, i.e. a JAR without a defined entrypoint.

- `cli-app1`, with a command line application, using the library produced by
  `lib`.

- `server1`, with an HTTP server application, using the library produced by
  `lib`.

A dependency on another sub-project is declared in the `dependencies` *block* of
the dependent sub-project's build script, using the `project` function. For
instance, the `build.gradle.kts` of `cli-app1` would contain:

``` kotlin
dependencies {
    implementation(project(":lib"))
}
```

Using sub-projects has the following advantages:

- The `lib` source code has no visibility into the two applications' source
  code, because `lib` does not declare any dependency on them and therefore
  their classes are not in the `lib` compilation classpath.

- The runtime classpath of `cli-app1`, and therefore any distribution created
  from it, only includes code coming from `lib` and from `cli-app1`. Namely, it
  does not include any code from `server1`. Note that the JAR produced by
  `cli-app1` only contains the `cli-app1` classes, with the `lib` classes being
  provided by a separate JAR in the classpath.

- Similarly, the `server1` runtime classpath also doesn't include any code from
  `cli-app1`.

## Syntax style rules and enforcement

In most languages, such as Kotlin and JavaScript, there are multiple syntactical
ways of writing the same expression or statement.

- Identifiers composed by multiple words can be created in different ways (e.g.
  `camelCase`, `PascalCase`, `snake_case`).

- Spaces can be inserted or removed between lexical tokens.

- Lines can end in different places in the middle of an expression or statement.

These differences are not relevant to the compiler and do not change the
compilation result or execution result, however they may have different human
readability properties. This variability results in different *styles* of
writing the same programs.

In the context of the same project, there are advantages in using a common style
among all people involved in it.

- Making sure all the produced code has the readability properties of the
  adopted style.

- When analyzing the difference between commits, avoid the differences that are
  only due to stylistic changes, so that the focus can be placed on the
  significant changes.

  - E.g. programmer A always inserts the `{` starting a block on a new line,
    while programmer B inserts the opening `{` at the end of the previous line.
    If programmer B fixes a problem in a function written by programmer A the
    difference may include multiple line changes that are only due to programmer
    B applying their stylistic preferences and which aren't related to the
    problem being solved.

A *linter* is a tool that can check if the source code follows a defined style,
among other things. Linters can be used in the build process to check that all
the source files follow the agreed stylistic rules and fail the build if they
don't. This is a good way to ensure the code base remains readable and doesn't
slowly drift into a patchwork of different styles.

In this course we use the [ktlint](https://ktlint.github.io/ktlint/latest/)
linter to check the Kotlin source code style, via the
[org.jlleitschuh.gradle.ktlint](https://github.com/jlleitschuh/ktlint-gradle)
Gradle plugin. When this plugin is applied to a project, it makes the `check`
task depend on a task that runs `ktlint`. This way, when running `./gradlew
check`, the style rules will be verified alongside all the other project checks,
such as running the tests.

Some of the style rules verified by ktlint can be configured via an
[`.editorconfig`](https://editorconfig.org) file, typically placed in the
project's root folder. This way the style definition is a part of the project
itself, ensuring all the project members use the same style rules. Another
advantage of using an `.editorconfig` file is the IntelliJ integration: when
running IntelliJ's `Reformat Code` command, the style rules applied will be
obtained from that file.

## Continuous integration and continuous checking

*Continuous Integration* is sometimes
[defined](https://en.wikipedia.org/wiki/Continuous_integration) as

> (...) the practice of integrating source code changes frequently and ensuring
> that the integrated codebase is in a workable state.

An important aspect of continuous integration is the constant checking that the
project is in a valid state, and reporting an error when it isn't. When using
Gradle this translates into running the `check` task and seeing if the result
was successful or not.

A way to perform this constant checking is by having an external system that:

- Detects changes to the source code in a shared repository, such as *pushes*
  into a GitHub repository.

- Triggers the execution of verification tasks when those changes happen.

- Notifies the development team when the verification outcome is not successful.

[GitHub Actions](https://docs.github.com/en/actions) provides a way to implement
this continuous checking, by providing *workflows* that are triggered by changes
in the repository. These workflows are defined in the repository content itself,
by creating YAML files inside the `.github/workflows` folder.

The following file defines an example workflow.

``` yaml
name: spring-context continuous check

on: 
  push:
    paths:
      - code/jvm/spring-context/**

jobs:
  build:
    name: Spring Context Example Continuous Check
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: temurin
      - name: Build with Gradle
        run: ./code/jvm/spring-context/gradlew -p code/jvm/spring-context check --info
```

This file defines a workflow named `Continuous Check` that runs each time there
is a push to the repository, namely to any of its branches or tags. The workflow
execution is composed of a *job* with the identifier `build` and the `Continuous
Check` name, with the following steps:

- Checking out of the pushed commit contents, i.e., the project content.

- Installing a Java 21 distribution (the `temurin` distribution in this case),
  also enabling the reuse of the Gradle caches between workflow executions.

- Running the `check` Gradle task. If this task fails, then the workflow also
  fails.

By default, the user whose push triggered the workflow receives an email when
that workflow fails. The remaining project participants only receive
notifications if they are watching the repository. The workflow results can also
be seen in the GitHub web interface (e.g. the [actions
page](https://github.com/isel-leic-daw/s2627i-51d-51n/actions) of the course's
public repository).
