package com.musa.atomcode

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.pre
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.strong
import kotlinx.html.style
import kotlinx.html.textArea
import kotlinx.html.title

// ===========================================================================
//  Full page
// ===========================================================================

fun HTML.idePage(session: Session) {
    lang = "en"
    head {
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1, viewport-fit=cover")
        title("Atom Code")
        link(rel = "preconnect", href = "https://fonts.googleapis.com")
        link(
            rel = "stylesheet",
            href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap",
        )
        link(rel = "stylesheet", href = "/atom.css")
    }
    body {
        div(classes = "app") {
            topNav(session)
            viewSwitcher()
            div(classes = "workspace") {
                id = "workspace"
                workspaceFragment(session)
            }
            contextPills(session)
            inputBar()
            controlBar(session)

            drawerLayer()
            settingsLayer()
            div { id = "overlay-root" }
        }
        script(src = "/web.js") {}
    }
}

/** The three swappable views (also returned on their own for session loads). */
fun FlowContent.workspaceFragment(session: Session) {
    chatView()
    terminalView()
    filesView()
}

// ===========================================================================
//  Top nav + view switcher
// ===========================================================================

private fun FlowContent.topNav(session: Session) {
    div(classes = "topnav") {
        button(classes = "icon-btn") {
            type = ButtonType.button
            attributes["data-action"] = "open-drawer"
            attributes["aria-label"] = "Menu"
            +"☰"
        }
        div(classes = "topnav-title") {
            div(classes = "topnav-name") { +session.title }
            div(classes = "topnav-path") { +"${session.repo} · ${session.branch}" }
        }
        engineStatusBadge(session)
        button(classes = "icon-btn") {
            type = ButtonType.button
            attributes["data-action"] = "open-settings"
            attributes["aria-label"] = "Settings"
            +"⚙"
        }
    }
}

fun FlowContent.engineStatusBadge(session: Session, oob: Boolean = false) {
    val offline = session.engine.offline
    span(classes = "engine-status " + if (offline) "is-offline" else "is-cloud") {
        id = "engine-status"
        if (oob) attributes["hx-swap-oob"] = "true"
        span(classes = "dot " + if (offline) "amber" else "green") {}
        +(if (offline) "GGUF·8B" else "Cloud")
    }
}

private fun FlowContent.viewSwitcher() {
    div(classes = "viewswitcher") {
        listOf("chat" to "Chat", "terminal" to "Terminal", "files" to "Files").forEach { (view, label) ->
            button(classes = "viewtab" + if (view == "chat") " active" else "") {
                type = ButtonType.button
                attributes["data-action"] = "switch-view"
                attributes["data-view"] = view
                +label
            }
        }
    }
}

// ===========================================================================
//  Chat view
// ===========================================================================

private fun FlowContent.chatView() {
    div(classes = "view active") {
        attributes["data-view"] = "chat"
        div(classes = "chat-stream") {
            id = "chat-stream"

            div(classes = "msg-user") {
                div(classes = "bubble-user") {
                    +"Build the debug APK and get it packaged so I can install it on this device."
                }
            }

            div(classes = "agent-id") {
                div(classes = "agent-avatar") { +"A" }
                span(classes = "agent-name") { +"Atom Agent" }
                span(classes = "agent-mode") { +"· auto-execute" }
            }

            commandAccordion()

            div(classes = "agent-text") {
                +"Compiled the debug APK successfully with "
                strong { +"AGP 8.13" }
                +". The artifact is signed with the debug keystore and ready to install."
            }

            apkCard()
            diffCard()
        }
    }
}

private fun FlowContent.commandAccordion() {
    div(classes = "accordion open") {
        button(classes = "accordion-head") {
            type = ButtonType.button
            attributes["data-action"] = "toggle-accordion"
            span(classes = "accordion-glyph") { +">_" }
            span(classes = "accordion-label") {
                span(classes = "accordion-title") { +"Ran 6 commands" }
                span(classes = "accordion-sub") { +"./gradlew assembleDebug" }
            }
            span(classes = "chevron") { +"⌄" }
        }
        div(classes = "accordion-body") {
            div { +"$ ./gradlew clean assembleDebug" }
            div(classes = "muted") { +"> Task :app:preBuild UP-TO-DATE" }
            div(classes = "muted") { +"> Task :app:compileDebugKotlin" }
            div(classes = "muted") { +"> Task :app:mergeDebugResources" }
            div(classes = "muted") { +"> Task :app:packageDebug" }
            div(classes = "c-purple") { +"> Task :app:assembleDebug" }
            div(classes = "c-green") { +"BUILD SUCCESSFUL in 47s" }
            div { +"32 actionable tasks: 30 executed, 2 up-to-date" }
        }
    }
}

