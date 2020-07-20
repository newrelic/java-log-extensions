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
    compile("com.fasterxml.jackson.core:jackson-core:2.9.9")
    compile("ch.qos.logback:logback-core:1.1.1")
    compile("ch.qos.logback:logback-classic:1.1.1")
    compile("com.newrelic.agent.java:newrelic-api:5.6.0")
    includeInJar(project(":core"))

    testCompile("org.junit.jupiter:junit-jupiter:5.5.1")
    testCompile("com.google.guava:guava:28.0-jre")
    testCompile("org.mockito:mockito-core:3.0.7")
    testCompile(project(":core-test"))
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
    (options as? CoreJavadocOptions)?.addStringOption("link", "https://logback.qos.ch/apidocs/")
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