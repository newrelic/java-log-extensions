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
    implementation("org.apache.logging.log4j:log4j-api:_")
    implementation("org.apache.logging.log4j:log4j-core:_")
    runtimeOnly(project(":log4j2"))

    implementation("com.fasterxml.jackson.core:jackson-core:_")
    implementation("com.lmax:disruptor:_")
    implementation("com.newrelic.agent.java:newrelic-api:_")
}

application {
    mainClassName = "com.newrelic.testapps.log4j2.Main"
    applicationDefaultJvmArgs += listOf(
            "-javaagent:${rootProject.projectDir}/lib/newrelic.jar",
            "-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"
    )
}
