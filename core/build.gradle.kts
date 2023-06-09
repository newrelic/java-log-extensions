plugins {
    java
}

repositories {
    mavenCentral()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    // Allows for easy testing of values based on Environment Variables and System Properties
    testImplementation("com.github.stefanbirkner:system-lambda:1.2.1")
}
