pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.3.21"
        id("org.jetbrains.kotlin.plugin.spring") version "2.3.21"
        id("org.springframework.boot") version "4.1.1"
        id("io.spring.dependency-management") version "1.1.7"
        id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    }
}

rootProject.name = "daw2026-52d"

include("lesson13-spring-mcv-intro")
include("lesson14-java-servlet")
include("lesson14-pipeline")
include("lesson15-movies")
include("lesson15-spring-context")
