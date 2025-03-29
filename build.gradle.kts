plugins {
    application
    jacoco
    java

    id("com.consentframework.consentmanagement.checkstyle-config") version "1.1.0"
}

repositories {
    mavenCentral()
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/Consent-Management-Platform/consent-management-api-models")
            credentials {
                username = project.findProperty("gpr.usr") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    gradlePluginPortal()
}

dependencies {
    implementation(libs.guava)
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

    // Logging
    val log4j2Version = "2.23.1"
    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")

    // DynamoDB client
    val dynamoDbClientVersion = "2.26.7"
    implementation("software.amazon.awssdk:dynamodb:$dynamoDbClientVersion")
    implementation("software.amazon.awssdk:dynamodb-enhanced:$dynamoDbClientVersion")

    // Smithy
    implementation("software.amazon.smithy:smithy-utils:1.49.0")

    // Consent service models
    implementation("com.consentframework.consentmanagement:consentmanagement-api-models:0.3.0")

    // Immutables
    val immutablesDependency = "org.immutables:value:2.10.1"
    compileOnly(immutablesDependency)
    annotationProcessor(immutablesDependency)
    testCompileOnly(immutablesDependency)
    testAnnotationProcessor(immutablesDependency)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
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

    build {
        dependsOn("packageJar")
    }

    check {
        // Fail build if under min test coverage thresholds
        dependsOn(jacocoTestCoverageVerification)
    }
}

// Build jar which will later be consumed to run the API service
tasks.register<Zip>("packageJar") {
    into("lib") {
        from(tasks.jar)
        from(configurations.runtimeClasspath)
    }
}

tasks.clean {
  delete("$rootDir/bin")
  delete("$rootDir/build")
}
