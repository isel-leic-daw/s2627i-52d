plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "pt.isel.daw"
version = "1.0-SNAPSHOT"

dependencies {

    // Module dependencies
    implementation(project(":domain"))
    implementation(project(":services"))

    // To use Spring MVC and the Servlet API
    implementation("org.springframework:spring-webmvc:7.0.9")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // To use SLF4J
    implementation("org.slf4j:slf4j-api:2.0.20")
}

tasks.test {
    useJUnitPlatform()
    if (System.getenv("DB_URL") == null) {
        environment("DB_URL", "jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
    }
}

kotlin {
    jvmToolchain(21)
}
