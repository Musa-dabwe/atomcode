# Atom Code

An AI-powered **mobile IDE** — a dark, phone-first agent workspace (chat + terminal + files)
served by an embedded **Ktor** server and driven by **HTMX** with a small Kotlin/JS interaction
layer. The same server runs both as a standalone dev process and embedded inside an Android app
that renders it in a WebView.

> Status: UI shell + backend foundations. Agent replies and terminal output stream over SSE from
> **stub** back-ends; real LLM inference (Cloud API / offline GGUF) and real shell execution are a
> later phase.

## Modules

| Module    | What it is                                                                                  |
|-----------|---------------------------------------------------------------------------------------------|
| `:core`   | The shared Ktor app — routing, kotlinx.html UI, session state, SSE endpoints, stub bridges. Engine-agnostic. |
| `:server` | Standalone dev entrypoint: Netty `EngineMain` running `:core`'s `module()`.                 |
| `:web`    | Kotlin/JS bundle (`web.js`): bundles htmx + the interaction layer, copied into `:core` resources. |
| `:app`    | Android application: starts an embedded **CIO** server (`:core`'s `module()`) and loads it in a WebView. |

## Architecture notes

- **Hybrid interactivity.** Presentational chrome (drawer, view switch, sheets, model popover,
  accordion, textarea autosize, SSE wiring) is handled by the Kotlin/JS layer in `:web`. Meaningful
  actions (prompt submit, approval gating, settings) go through HTMX to `:core` endpoints; per-user
  state lives server-side in an in-memory `SessionStore` keyed by a session cookie.
- **Streaming.** Agent replies (`/api/chat/stream/{id}`) and terminal/build output
  (`/api/logs/stream`) are Server-Sent Events; the browser appends chunks via a small `data-sse`
  helper (no external htmx SSE extension needed).
- **Stub back-ends.** `AgentBridge` and `TerminalService` (in `:core`) emit canned streams; swap in
  real ProcessBuilder / Android-exec / llama.cpp / Cloud API implementations later.

## Build & run

Requires JDK 17. The Android module needs an Android SDK; point `local.properties` at it
(`sdk.dir=/path/to/Android/Sdk`) — compileSdk 36.

```bash
./gradlew :server:run          # dev server at http://127.0.0.1:8080  (open in a mobile viewport)
./gradlew :core:test           # backend tests (routes, chat fragment, SSE, exec-mode)
./gradlew :app:assembleDebug   # build the Android APK (WebView host + embedded server)
```
