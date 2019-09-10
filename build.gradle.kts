group = "com.newrelic.logging"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    tasks.withType<Test>().all {
        useJUnitPlatform()
        reports.junitXml.isEnabled = true
    }

    tasks.withType<Javadoc>().all {
        enabled = false
    }

    tasks.withType(JavaCompile::class) {
        options.compilerArgs.add("-Xlint:unchecked")
        options.isDeprecation = true
    }
}
