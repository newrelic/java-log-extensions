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
    implementation(Testing.junit.jupiter)
    implementation("com.fasterxml.jackson.core:jackson-core:_")
    implementation(project(":core"))
}
