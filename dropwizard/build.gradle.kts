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
configurations["compileOnly"].extendsFrom(includeInJar)

dependencies {
    implementation("io.dropwizard:dropwizard-logging:1.3.14")
    implementation("io.dropwizard:dropwizard-request-logging:1.3.14")
    implementation("javax.servlet:javax.servlet-api:3.1.0")

    implementation("com.newrelic.agent.java:newrelic-api:7.4.3")
    includeInJar(project(":logback")) {
        isTransitive = false
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.mockito:mockito-core:3.4.4")
    testImplementation("org.mockito:mockito-junit-jupiter:3.4.4")
    testImplementation("org.hamcrest:hamcrest:2.2")
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

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports.create("html") {
        isEnabled = true
    }
}