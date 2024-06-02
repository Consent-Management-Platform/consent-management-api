plugins {
    java
    application
    checkstyle
    jacoco
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

    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

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

tasks {
    withType<Test> {
        useJUnitPlatform()

        // Always run jacoco test report after tests
        finalizedBy(jacocoTestReport)
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal.valueOf(0.95)
                }
            }
        }
    }

    check {
        // Fail build if under min test coverage thresholds
        dependsOn(jacocoTestCoverageVerification)
    }
}
