import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import libs.Kotlin.kotlin
import libs.Postgresql.postgresql
import libs.Spring.spring
import libs.Spring.springTest

val javaTarget = JavaVersion.VERSION_11

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0" apply false
    kotlin("plugin.jpa") version "1.8.0" apply false
    id("org.springframework.boot") version "2.7.4" apply false
    id("io.spring.dependency-management") version "1.0.14.RELEASE" apply false
}

allprojects {
    group = "com.backend"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = javaTarget.toString()
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("kotlin-spring")
        plugin("kotlin-jpa")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
    }

    java {
        sourceCompatibility = javaTarget
        targetCompatibility = javaTarget
    }

    dependencies {
        kotlin()
        spring()
        springTest()
        postgresql()
    }

    tasks.getByName("bootJar") {
        enabled = false
    }

    tasks.getByName("jar") {
        enabled = true
    }
}