plugins {
    java
}

group = "com.newrelic.logging"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.dropwizard:dropwizard-core:3.0.17")
    implementation(project(":dropwizard3"))
    implementation("com.newrelic.agent.java:newrelic-api:9.1.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes(mapOf("Main-Class" to "com.newrelic.testapps.dropwizard.Main"))
    }
}

val execTask by tasks.register("start", JavaExec::class) {
    dependsOn("jar")
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.newrelic.testapps.dropwizard.Main")
    jvmArgs = listOf(
            "-javaagent:${rootProject.projectDir}/lib/newrelic.jar"
    )
    args = listOf(
            "server",
            "$projectDir/test.yml"
    )
}
