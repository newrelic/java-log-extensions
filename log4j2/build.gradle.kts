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
    mavenLocal()
    mavenCentral()
    maven(url = "https://dl.bintray.com/mockito/maven/")
}

val includeInJar: Configuration by configurations.creating
configurations["compileOnly"].extendsFrom(includeInJar)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    annotationProcessor("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.2")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("com.newrelic.agent.java:newrelic-api:7.7.0")
    includeInJar(project(":core"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("com.google.guava:guava:31.1-jre")
    testImplementation("org.mockito:mockito-core:4.5.1")
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

tasks.withType<Javadoc> {
    enabled = true
    (options as? CoreJavadocOptions)?.addStringOption("link", "https://logging.apache.org/log4j/2.x/log4j-api/apidocs/")
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
