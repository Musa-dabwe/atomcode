plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

// Bundle the :web JS distribution into this module's resources so every consumer
// (:server via Netty, :app via CIO on Android) serves it from the classpath at /web.
// :web is absent when building with -Patom.skipWeb=true (hosts that can't run Node.js).
findProject(":web")?.let { web ->
    val copyWebDist by tasks.registering(Copy::class) {
        dependsOn(web.tasks.named("jsBrowserDistribution"))
        from(web.layout.buildDirectory.dir("dist/js/productionExecutable"))
        into(layout.buildDirectory.dir("resources/main/web"))
    }

    tasks.named("processResources") {
        dependsOn(copyWebDist)
    }
}

dependencies {
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.htmlBuilder)
    implementation(ktorLibs.server.htmx)
    implementation(ktorLibs.server.sse)
    implementation(ktorLibs.server.sessions)
    implementation(ktorLibs.htmx.html)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
