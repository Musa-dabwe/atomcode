pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("ktorLibs").from("io.ktor:ktor-version-catalog:3.5.0")
    }
}

rootProject.name = "atomcode"

include(":core")
include(":server")
include(":app")

// The Kotlin/JS front-end needs a Node.js the build host can execute. Hosts that can't run
// one (e.g. an on-device Android IDE, where Gradle downloads a glibc linux-arm64 Node that
// bionic can't exec) must build with -Patom.skipWeb=true (or set it in gradle.properties);
// the APK then ships without /web.js.
if (providers.gradleProperty("atom.skipWeb").orNull?.toBoolean() != true) {
    include(":web")
}
