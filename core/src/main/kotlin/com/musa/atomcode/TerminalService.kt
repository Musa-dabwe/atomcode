package com.musa.atomcode

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Streams terminal / build output line by line. The stub replays a canned Gradle assemble; a real
 * implementation would attach to a ProcessBuilder (JVM) or an Android exec / Termux session.
 */
interface TerminalService {
    fun stream(): Flow<TerminalLine>
}

/** One console line plus a semantic tone the UI colours it with. */
data class TerminalLine(val text: String, val tone: Tone = Tone.PLAIN) {
    enum class Tone { PLAIN, MUTED, ACCENT, SUCCESS }
}

class StubTerminalService : TerminalService {
    override fun stream(): Flow<TerminalLine> = flow {
        val lines = listOf(
            TerminalLine("$ ./gradlew clean assembleDebug"),
            TerminalLine("> Task :app:preBuild UP-TO-DATE", TerminalLine.Tone.MUTED),
            TerminalLine("> Task :app:compileDebugKotlin", TerminalLine.Tone.MUTED),
            TerminalLine("> Task :app:mergeDebugResources", TerminalLine.Tone.MUTED),
            TerminalLine("> Task :app:packageDebug", TerminalLine.Tone.MUTED),
            TerminalLine("> Task :app:assembleDebug", TerminalLine.Tone.ACCENT),
            TerminalLine("BUILD SUCCESSFUL in 47s", TerminalLine.Tone.SUCCESS),
            TerminalLine("32 actionable tasks: 30 executed, 2 up-to-date"),
        )
        for (line in lines) {
            emit(line)
            delay(420)
        }
    }
}