private fun FlowContent.apkCard() {
    div(classes = "card") {
        div(classes = "artifact-head") {
            div(classes = "artifact-icon") { +"APK" }
            div(classes = "artifact-meta") {
                div(classes = "artifact-name") { +"app-debug.apk" }
                div(classes = "artifact-sub") { +"63.0 MB · debug-signed" }
            }
        }
        div(classes = "artifact-actions") {
            id = "apk-actions"
            button(classes = "btn-primary") {
                type = ButtonType.button
                attributes["data-action"] = "install"
                attributes["data-apk"] = "/sdcard/AtomCode/build/app-debug.apk"
                +"Install APK"
            }
            button(classes = "btn-secondary") {
                type = ButtonType.button
                +"Open dir"
            }
        }
    }
}

private fun FlowContent.diffCard() {
    button(classes = "diff-card") {
        type = ButtonType.button
        attributes["data-action"] = "switch-view"
        attributes["data-view"] = "files"
        div(classes = "diff-icon") { +"kt" }
        div(classes = "artifact-meta") {
            div(classes = "diff-file") { +"MainActivity.kt" }
            div(classes = "diff-stat") {
                span(classes = "add") { +"+18" }
                +" "
                span(classes = "del") { +"−4" }
                +" "
                span(classes = "muted") { +"· 2 files changed" }
            }
        }
        span(classes = "view-diff-link") { +"View diff ›" }
    }
}

// ===========================================================================
//  Terminal view
// ===========================================================================

private fun FlowContent.terminalView() {
    div(classes = "view") {
        attributes["data-view"] = "terminal"

        div(classes = "provision") {
            div(classes = "provision-head") {
                span(classes = "provision-title") { +"Toolchain provisioning" }
                span(classes = "provision-pct") { +"80%" }
            }
            div(classes = "progressbar") { div(classes = "progressbar-fill") { style = "width:80%" } }
            div(classes = "chips") {
                span(classes = "chip ok") { +"git ✓" }
                span(classes = "chip ok") { +"openjdk-17 ✓" }
                span(classes = "chip ok") { +"gradle ✓" }
                span(classes = "chip wait") { +"android-sdk ⋯" }
            }
        }

        div(classes = "console") {
            div {
                span(classes = "cwd") { +"~/AtomIDE" }
                +" "; span(classes = "sig") { +"$" }; +" pkg install openjdk-17 gradle"
            }
            div(classes = "term-line muted") { +"Extracting binaries … done (14.2 MB)" }
            // live output streamed from the terminal service
            div {
                id = "term-live"
                attributes["data-sse"] = "/api/logs/stream"
                attributes["data-sse-event"] = "log"
            }
            div {
                span(classes = "cwd") { +"~/AtomIDE" }
                +" "; span(classes = "sig") { +"$" }; +" "
                span(classes = "cursor") {}
            }
        }

        div(classes = "keybar") {
            listOf("Ctrl", "Alt", "Tab", "Esc", "|", "↑", "↓").forEach { span(classes = "key") { +it } }
        }
    }
}

// ===========================================================================
//  Files view
// ===========================================================================

