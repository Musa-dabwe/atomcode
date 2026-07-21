package com.musa.atomcode

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/** Hosts the Atom Code IDE served by the embedded Ktor server inside a full-screen WebView. */
class MainActivity : Activity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this).apply {
            setBackgroundColor(Color.parseColor("#0e0e0e"))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(AtomNative(this@MainActivity), "AtomNative")
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError,
                ) {
                    // The server may still be starting on first launch — retry the main frame.
                    if (request.isForMainFrame) {
                        Handler(Looper.getMainLooper()).postDelayed({ view.loadUrl(URL) }, 400)
                    }
                }
            }
        }
        setContentView(webView)
        // Give the background server a moment to bind before the first load.
        Handler(Looper.getMainLooper()).postDelayed({ webView.loadUrl(URL) }, 300)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    private companion object {
        const val URL = "http://127.0.0.1:8080/"
    }
}
