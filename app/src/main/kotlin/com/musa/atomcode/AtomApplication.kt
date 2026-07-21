package com.musa.atomcode

import android.app.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlin.concurrent.thread

/**
 * Boots the embedded Atom Code server (the same Ktor [module] the standalone dev server runs)
 * on a background thread so the in-app WebView can load it from localhost.
 */
class AtomApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        thread(isDaemon = true, name = "atom-ktor") {
            embeddedServer(CIO, port = PORT, host = "127.0.0.1") {
                module()
            }.start(wait = true)
        }
    }

    companion object {
        const val PORT = 8080
    }
}
