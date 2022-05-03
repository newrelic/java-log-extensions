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
    implementation(project(":logback"))
    implementation("ch.qos.logback:logback-core:_")
    implementation("ch.qos.logback:logback-classic:_")
    implementation("com.fasterxml.jackson.core:jackson-core:_")

    implementation("com.newrelic.agent.java:newrelic-api:_")
}

application {
    mainClassName = "com.newrelic.testapps.logback.Main"
    applicationDefaultJvmArgs += listOf("-javaagent:${rootProject.projectDir}/lib/newrelic.jar")
}
