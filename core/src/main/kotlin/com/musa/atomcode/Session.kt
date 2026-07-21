package com.musa.atomcode

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** A single chat turn appended to a session after the seeded showcase content. */
enum class Role { USER, AGENT }

data class Message(
    val id: String,
    val role: Role,
    val text: String,
)

/** Whether the agent runs shell commands automatically or pauses for approval. */
enum class ExecMode { AUTO, APPROVAL }

enum class Reasoning(val label: String) { LOW("Low"), MEDIUM("Medium"), HIGH("High") }

/** An inference engine — either a cloud API or an on-device GGUF model. */
data class Engine(val label: String, val offline: Boolean)

object Engines {
    val CLOUD = Engine("Claude Fable 5", offline = false)
    val LLAMA = Engine("Llama-3-8B-Q4", offline = true)
    val DEEPSEEK = Engine("DeepSeek-Coder", offline = true)
    val all = listOf(CLOUD, LLAMA, DEEPSEEK)
    fun byLabel(label: String): Engine? = all.firstOrNull { it.label == label }
}

enum class TaskStatus(val label: String) {
    NEEDS_INPUT("Needs input"),
    RUNNING("Running"),
    COMPLETED("Completed"),
}

/** An entry in the drawer's "recent tasks" list. */
data class RecentTask(val title: String, val status: TaskStatus, val age: String)

/** Mutable, per-user working session. In-memory only for this phase. */
class Session(
    val id: String,
    var title: String,
    var repo: String = "AtomIDE",
    var branch: String = "master",
    var engine: Engine = Engines.LLAMA,
    var execMode: ExecMode = ExecMode.AUTO,
    var reasoning: Reasoning = Reasoning.HIGH,
    val messages: MutableList<Message> = mutableListOf(),
)

/**
 * In-memory session registry. A real implementation would persist sessions; for now this holds
 * everything for the current process (shared by the standalone server and the Android-embedded one).
 */
object SessionStore {

    private val sessions = ConcurrentHashMap<String, Session>()

    /** Prompts awaiting an SSE reply stream, keyed by the placeholder agent-message id. */
    private val pendingReplies = ConcurrentHashMap<String, String>()

    val recentTasks = listOf(
        RecentTask("APK assembly", TaskStatus.NEEDS_INPUT, "2m"),
        RecentTask("Migrate Room DB v3", TaskStatus.RUNNING, "8m"),
        RecentTask("Fix lint + CI pipeline", TaskStatus.COMPLETED, "1h"),
    )

    fun get(id: String): Session? = sessions[id]

    fun create(title: String = "New task"): Session {
        val session = Session(UUID.randomUUID().toString(), title)
        sessions[session.id] = session
        return session
    }

    /** Returns the session for [id], creating a fresh default one if it is missing or unknown. */
    fun resolve(id: String?): Session {
        id?.let { existing -> sessions[existing]?.let { return it } }
        return create("APK Assembly")
    }

    fun stagePrompt(prompt: String): String {
        val messageId = UUID.randomUUID().toString()
        pendingReplies[messageId] = prompt
        return messageId
    }

    fun consumePrompt(messageId: String): String? = pendingReplies.remove(messageId)
}
