import com.github.spotbugs.SpotBugsTask

plugins {
    java
    id("com.github.spotbugs").version("2.0.0")
}

group = "com.newrelic.logging"
val releaseVersion: String? by project
version = releaseVersion ?: "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/mockito/maven/")
}

val includeInJar: Configuration by configurations.creating
configurations["compileOnly"].extendsFrom(includeInJar)

dependencies {
    implementation("io.dropwizard:dropwizard-logging:1.3.14")
    implementation("com.newrelic.agent.java:newrelic-api:5.6.0")
    includeInJar(project(":logback")) {
        isTransitive = false
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
    testImplementation("org.mockito:mockito-core:3.0.7")
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

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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

tasks.withType(SpotBugsTask::class) {
    reports {
        html.isEnabled = true
        xml.isEnabled = false
    }
}