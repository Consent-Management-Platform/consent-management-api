plugins {
    java
    application
    checkstyle
    jacoco
}

repositories {
    mavenCentral()
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/Consent-Management-Platform/checkstyle-config")
            credentials {
                username = project.findProperty("gpr.usr") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/Consent-Management-Platform/consent-management-api-models")
            credentials {
                username = project.findProperty("gpr.usr") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

checkstyle {
    toolVersion = "10.16.0"
    setIgnoreFailures(false)
    configFile = project.layout.projectDirectory.file("config/checkstyle/checkstyle.xml").asFile
    configProperties = mapOf(
        "checkstyle.config.dir" to project.layout.projectDirectory.file("config/checkstyle").asFile,
        "checkstyle.suppressions.file" to project.layout.projectDirectory.file("config/checkstyle/suppressions.xml").asFile
    )
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
    implementation("com.consentframework.consentmanagement:consentmanagement-api-models:0.2.10")

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
    withType<Checkstyle> {
        dependsOn("downloadCheckstyleConfig")
    }

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

    clean {
        // Clean up downloaded Checkstyle config files
        delete("$rootDir/config/checkstyle")
    }
}

// Task to download the Checkstyle config files
tasks.register("downloadCheckstyleConfig", Copy::class.java) {
    val checkstyleConfigDependency = configurations.detachedConfiguration(
        dependencies.create("com.consentframework.consentmanagement:checkstyle-config:0.0.3")
    )
    from(zipTree(checkstyleConfigDependency.singleFile))
    into(project.layout.projectDirectory.file("config/checkstyle"))
    include("checkstyle.xml")
    include("suppressions.xml")
}

// Build jar which will later be consumed to run the API service
tasks.register<Zip>("packageJar") {
    into("lib") {
        from(tasks.jar)
        from(configurations.runtimeClasspath)
    }
}
