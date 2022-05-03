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
    implementation("com.fasterxml.jackson.core:jackson-core:_")
    implementation("com.newrelic.agent.java:newrelic-api:_")
    includeInJar(project(":core"))

    testImplementation(Testing.junit.jupiter)
    testImplementation("com.google.guava:guava:_")
    testImplementation(Testing.mockito.core)
    testImplementation(project(":core"))
    testImplementation(project(":core-test"))
}

val jar by tasks.getting(Jar::class) {
    from(configurations["includeInJar"].flatMap {
        when {
            it.isDirectory -> listOf(it)
            else -> listOf(zipTree(it))
        }
    })
}

// this is done because JUL can't be cleanly reset between tests run on the same JVM.
tasks.withType<Test> {
    setForkEvery(1)
}

tasks.withType<Javadoc> {
    enabled = true
    (options as? CoreJavadocOptions)?.addStringOption("link", "https://docs.oracle.com/javase/8/docs/api/")
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
