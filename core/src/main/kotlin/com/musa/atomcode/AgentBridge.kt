package com.musa.atomcode

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Produces the agent's reply to a prompt as a stream of text chunks. The stub emits a canned,
 * word-by-word plan; a real implementation would drive the Cloud API or an on-device GGUF model.
 */
interface AgentBridge {
    fun reply(prompt: String, engine: Engine): Flow<String>
}

class StubAgentBridge : AgentBridge {
    override fun reply(prompt: String, engine: Engine): Flow<String> = flow {
        val opener = if (engine.offline) {
            "Running locally on ${engine.label}."
        } else {
            "Using ${engine.label} in the cloud."
        }
        val body = "$opener On it — I'll plan the steps, prepare the local shell, " +
            "compile the module, run the checks, and report back with the artifact."
        for (word in body.split(" ")) {
            emit("$word ")
            delay(40)
        }
    }
}
