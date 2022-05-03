plugins {
    java
    application
}

group = "com.newrelic.logging"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":log4j1"))

    implementation("log4j:log4j:1.2.17")
    implementation("com.fasterxml.jackson.core:jackson-core:2.11.1")
    implementation("com.newrelic.agent.java:newrelic-api:7.7.0")
}

application {
    mainClassName = "com.newrelic.testapps.log4j1.Main"
    applicationDefaultJvmArgs += listOf(
            "-javaagent:${rootProject.projectDir}/lib/newrelic.jar"
    )
}
