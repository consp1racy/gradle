plugins {
    id("gradlebuild.platform")
}

description = "Defines which JARs go into the core part (libs/*.jar) of a Gradle distribution (NOT libs/plugins/*.jar)."

javaPlatform.allowDependencies()

dependencies {
    runtime(project(":installation-beacon"))
    runtime(project(":api-metadata"))
    runtime(project(":launcher")) {
        because("This is the entry point of Gradle core which transitively depends on all other core projects.")
    }
    runtime(project(":daemon-main")) {
        because("This is the entry point of the Gradle daemon.")
    }
    runtime(project(":tooling-api-provider")) {
        because("This is the entry point of the tooling API.")
    }
    runtime(project(":kotlin-dsl")) {
        because("Adds support for Kotlin DSL scripts.")
    }
    runtime(project(":declarative-dsl-provider")) {
        because("Adds support for interpreting files with the declarative DSL")
    }
}
