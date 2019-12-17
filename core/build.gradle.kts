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
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
}