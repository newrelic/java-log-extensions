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
    implementation("io.dropwizard:dropwizard-core:_")
    implementation(project(":dropwizard"))
    implementation("com.newrelic.agent.java:newrelic-api:_")
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
