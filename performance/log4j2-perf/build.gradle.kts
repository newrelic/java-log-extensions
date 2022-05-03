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
    implementation(project(":log4j2"))

    implementation("org.apache.logging.log4j:log4j-api:_")
    implementation("com.fasterxml.jackson.core:jackson-databind:_")
    implementation("com.lmax:disruptor:_")
    implementation("com.newrelic.agent.java:newrelic-api:_")
}

application {
    mainClassName = "PerformanceMain"
    applicationDefaultJvmArgs += listOf(
            "-javaagent:${rootProject.projectDir}/lib/newrelic.jar",
            "-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"
    )
}

task("executeNoAgent", JavaExec::class) {
    main = "PerformanceMain"
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("INFO", "No_Agent")
    jvmArgs("-Xmx1024m")
}

task("executeWithAgent", JavaExec::class) {
    main = "PerformanceMain"
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("INFO", "With_Agent")
    jvmArgs("-Xmx1024m", "-javaagent:${rootProject.projectDir}/lib/newrelic.jar")
}

task("executeNoAgentAsync", JavaExec::class) {
    main = "PerformanceMain"
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("INFO", "No_Agent_Async")
    jvmArgs("-Xmx1024m")
    systemProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
}

task("executeWithAgentAsync", JavaExec::class) {
    main = "PerformanceMain"
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("INFO", "With_Agent_Async")
    jvmArgs("-Xmx1024m", "-javaagent:${rootProject.projectDir}/lib/newrelic.jar")
    systemProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
}

task("runPerformanceTests") {
    dependsOn("jar")
    dependsOn("executeNoAgent")
    dependsOn("executeWithAgent")
    dependsOn("executeNoAgentAsync")
    dependsOn("executeWithAgentAsync")
}
