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
    implementation("io.dropwizard:dropwizard-core:1.3.14")
    implementation(project(":dropwizard"))
    implementation("com.newrelic.agent.java:newrelic-api:7.7.0")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes(mapOf("Main-Class" to "com.newrelic.testapps.dropwizard.Main"))
    }
}

val execTask by tasks.register("start", JavaExec::class) {
    dependsOn("jar")
    classpath = sourceSets.main.get().runtimeClasspath
    jvmArgs = listOf(
            "-javaagent:${rootProject.projectDir}/lib/newrelic.jar"
    )
    main = "com.newrelic.testapps.dropwizard.Main"
    args = listOf(
            "server",
            "$projectDir/test.yml"
    )
}
