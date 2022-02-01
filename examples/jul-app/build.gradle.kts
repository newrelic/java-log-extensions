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
    implementation(project(":jul"))
    implementation("com.newrelic.agent.java:newrelic-api:7.4.3")
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "com.newrelic.testapps.jul.Main"
    applicationDefaultJvmArgs += listOf(
            "-javaagent:${rootProject.projectDir}/lib/newrelic.jar",
            "-Djava.util.logging.config.file=src/main/resources/logging.properties"
    )
}

tasks.register("makeLogsDir") {
    doLast {
        mkdir("logs")
    }
}

val run by tasks.getting(JavaExec::class) {
    dependsOn("makeLogsDir")
}