// Aggregates the test results of all modules into a single HTML report.
// The results are referenced by directory, and not by test task, so that generating the report
// doesn't force the tests of every module (some of them requiring the DB) to run.
val testReport =
    tasks.register<TestReport>("testReport") {
        description = "Generates an HTML report with the test results of all modules"
        group = "verification"
        destinationDirectory = layout.buildDirectory.dir("reports/tests/all")
        testResults.from(subprojects.map { it.layout.buildDirectory.dir("test-results/test/binary") })
        mustRunAfter(subprojects.map { "${it.path}:test" })
    }

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.8.0")
    }
    repositories {
        mavenCentral()
    }
    // Running 'check' also produces the aggregated test report
    tasks.matching { it.name == "check" }.configureEach {
        finalizedBy(testReport)
    }
}
tasks.register<Exec>("composeDown") {
    description = "Stops and removes the containers created by compose up"
    commandLine("docker", "compose", "down")
}

extra["composeFileDir"] = layout.projectDirectory
