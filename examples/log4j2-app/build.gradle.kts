plugins {
    java
    application
}

group = "com.newrelic.logging"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    runtimeOnly(project(":log4j2"))

    implementation("com.fasterxml.jackson.core:jackson-core:2.11.1")
    implementation("com.lmax:disruptor:3.4.2")
    implementation("com.newrelic.agent.java:newrelic-api:7.4.3")
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "com.newrelic.testapps.log4j2.Main"
    applicationDefaultJvmArgs += listOf(
            "-javaagent:${rootProject.projectDir}/lib/newrelic.jar",
            "-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"
    )
}
