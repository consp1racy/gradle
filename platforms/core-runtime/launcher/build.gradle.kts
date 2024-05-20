plugins {
    id("gradlebuild.distribution.api-java")
    id("gradlebuild.launchable-jar")
}

description = "Implementation for launching, controlling and communicating with Gradle Daemon from CLI and TAPI"

errorprone {
    disabledChecks.addAll(
        "DefaultCharset", // 5 occurrences
        "FutureReturnValueIgnored", // 2 occurrences
        "InlineFormatString", // 1 occurrences
        "LockNotBeforeTry", // 7 occurrences
        "MissingCasesInEnumSwitch", // 1 occurrences
        "NarrowCalculation", // 1 occurrences
        "StringCaseLocaleUsage", // 1 occurrences
        "StringSplitter", // 1 occurrences
        "URLEqualsHashCode", // 3 occurrences
        "UndefinedEquals", // 1 occurrences
        "UnusedVariable", // 3 occurrences
    )
}

dependencies {
    api(project(":base-services"))
    api(project(":build-events"))
    api(project(":build-operations"))
    api(project(":build-option"))
    api(project(":build-state"))
    api(project(":cli"))
    api(project(":client-services")) {
        because("This project contains the client, daemon and tooling API provider. It should be split up.  For now, add dependencies on both the client and daemon pieces")
    }
    api(project(":concurrent"))
    api(project(":core-api"))
    api(project(":core"))
    api(project(":daemon-protocol"))
    api(project(":daemon-services")) {
        because("This project contains the client, daemon and tooling API provider. It should be split up.  For now, add dependencies on both the client and daemon pieces")
    }
    api(project(":enterprise-logging"))
    api(project(":execution"))
    api(project(":files"))
    api(project(":file-collections"))
    api(project(":file-watching"))
    api(project(":hashing"))
    api(project(":java-language-extensions"))
    api(project(":jvm-services"))
    api(project(":logging-api"))
    api(project(":logging"))
    api(project(":messaging"))
    api(project(":model-core"))
    api(project(":native"))
    api(project(":persistent-cache"))
    api(project(":process-services"))
    api(project(":serialization"))
    api(project(":service-provider"))
    api(project(":snapshots"))
    api(project(":time"))
    api(project(":toolchains-jvm-shared"))
    api(project(":tooling-api"))

    api(libs.guava)
    api(libs.jsr305)

    implementation(project(":build-configuration"))
    implementation(project(":enterprise-operations"))
    implementation(project(":functional"))
    implementation(projects.io)
    implementation(project(":problems-api"))

    implementation(libs.groovy) // for 'ReleaseInfo.getVersion()'
    implementation(libs.slf4jApi)
    implementation(libs.commonsIo)
    implementation(libs.commonsLang)
    implementation(libs.ant)

    runtimeOnly(libs.asm)
    runtimeOnly(libs.commonsIo)
    runtimeOnly(libs.commonsLang)
    runtimeOnly(libs.slf4jApi)

    manifestClasspath(project(":bootstrap"))
    manifestClasspath(projects.javaLanguageExtensions)
    manifestClasspath(project(":base-services"))
    manifestClasspath(project(":worker-services"))
    manifestClasspath(project(":core-api"))
    manifestClasspath(project(":core"))
    manifestClasspath(project(":persistent-cache"))

    agentsClasspath(project(":instrumentation-agent"))

    testImplementation(project(":internal-integ-testing"))
    testImplementation(project(":native"))
    testImplementation(project(":cli"))
    testImplementation(project(":process-services"))
    testImplementation(project(":core-api"))
    testImplementation(project(":model-core"))
    testImplementation(project(":resources"))
    testImplementation(project(":snapshots"))
    testImplementation(project(":base-services-groovy")) // for 'Specs'

    testImplementation(testFixtures(projects.serialization))
    testImplementation(testFixtures(project(":core")))
    testImplementation(testFixtures(project(":language-java")))
    testImplementation(testFixtures(project(":messaging")))
    testImplementation(testFixtures(project(":logging")))
    testImplementation(testFixtures(project(":tooling-api")))

    integTestImplementation(project(":persistent-cache"))
    integTestImplementation(libs.slf4jApi)
    integTestImplementation(libs.guava)
    integTestImplementation(libs.commonsLang)
    integTestImplementation(libs.commonsIo)
    integTestImplementation(testFixtures(project(":build-configuration")))

    testRuntimeOnly(project(":distributions-core")) {
        because("Tests instantiate DefaultClassLoaderRegistry which requires a 'gradle-plugins.properties' through DefaultPluginModuleRegistry")
    }
    integTestDistributionRuntimeOnly(project(":distributions-full")) {
        because("built-in options are required to be present at runtime for 'TaskOptionsSpec'")
    }
}

strictCompile {
    ignoreRawTypes() // raw types used in public API
}

testFilesCleanup.reportOnly = true
