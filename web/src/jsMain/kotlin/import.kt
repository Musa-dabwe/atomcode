package com.musa.atomcode

import kotlin.js.*

// Bundles htmx and exposes it on window.htmx so hx-* attributes are processed.
@JsModule("htmx.org")
external object htmx
