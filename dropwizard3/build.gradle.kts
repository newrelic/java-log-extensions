plugins {
    java
    id("com.github.spotbugs").version("4.4.4")
}

group = "com.newrelic.logging"

// -Prelease=true will render a non-snapshot version
// All other values (including unset) will render a snapshot version.
val release: String? by project
val releaseVersion: String by project
version = releaseVersion + if ("true" == release) "" else "-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/mockito/maven/")
}

val includeInJar: Configuration by configurations.creating
includeInJar.exclude(group = "org.apache.commons")
configurations["compileOnly"].extendsFrom(includeInJar)

dependencies {
    implementation("io.dropwizard:dropwizard-logging:3.0.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")
    implementation("io.dropwizard:dropwizard-request-logging:3.0.0")
    implementation("javax.servlet:javax.servlet-api:3.1.0")

    implementation("com.newrelic.agent.java:newrelic-api:9.1.0")
    includeInJar(project(":logback")) {
        isTransitive = false
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.mockito:mockito-core:3.4.4")
    testImplementation("org.mockito:mockito-junit-jupiter:3.4.4")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation(project(":logback"))
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
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
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

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports.create("html") {
        isEnabled = true
    }
}