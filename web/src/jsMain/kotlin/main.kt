package com.musa.atomcode

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event

/**
 * Atom Code interaction layer. Presentational chrome (drawer, view switch, sheets, popover,
 * accordion, textarea, streaming) is handled here; meaningful actions go through htmx.
 */
fun main() {
    // Force htmx into the bundle / onto window so hx-* attributes are processed.
    console.log("Atom Code UI", htmx)

    document.addEventListener("click", { e -> onClick(e) })
    document.addEventListener("input", { e -> onInput(e) })
    document.addEventListener("DOMContentLoaded", { initSse() })
    document.body?.addEventListener("htmx:afterSwap", { initSse(); scrollChatToBottom() })

    // In case the bundle runs after DOMContentLoaded already fired.
    initSse()
    initKeyboardAvoidance()
}

private inline fun forEachEl(selector: String, action: (Element) -> Unit) {
    val nodes = document.querySelectorAll(selector)
    for (i in 0 until nodes.length) {
        (nodes.item(i) as? Element)?.let(action)
    }
}

// ---------------------------------------------------------------------------
//  Click delegation
// ---------------------------------------------------------------------------

private fun onClick(e: Event) {
    val target = e.target as? Element ?: return
    val actionEl = target.closest("[data-action]")

    // Close the model popover on any outside click.
    val menu = document.getElementById("model-menu")
    if (menu != null && menu.classList.contains("open")) {
        val insideMenu = target.closest("#model-menu") != null
        val isToggle = actionEl?.getAttribute("data-action") == "toggle-model-menu"
        if (!insideMenu && !isToggle) menu.classList.remove("open")
    }

    when (actionEl?.getAttribute("data-action")) {
        "open-drawer" -> open("drawer-layer")
        "close-drawer" -> close("drawer-layer")
        "open-settings" -> { close("drawer-layer"); open("settings-layer") }
        "close-settings" -> close("settings-layer")
        "close-overlay" -> clearOverlay()
        "stop" -> {} // clicks inside a sheet body: swallow so the scrim doesn't close it
        "switch-view" -> switchView(actionEl.getAttribute("data-view"))
        "toggle-accordion" -> actionEl.closest(".accordion")?.classList?.toggle("open")
        "toggle-model-menu" -> menu?.classList?.toggle("open")
        "pick-model" -> menu?.classList?.remove("open")
        "select-tab" -> selectTab(actionEl.getAttribute("data-tab"))
        "inc-threads" -> stepThreads(1)
        "dec-threads" -> stepThreads(-1)
        "set-ctx" -> setCtx(actionEl)
        "install" -> install(actionEl)
    }
}

private fun open(id: String) { document.getElementById(id)?.classList?.add("open") }
private fun close(id: String) { document.getElementById(id)?.classList?.remove("open") }
private fun clearOverlay() { document.getElementById("overlay-root")?.let { it.innerHTML = "" } }

private fun switchView(view: String?) {
    if (view == null) return
    forEachEl(".viewtab") { it.classList.toggle("active", it.getAttribute("data-view") == view) }
    forEachEl(".view") { it.classList.toggle("active", it.getAttribute("data-view") == view) }
}

private fun selectTab(tab: String?) {
    if (tab == null) return
    forEachEl(".sheet-tab") { it.classList.toggle("active", it.getAttribute("data-tab") == tab) }
    forEachEl(".settings-pane") { it.classList.toggle("active", it.getAttribute("data-pane") == tab) }
}

private fun stepThreads(delta: Int) {
    val valueEl = document.getElementById("threads-value") ?: return
    val fillEl = document.getElementById("threads-fill") as? HTMLElement ?: return
    val current = valueEl.textContent?.trim()?.toIntOrNull() ?: 4
    val next = (current + delta).coerceIn(1, 8)
    valueEl.textContent = next.toString()
    fillEl.style.width = "${next * 100 / 8}%"
}

private fun setCtx(el: Element) {
    forEachEl(".ctx-opt") { it.classList.remove("active") }
    el.classList.add("active")
}

private fun install(el: Element) {
    val actions = document.getElementById("apk-actions") ?: return
    actions.innerHTML =
        "<div class=\"btn-installing\"><span class=\"spinner\"></span>Installing…</div>" +
        "<button class=\"btn-secondary\" type=\"button\">Open dir</button>"
    window.asDynamic().setTimeout({
        actions.innerHTML =
            "<div class=\"btn-installed\">✓ Installed</div>" +
            "<button class=\"btn-secondary\" type=\"button\">Open dir</button>"
    }, 1600)

    // On Android, hand the path to the native installer bridge if present.
    val apk = el.getAttribute("data-apk") ?: return
    val bridge = window.asDynamic().AtomNative
    if (bridge != null && bridge.installApk != undefined) bridge.installApk(apk)
}

// ---------------------------------------------------------------------------
//  Input (textarea autosize)
// ---------------------------------------------------------------------------

private fun onInput(e: Event) {
    val t = e.target
    if (t is HTMLTextAreaElement && t.classList.contains("prompt-input")) {
        t.style.height = "auto"
        val h = if (t.scrollHeight > 110) 110 else t.scrollHeight
        t.style.height = "${h}px"
    }
}

// ---------------------------------------------------------------------------
//  Server-Sent Events (agent reply + terminal logs)
// ---------------------------------------------------------------------------

private fun initSse() {
    forEachEl("[data-sse]:not([data-sse-live])") { el ->
        el.setAttribute("data-sse-live", "1")
        val url = el.getAttribute("data-sse") ?: return@forEachEl
        val eventName = el.getAttribute("data-sse-event") ?: "message"

        val source = js("new EventSource(url)")
        source.addEventListener(eventName, { ev: dynamic ->
            el.insertAdjacentHTML("beforeend", ev.data as String)
            scrollChatToBottom()
        })
        source.addEventListener("done", { _: dynamic -> source.close() })
    }
}

private fun scrollChatToBottom() {
    val ws = document.querySelector(".workspace") ?: return
    ws.scrollTop = ws.scrollHeight.toDouble()
}

// ---------------------------------------------------------------------------
//  Keyboard avoidance — keep the input above the soft keyboard on phones
// ---------------------------------------------------------------------------

private fun initKeyboardAvoidance() {
    val vv = window.asDynamic().visualViewport ?: return
    vv.addEventListener("resize", {
        if (window.innerWidth < 520) {
            (document.querySelector(".app") as? HTMLElement)?.style?.height = "${vv.height}px"
        }
    })
}