private fun FlowContent.filesView() {
    div(classes = "view") {
        attributes["data-view"] = "files"
        div(classes = "filepath") { +"app / src / main / … / atomcode" }

        div(classes = "filetree") {
            treeDir("app/", 0)
            treeDir("src/main/", 1)
            treeFile("AndroidManifest.xml", 2)
            treeFile("MainActivity.kt", 2, modified = true, selected = true)
            treeFile("AgentBridge.kt", 2)
            treeFile("TerminalService.kt", 2)
            treeFile("build.gradle.kts", 0, modified = true)
        }

        div(classes = "diff-preview") {
            div(classes = "diff-preview-head") { +"MainActivity.kt" }
            div(classes = "diff-body") {
                div(classes = "ctx") { +"  private lateinit var bridge: AgentBridge" }
                div(classes = "minus") { +"- terminal.exec(\"gradlew assembleDebug\")" }
                div(classes = "plus") { +"+ bridge.run(Task.AssembleDebug) { apk ->" }
                div(classes = "plus") { +"+   installer.prompt(apk)" }
                div(classes = "plus") { +"+ }" }
                div(classes = "ctx") { +"  override fun onResume() { … }" }
            }
        }
    }
}

private fun FlowContent.treeDir(name: String, indent: Int) {
    div(classes = "tree-row" + indentClass(indent)) {
        span(classes = "caret") { +"▾" }
        +name
    }
}

private fun FlowContent.treeFile(name: String, indent: Int, modified: Boolean = false, selected: Boolean = false) {
    div(classes = "tree-row file" + indentClass(indent) + if (selected) " selected" else "") {
        span { +name }
        if (modified) span(classes = "badge-mod") { +"M" }
    }
}

private fun indentClass(indent: Int) = when (indent) {
    1 -> " indent-1"
    2 -> " indent-2"
    else -> ""
}

// ===========================================================================
//  Context pills + input + control bar
// ===========================================================================

private fun FlowContent.contextPills(session: Session) {
    div(classes = "contextpills") {
        div(classes = "pill") { span(classes = "dot cyan") {}; +"Local shell" }
        div(classes = "pill") { span(classes = "pill-code") { +"</>" }; +session.repo }
        div(classes = "pill") { span(classes = "dot green") {}; span(classes = "pill-mono") { +session.branch } }
    }
}

private fun FlowContent.inputBar() {
    form(classes = "inputbar") {
        attributes["hx-post"] = "/api/chat"
        attributes["hx-target"] = "#chat-stream"
        attributes["hx-swap"] = "beforeend"
        attributes["hx-on:htmx:after-request"] = "if(event.detail.successful) this.reset()"
        button(classes = "attach-btn") {
            type = ButtonType.button
            attributes["aria-label"] = "Attach"
            +"+"
        }
        textArea(rows = "1", classes = "prompt-input") {
            attributes["name"] = "prompt"
            attributes["placeholder"] = "Type a prompt or /command…"
            attributes["data-action"] = "autosize"
        }
        button(classes = "send-btn") {
            type = ButtonType.submit
            attributes["aria-label"] = "Send"
            +"↑"
        }
    }
}

private fun FlowContent.controlBar(session: Session) {
    div(classes = "controlbar") {
        execBadge(session)
        reasoningBadge(session)
        div(classes = "spacer") {}
        engineBadge(session)
        modelMenu()
    }
}

fun FlowContent.execBadge(session: Session) {
    val auto = session.execMode == ExecMode.AUTO
    button(classes = "ctrl-pill") {
        id = "exec-badge"
        type = ButtonType.button
        attributes["hx-post"] = "/api/settings/exec-mode"
        attributes["hx-target"] = "#exec-badge"
        attributes["hx-swap"] = "outerHTML"
        span(classes = "dot " + if (auto) "green" else "amber") {}
        +(if (auto) "Auto" else "Approval")
    }
}

fun FlowContent.reasoningBadge(session: Session) {
    button(classes = "ctrl-pill") {
        id = "reasoning-badge"
        type = ButtonType.button
        attributes["hx-post"] = "/api/settings/reasoning"
        attributes["hx-target"] = "#reasoning-badge"
        attributes["hx-swap"] = "outerHTML"
        +session.reasoning.label
    }
}

fun FlowContent.engineBadge(session: Session) {
    button(classes = "ctrl-pill model-btn") {
        id = "engine-badge"
        type = ButtonType.button
        attributes["data-action"] = "toggle-model-menu"
        span { +session.engine.label }
        span(classes = "chevron") { +"⌄" }
    }
}

