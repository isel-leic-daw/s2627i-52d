plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.spring") version "2.3.21" apply false
    id("org.springframework.boot") version "4.1.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0" apply false
}

rootProject.name = "tic-tac-toe"

// The host module - application configuration and entry-point
include("host")

// The domain module
include("domain")

// The HTTP module, with the HTTP interface
include("http")

// The services module
include("services")

// The repository module, with the repository interfaces
include("repository")

// The repository-jdbi module, with the JDBC/JDBI based repository implementation
include("repository-jdbi")
