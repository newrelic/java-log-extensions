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
    implementation(project(":logback"))
    implementation("ch.qos.logback:logback-core:1.4.11")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.fasterxml.jackson.core:jackson-core:2.11.1")

    implementation("com.newrelic.agent.java:newrelic-api:8.6.0")
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClassName = "com.newrelic.testapps.logback.Main"
    applicationDefaultJvmArgs += listOf("-javaagent:${rootProject.projectDir}/lib/newrelic.jar")
}
