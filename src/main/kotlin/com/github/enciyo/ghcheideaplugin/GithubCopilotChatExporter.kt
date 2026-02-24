package com.github.enciyo.ghcheideaplugin

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.awt.Container

object GithubCopilotChatExporter {

    private const val CHAT_WINDOW_ID = "GitHub Copilot Chat"

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    fun export(project: Project) {
        job?.cancel()
        job = scope.launch {
            try {
                val manager = ToolWindowManager.getInstance(project)
                val toolWindow = manager.getToolWindow(CHAT_WINDOW_ID)

                if (toolWindow == null) {
                    thisLogger().error("GitHub Copilot Chat ToolWindow ('$CHAT_WINDOW_ID') not found. Please ensure GitHub Copilot Chat is open.")
                    return@launch
                }

                val contentManager = toolWindow.contentManager
                val content = contentManager.contents

                if (content.isEmpty()) {
                    thisLogger().warn("No content found in GitHub Copilot Chat ToolWindow.")
                    return@launch
                }

                val mdExporter = MarkdownExport(project)
                val firstContent = content.firstOrNull() ?: return@launch

                val component = firstContent.component
                val chats = component.findChat()

                if (chats.isEmpty()) {
                    thisLogger().warn("No chat messages found in the component.")
                } else {
                    thisLogger().info("Found ${chats.size} chat messages. Proceeding with export.")
                    mdExporter.export(chats)
                }
            } catch (e: Exception) {
                thisLogger().error("Unexpected error during export: ${e.message}", e)
            }
        }
    }

}
