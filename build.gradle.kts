plugins {
    java
    application
    checkstyle
}

repositories {
    mavenCentral()
}

checkstyle {
    toolVersion = "10.16.0"
    setIgnoreFailures(false)
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.guava)
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("com.consentframework.consentmanagement.api.ConsentManagementApiRequestHandler")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
