package com.musa.atomcode

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sse.SSE

/** Cookie payload used to correlate a browser with its in-memory [Session]. */
data class UserSession(val id: String)

/** Shared stub back-ends. Swap these for real implementations in a later phase. */
val agentBridge: AgentBridge = StubAgentBridge()
val terminalService: TerminalService = StubTerminalService()

/**
 * The single Ktor application shared by every entrypoint — the standalone Netty dev server
 * (`:server`) and the Android-embedded CIO server (`:app`). Only the engine differs per host.
 */
fun Application.module() {
    install(Sessions) {
        cookie<UserSession>("ATOM_SESSION")
    }
    install(SSE)
    configureRouting()
}
