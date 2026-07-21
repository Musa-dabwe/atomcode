plugins {
    // AGP 9+ ships built-in Kotlin support, so the kotlin-android plugin is not applied here.
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.musa.atomcode"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.musa.atomcode"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Ktor pulls in many jars that collide on duplicate META-INF entries; drop the noise.
    packaging {
        resources.excludes += setOf(
            "META-INF/INDEX.LIST",
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE*",
            "META-INF/NOTICE*",
            "META-INF/*.kotlin_module",
            "META-INF/versions/**",
            "META-INF/{AL2.0,LGPL2.1}",
        )
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation(ktorLibs.server.cio)
    implementation(libs.logback.classic)
}
