package com.musa.atomcode

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import io.ktor.server.html.respondHtmlFragment
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.sse.sse
import kotlinx.coroutines.flow.collect
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id

/** Resolves (or lazily creates) the caller's session and keeps the cookie in sync. */
fun ApplicationCall.currentSession(): Session {
    val cookie = sessions.get<UserSession>()
    val session = SessionStore.resolve(cookie?.id)
    if (cookie?.id != session.id) sessions.set(UserSession(session.id))
    return session
}

fun Application.configureRouting() {
    routing {
        staticResources("/", "/web")

        // Full page ---------------------------------------------------------
        get("/") {
            val session = call.currentSession()
            call.respondHtml { idePage(session) }
        }

        // Prompt submit -----------------------------------------------------
        post("/api/chat") {
            val session = call.currentSession()
            val prompt = call.receiveParameters()["prompt"].orEmpty().trim()
            if (prompt.isEmpty()) {
                call.respondText("")
                return@post
            }
            if (session.execMode == ExecMode.APPROVAL) {
                // Gate the run: retarget the response to the overlay layer as an approval sheet.
                val token = SessionStore.stagePrompt(prompt)
                call.response.header("HX-Retarget", "#overlay-root")
                call.response.header("HX-Reswap", "innerHTML")
                call.respondHtmlFragment { approvalSheet(token, prompt) }
            } else {
                call.respondHtmlFragment { chatAppend(session, prompt) }
            }
        }

        // Approve a gated command, then run it ------------------------------
        post("/api/chat/approve") {
            val session = call.currentSession()
            val token = call.receiveParameters()["token"].orEmpty()
            val prompt = SessionStore.consumePrompt(token)
            if (prompt == null) {
                call.respondText("")
                return@post
            }
            call.respondHtmlFragment {
                chatAppend(session, prompt)
                clearOverlayOob()
            }
        }

        // Streamed agent reply ---------------------------------------------
        sse("/api/chat/stream/{id}") {
            val session = call.currentSession()
            val id = call.parameters["id"].orEmpty()
            val prompt = SessionStore.consumePrompt(id)
            if (prompt != null) {
                agentBridge.reply(prompt, session.engine).collect { token ->
                    send(event = "token", data = token)
                }
            }
            send(event = "done", data = "")
        }

        // Streamed terminal / build output ---------------------------------
        sse("/api/logs/stream") {
            terminalService.stream().collect { line ->
                send(event = "log", data = renderTermLine(line))
            }
            send(event = "done", data = "")
        }

        // Session load / create --------------------------------------------
        get("/ui/session/{id}") {
            val session = call.currentSession()
            call.respondHtmlFragment { workspaceFragment(session) }
        }
        post("/api/session/new") {
            val session = SessionStore.create()
            call.sessions.set(UserSession(session.id))
            call.response.header("HX-Redirect", "/")
            call.respondText("")
        }

        // Control-bar settings ---------------------------------------------
        post("/api/settings/exec-mode") {
            val session = call.currentSession()
            session.execMode = if (session.execMode == ExecMode.AUTO) ExecMode.APPROVAL else ExecMode.AUTO
            call.respondHtmlFragment { execBadge(session) }
        }
        post("/api/settings/reasoning") {
            val session = call.currentSession()
            val order = Reasoning.entries
            session.reasoning = order[(order.indexOf(session.reasoning) + 1) % order.size]
            call.respondHtmlFragment { reasoningBadge(session) }
        }
        post("/api/settings/model") {
            val session = call.currentSession()
            val label = call.receiveParameters()["label"].orEmpty()
            Engines.byLabel(label)?.let { session.engine = it }
            call.respondHtmlFragment {
                engineBadge(session)
                engineStatusBadge(session, oob = true)
            }
        }
    }
}

/** Renders one terminal line as a single-node HTML fragment safe to send over SSE (no newlines). */
private fun renderTermLine(line: TerminalLine): String {
    val tone = when (line.tone) {
        TerminalLine.Tone.MUTED -> " muted"
        TerminalLine.Tone.ACCENT -> " accent"
        TerminalLine.Tone.SUCCESS -> " success"
        TerminalLine.Tone.PLAIN -> ""
    }
    return "<div class=\"term-line$tone\">${escapeHtml(line.text)}</div>"
}

fun escapeHtml(s: String): String = buildString {
    for (c in s) when (c) {
        '&' -> append("&amp;")
        '<' -> append("&lt;")
        '>' -> append("&gt;")
        '"' -> append("&quot;")
        else -> append(c)
    }
}

/** Out-of-band swap that empties the overlay layer (dismisses the approval sheet). */
private fun FlowContent.clearOverlayOob() {
    div {
        id = "overlay-root"
        attributes["hx-swap-oob"] = "innerHTML"
    }
}
