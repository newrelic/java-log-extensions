plugins {
    java
    id("com.github.spotbugs").version("4.4.4")
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
    implementation("com.fasterxml.jackson.core:jackson-core:2.9.9")
    implementation("com.newrelic.agent.java:newrelic-api:5.6.0")
    includeInJar(project(":core"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
    testImplementation("com.google.guava:guava:28.0-jre")
    testImplementation("org.mockito:mockito-core:3.0.7")
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
    excludeFilter.set(file("spotbugs-filter.xml"))
    reports.create("html") {
        isEnabled = true
    }
}