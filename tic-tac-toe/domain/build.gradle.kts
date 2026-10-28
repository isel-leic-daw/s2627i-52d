plugins {
    kotlin("jvm")
}

group = "pt.isel.daw"
version = "1.0-SNAPSHOT"

dependencies {
    // To get the DI annotation
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    // To get password encode
    api("org.springframework.security:spring-security-core:7.1.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
