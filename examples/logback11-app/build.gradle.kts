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
    implementation(project(":logback11"))
    implementation("ch.qos.logback:logback-core:1.1.1")
    implementation("ch.qos.logback:logback-classic:1.1.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.11.1")

    implementation("com.newrelic.agent.java:newrelic-api:7.7.0")
}

application {
    mainClassName = "com.newrelic.testapps.logback11.Main"
    applicationDefaultJvmArgs += listOf("-javaagent:${rootProject.projectDir}/lib/newrelic.jar")
}
