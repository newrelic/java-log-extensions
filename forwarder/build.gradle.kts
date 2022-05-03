plugins {
    java
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
    implementation("com.newrelic.agent.java:newrelic-api:_")
    implementation("com.newrelic.telemetry:telemetry-core:_")
    implementation("com.newrelic.telemetry:telemetry-http-okhttp:_")

    testImplementation(Testing.junit.jupiter)
    testImplementation(Testing.mockito.core)
    testImplementation("ch.qos.logback:logback-classic:_")
    testImplementation(project(":core-test"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}