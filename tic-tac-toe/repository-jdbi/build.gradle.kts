plugins {
    kotlin("jvm")
}

group = "pt.isel.daw"
version = "1.0-SNAPSHOT"

dependencies {
    // Module dependencies
    api(project(":repository"))

    // To get the DI annotation
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    // for JDBI
    implementation("org.jdbi:jdbi3-core:3.55.0")
    implementation("org.jdbi:jdbi3-kotlin:3.55.0")
    implementation("org.jdbi:jdbi3-postgres:3.55.0")
    implementation("org.postgresql:postgresql:42.7.14")

    // For Jackson
    implementation("tools.jackson.core:jackson-databind:3.2.3")
    implementation("tools.jackson.module:jackson-module-kotlin:3.2.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    if (System.getenv("DB_URL") == null) {
        environment("DB_URL", "jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
    }
    dependsOn(":repository-jdbi:dbTestsWait")
    finalizedBy(":repository-jdbi:dbTestsDown")
}

kotlin {
    jvmToolchain(21)
}

/**
 * DB related tasks
 * - To run `psql` inside the container, do
 *      docker exec -ti db-tests psql -d db -U dbuser -W
 *   and provide it with the same password as define on `tests/Dockerfile-db-test`
 */

val composeFileDir: Directory by parent!!.extra
val dockerComposePath = composeFileDir.file("docker-compose.yml").toString()

tasks.register<Exec>("dbTestsUp") {
    description = "Starts the Postgres container (using compose file at '$dockerComposePath')"
    group = "Test DB"
    commandLine("docker", "compose", "-f", dockerComposePath, "up", "-d", "--build", "--force-recreate", "db-tests")
}

tasks.register<Exec>("dbTestsWait") {
    description = "Waits for the Postgres server to be ready for incoming connections, (using compose file at '$dockerComposePath')"
    group = "Test DB"
    commandLine("docker", "exec", "db-tests", "/app/bin/wait-for-postgres.sh", "localhost")
    dependsOn("dbTestsUp")
}

tasks.register<Exec>("dbTestsDown") {
    description = "Removes the Postgres container (using compose file at '$dockerComposePath')"
    group = "Test DB"
    commandLine("docker", "compose", "-f", dockerComposePath, "down", "db-tests")
}