private fun FlowContent.modelMenu() {
    div(classes = "model-menu") {
        id = "model-menu"
        div(classes = "menu-group") { +"Cloud API" }
        menuItem("Claude Fable 5", "green")
        div(classes = "menu-group") { +"Offline GGUF" }
        menuItem("Llama-3-8B-Q4", "amber")
        menuItem("DeepSeek-Coder", "amber")
    }
}

private fun FlowContent.menuItem(label: String, tone: String) {
    button(classes = "menu-item") {
        type = ButtonType.button
        attributes["hx-post"] = "/api/settings/model"
        attributes["hx-vals"] = """{"label":"$label"}"""
        attributes["hx-target"] = "#engine-badge"
        attributes["hx-swap"] = "outerHTML"
        attributes["data-action"] = "pick-model"
        span(classes = "dot $tone") {}
        span { +label }
    }
}

// ===========================================================================
//  Chat append + approval fragments
// ===========================================================================

/** Appends a user bubble plus an SSE-streamed agent reply container. */
fun FlowContent.chatAppend(session: Session, prompt: String) {
    val replyId = SessionStore.stagePrompt(prompt)
    div(classes = "msg-user") { div(classes = "bubble-user") { +prompt } }
    div(classes = "msg-agent") {
        div(classes = "agent-avatar") { +"A" }
        div(classes = "agent-text") {
            attributes["data-sse"] = "/api/chat/stream/$replyId"
            attributes["data-sse-event"] = "token"
        }
    }
}

fun FlowContent.approvalSheet(token: String, prompt: String) {
    div(classes = "sheet-scrim") {
        attributes["data-action"] = "close-overlay"
        div(classes = "approval") {
            attributes["data-action"] = "stop"
            div(classes = "approval-head") {
                span(classes = "approval-icon") { +"!" }
                span(classes = "approval-title") { +"Shell command approval" }
            }
            div(classes = "approval-desc") { +"The agent wants to run this before continuing:" }
            pre(classes = "approval-cmd") { +prompt }
            div(classes = "approval-actions") {
                button(classes = "deny") {
                    type = ButtonType.button
                    attributes["data-action"] = "close-overlay"
                    +"Deny"
                }
                button(classes = "approve") {
                    type = ButtonType.button
                    attributes["hx-post"] = "/api/chat/approve"
                    attributes["hx-vals"] = """{"token":"$token"}"""
                    attributes["hx-target"] = "#chat-stream"
                    attributes["hx-swap"] = "beforeend"
                    +"Approve & run"
                }
            }
        }
    }
}

// ===========================================================================
//  Drawer + settings (JS-toggled overlay layers)
// ===========================================================================

private fun FlowContent.drawerLayer() {
    div(classes = "overlay-layer") {
        id = "drawer-layer"
        div(classes = "scrim") { attributes["data-action"] = "close-drawer" }
        div(classes = "drawer") {
            div(classes = "drawer-profile") {
                div(classes = "drawer-avatar") { +"M" }
                div(classes = "topnav-title") {
                    div(classes = "drawer-name") { +"Musa" }
                    div(classes = "drawer-plan") { +"Pro · Termux Native" }
                }
            }
            div(classes = "drawer-body") {
                button(classes = "drawer-newtask") {
                    type = ButtonType.button
                    attributes["hx-post"] = "/api/session/new"
                    +"+ New task"
                }
                div(classes = "drawer-section") { +"Workspace" }
                listOf("Sessions", "Projects", "Artifacts", "MCP skills").forEach {
                    button(classes = "drawer-link") { type = ButtonType.button; +it }
                }
                div(classes = "drawer-section spaced") { +"Recent tasks" }
                SessionStore.recentTasks.forEach { recentTaskRow(it) }
            }
            button(classes = "drawer-settings") {
                type = ButtonType.button
                attributes["data-action"] = "open-settings"
                +"⚙  Settings"
            }
        }
    }
}

private fun FlowContent.recentTaskRow(task: RecentTask) {
    val tone = when (task.status) {
        TaskStatus.NEEDS_INPUT -> "amber"
        TaskStatus.RUNNING -> "purple"
        TaskStatus.COMPLETED -> "green"
    }
    div(classes = "recent" + if (task.status == TaskStatus.NEEDS_INPUT) " active" else "") {
        span(classes = "dot $tone") {}
        div(classes = "recent-body") {
            div(classes = "recent-title") { +task.title }
            div(classes = "recent-sub") { +"${task.status.label} · ${task.age}" }
        }
    }
}

