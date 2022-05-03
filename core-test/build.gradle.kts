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
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.2")
    implementation(project(":core"))
}
