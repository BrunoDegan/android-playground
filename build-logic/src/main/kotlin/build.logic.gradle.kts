import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.devtools.ksp")
    id("io.insert-koin.compiler.plugin")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.skydoves.compose.stability.analyzer")
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(FileInputStream(localPropertiesFile))
        }
    }

android {
    testFixtures.enable = true
}

kotlin {
    ktlint {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        outputColorName.set("RED")
        baseline.set(file("config/ktlint/baseline.xml"))
    }
}

extensions.configure<ApplicationExtension> {
    compileSdk = 37

    lint {
        abortOnError = true
        ignoreWarnings = true
        checkDependencies = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        minSdk = 30
        targetSdk = 37
        applicationId = "com.brunodegan.androidplayground"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "TMDB_BEARER_TOKEN",
            "\"${localProperties.getProperty("TMDB_BEARER_TOKEN", "")}\"",
        )
        buildConfigField(
            "String",
            "TMDB_ACCOUNT_ID",
            "\"${localProperties.getProperty("TMDB_ACCOUNT_ID", "")}\"",
        )
    }

    buildTypes {
        debug {
            optimization {
                enable = false
            }
        }
        release {
            isDebuggable = false
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_19)
    }
}

composeStabilityAnalyzer {
    traceAll {
        enabled.set(true)
        threshold.set(2)
    }
    stabilityValidation {
        failOnStabilityChange.set(true)
        includeTests.set(false)
        ignoreNonRegressiveChanges.set(false)
    }
}
