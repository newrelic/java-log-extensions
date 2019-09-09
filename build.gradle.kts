group = "com.newrelic.logging"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    tasks.withType(Test::class).all {
        useJUnitPlatform()
        reports.html.isEnabled = true
    }

    tasks.withType(Javadoc::class).all {
        enabled = false
    }

    tasks.withType(JavaCompile::class) {
        options.compilerArgs.add("-Xlint:unchecked")
        options.isDeprecation = true
    }
}
