plugins {
    java
}

group = "com.newrelic.logging"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Prevent dependencies in this subproject from getting fat-jar-ed into projects using includeInJar
val compileAndTestOnly: Configuration by configurations.creating
configurations["compileOnly"].extendsFrom(compileAndTestOnly)
configurations["testImplementation"].extendsFrom(compileAndTestOnly)

dependencies {
    compileAndTestOnly("com.newrelic.agent.java:newrelic-api:7.6.0")
    compileAndTestOnly("com.newrelic.telemetry:telemetry-core:0.13.2")
    compileAndTestOnly("com.newrelic.telemetry:telemetry-http-okhttp:0.13.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.mockito:mockito-core:3.4.4")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation(project(":core-test"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
