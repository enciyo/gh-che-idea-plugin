package com.github.enciyo.ghcheideaplugin

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object GithubCopilotChatExporter {

    private const val CHAT_WINDOW_ID = "GitHub Copilot Chat"

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job : Job? = null

    fun export(project: Project) {
        job?.cancel()
        job = scope.launch {
            val manager = ToolWindowManager.getInstance(project)
            val toolWindow = manager.getToolWindow(CHAT_WINDOW_ID) ?: run {
                thisLogger().warn("ToolWindow not found")
                return@launch
            }
            val contentManager = toolWindow.contentManager
            val content = contentManager.contents
            val mdExporter = MarkdownExport(project)
            content.forEach {
                val component = it.component
                val chats = component.findChat()
                thisLogger().warn("Chats: ${chats.size}")
                mdExporter.export(chats)
            }
        }
    }

}