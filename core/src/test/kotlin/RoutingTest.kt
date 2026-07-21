package com.musa.atomcode

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class RoutingTest {

    @Test
    fun `root renders the IDE shell`() = testApplication {
        application { module() }
        val res = client.get("/")
        assertEquals(HttpStatusCode.OK, res.status)
        val body = res.bodyAsText()
        assertContains(body, "Atom Code")
        assertContains(body, "id=\"chat-stream\"")
        assertContains(body, "Local shell")
    }

    @Test
    fun `posting a prompt returns a chat fragment with an SSE reply container`() = testApplication {
        application { module() }
        val res = client.post("/api/chat") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody("prompt=hello+there")
        }
        assertEquals(HttpStatusCode.OK, res.status)
        val body = res.bodyAsText()
        assertContains(body, "bubble-user")
        assertContains(body, "hello there")
        assertContains(body, "/api/chat/stream/")
    }

    @Test
    fun `toggling exec-mode returns the badge fragment`() = testApplication {
        application { module() }
        val res = client.post("/api/settings/exec-mode")
        assertEquals(HttpStatusCode.OK, res.status)
        // A fresh session starts in AUTO, so the first toggle yields APPROVAL.
        assertContains(res.bodyAsText(), "Approval")
    }

    @Test
    fun `terminal log stream emits build output`() = testApplication {
        application { module() }
        val body = client.get("/api/logs/stream").bodyAsText()
        assertContains(body, "term-line")
        assertContains(body, "BUILD SUCCESSFUL")
    }
}
