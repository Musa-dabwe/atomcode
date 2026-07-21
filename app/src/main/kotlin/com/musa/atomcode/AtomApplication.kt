package com.musa.atomcode

import android.app.Application
import android.util.Log
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
            // An uncaught Throwable on any thread kills the whole Android process, so a server
            // that fails to boot must not take the UI down with it.
            try {
                embeddedServer(CIO, port = PORT, host = "127.0.0.1") {
                    module()
                }.start(wait = true)
            } catch (t: Throwable) {
                Log.e(TAG, "Embedded server failed", t)
            }
        }
    }

    companion object {
        const val PORT = 8080
        private const val TAG = "AtomCode"
    }
}
