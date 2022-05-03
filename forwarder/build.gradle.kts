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
    implementation("com.newrelic.agent.java:newrelic-api:7.7.0")
    implementation("com.newrelic.telemetry:telemetry-core:0.13.1")
    implementation("com.newrelic.telemetry:telemetry-http-okhttp:0.13.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.mockito:mockito-core:4.5.1")
    testImplementation("ch.qos.logback:logback-classic:1.2.11")
    testImplementation(project(":core-test"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}