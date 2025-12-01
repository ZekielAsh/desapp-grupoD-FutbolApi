import org.gradle.testing.jacoco.plugins.JacocoPluginExtension

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "6.3.1.5724"
    id("jacoco")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.12"
}

val jsonWebTokenVersion = "0.11.5"
val swaggerVersion = "2.8.5"
val h2Version = "2.3.232"
val seleniumVersion = "4.15.0"
val webDriverManagerVersion = "5.6.2"
val mockitoKotlinVersion = "5.1.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation ("io.jsonwebtoken:jjwt-api:${jsonWebTokenVersion}")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${swaggerVersion}")
    implementation("org.seleniumhq.selenium:selenium-java:$seleniumVersion")
    implementation("io.github.bonigarcia:webdrivermanager:$webDriverManagerVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    // Actuator for health checks and metrics
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // Micrometer Prometheus registry
    implementation("io.micrometer:micrometer-registry-prometheus")
    // Spring Cache for performance optimization
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonWebTokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonWebTokenVersion")
    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("com.tngtech.archunit:archunit-junit5:1.2.1")
    testImplementation("com.tngtech.archunit:archunit-junit5-api:1.2.1")
    testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine:1.2.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("io.mockk:mockk:1.13.5")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sonar {
  properties {
    property("sonar.projectKey", "ZekielAsh_desapp-grupoD-FutbolApi")
    property("sonar.organization", "zekielash")
  }
}

sourceSets {
    test {
        java.srcDirs("src/test/kotlin")
        resources.srcDirs("src/test/resources")
    }
}

tasks.test {
    useJUnitPlatform()
    // evita el error de zona horaria en cualquier SO
    systemProperty("user.timezone", "UTC")
    // Enable dynamic agent loading for Mockito (suppress warnings in Java 21+)
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    finalizedBy(tasks.jacocoTestReport)
    environment("FOOTBALL_DATA_API_KEY", System.getenv("FOOTBALL_DATA_API_KEY") ?: "5764b841c2f946a3916de2c73742c198")
}

tasks.processTestResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}