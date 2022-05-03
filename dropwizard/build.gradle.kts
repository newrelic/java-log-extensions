plugins {
    java
}

group = "com.newrelic.logging"

// -Prelease=true will render a non-snapshot version
// All other values (including unset) will render a snapshot version.
val release: String? by project
val releaseVersion: String by project
version = releaseVersion + if ("true" == release) "" else "-SNAPSHOT"

repositories {
    mavenCentral()
}

val includeInJar: Configuration by configurations.creating
configurations["compileOnly"].extendsFrom(includeInJar)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("io.dropwizard:dropwizard-logging:_")
    implementation("io.dropwizard:dropwizard-request-logging:_")
    implementation("javax.servlet:javax.servlet-api:_")

    implementation("com.newrelic.agent.java:newrelic-api:_")
    includeInJar(project(":logback")) {
        isTransitive = false
    }

    testImplementation(Testing.junit.jupiter)
    testImplementation(Testing.mockito.core)
    testImplementation(Testing.mockito.junitJupiter)
    testImplementation("org.hamcrest:hamcrest:_")
    testImplementation(project(":logback"))
}

val jar by tasks.getting(Jar::class) {
    from(configurations["includeInJar"].flatMap {
        when {
            it.isDirectory -> listOf(it)
            else -> listOf(zipTree(it))
        }
    })
}

tasks.withType<Javadoc> {
    enabled = true
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

apply(from = "$rootDir/gradle/publish.gradle.kts")
