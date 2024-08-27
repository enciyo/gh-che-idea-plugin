package com.github.enciyo.ghcheideaplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

object GithubCopilotChatExporter {

    private const val CHAT_WINDOW_ID = "GitHub Copilot Chat"

    fun export(project: Project) {
        val manager = ToolWindowManager.getInstance(project)
        val toolWindow = manager.getToolWindow(CHAT_WINDOW_ID) ?: return
        val contentManager = toolWindow.contentManager
        val content = contentManager.contents
        val mdExporter = MarkdownExport(project)
        content.forEach {
            val component = it.component
            val chats = component.findChat()
            mdExporter.export(chats)
        }
    }

}