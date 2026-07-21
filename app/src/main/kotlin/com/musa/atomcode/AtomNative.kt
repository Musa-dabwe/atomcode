package com.musa.atomcode

import android.content.Context
import android.webkit.JavascriptInterface

/**
 * Native bridge exposed to the WebView as `window.AtomNative`. The web UI calls into this for
 * device-only capabilities. For now it is a stub; real integrations (package installer, Termux
 * exec, share sheet) land here in a later phase.
 */
class AtomNative(private val context: Context) {

    @JavascriptInterface
    fun installApk(path: String) {
        // TODO: launch the Android package installer (ACTION_VIEW / PackageInstaller) for `path`.
    }
}