private fun FlowContent.settingsLayer() {
    div(classes = "overlay-layer") {
        id = "settings-layer"
        div(classes = "sheet-scrim") {
            attributes["data-action"] = "close-settings"
            div(classes = "sheet") {
                attributes["data-action"] = "stop"
                div(classes = "sheet-grip-wrap") { div(classes = "sheet-grip") {} }
                div(classes = "sheet-head") {
                    span(classes = "sheet-title") { +"Settings" }
                    button(classes = "sheet-close") {
                        type = ButtonType.button
                        attributes["data-action"] = "close-settings"
                        +"✕"
                    }
                }
                div(classes = "sheet-tabs") {
                    listOf("general" to "General", "engine" to "Engine", "mcp" to "MCP", "git" to "Git").forEach { (tab, label) ->
                        button(classes = "sheet-tab" + if (tab == "engine") " active" else "") {
                            type = ButtonType.button
                            attributes["data-action"] = "select-tab"
                            attributes["data-tab"] = tab
                            +label
                        }
                    }
                }
                div(classes = "sheet-body") {
                    generalPane()
                    enginePane()
                    mcpPane()
                    gitPane()
                }
            }
        }
    }
}

private fun FlowContent.generalPane() {
    div(classes = "settings-pane") {
        attributes["data-pane"] = "general"
        div {
            div(classes = "field-label") { +"Preferred name" }
            div(classes = "field-value") { +"Musa" }
        }
        div {
            div(classes = "field-label") { +"Default workspace" }
            div(classes = "field-value mono") { +"~/AtomIDE" }
        }
        div(classes = "field-row") {
            span { +"AMOLED dark theme" }
            span(classes = "toggle") {}
        }
    }
}

private fun FlowContent.enginePane() {
    div(classes = "settings-pane active") {
        attributes["data-pane"] = "engine"
        div {
            div(classes = "field-label") { +"Offline model path" }
            div(classes = "field-value mono") { +"/sdcard/AtomCode/models/llama-3-8b-q4.gguf" }
        }
        div {
            div(classes = "field-row") {
                span(classes = "field-label") { +"CPU threads" }
                span(classes = "stepper-value") { id = "threads-value"; +"4" }
            }
            div(classes = "stepper") {
                button { type = ButtonType.button; attributes["data-action"] = "dec-threads"; +"−" }
                div(classes = "track") { div(classes = "fill") { id = "threads-fill"; style = "width:50%" } }
                button { type = ButtonType.button; attributes["data-action"] = "inc-threads"; +"+" }
            }
        }
        div {
            div(classes = "field-label") { +"Context window" }
            div(classes = "ctx-group") {
                listOf("2048", "4096", "8192").forEachIndexed { i, v ->
                    button(classes = "ctx-opt" + if (i == 1) " active" else "") {
                        type = ButtonType.button
                        attributes["data-action"] = "set-ctx"
                        attributes["data-ctx"] = i.toString()
                        +v
                    }
                }
            }
        }
    }
}

private fun FlowContent.mcpPane() {
    div(classes = "settings-pane") {
        attributes["data-pane"] = "mcp"
        listOf("file_system", "terminal_exec", "android_sdk_bridge").forEach { name ->
            div(classes = "mcp-row") {
                span(classes = "dot green") {}
                span(classes = "mcp-name") { +name }
                span(classes = "mcp-state") { +"active" }
            }
        }
        button(classes = "mcp-add") { type = ButtonType.button; +"+ Add MCP server" }
    }
}

private fun FlowContent.gitPane() {
    div(classes = "settings-pane") {
        attributes["data-pane"] = "git"
        div {
            div(classes = "field-label") { +"SSH key" }
            div(classes = "field-value mono field-row") {
                span { +"id_ed25519" }
                span(classes = "mcp-state") { +"loaded" }
            }
        }
        div {
            div(classes = "field-label") { +"GitHub token" }
            div(classes = "field-value mono") { +"ghp_••••••••••••••••4c2a" }
        }
        div(classes = "note") { +"Used for PR creation and remote pushes from the agent." }
    }
}
