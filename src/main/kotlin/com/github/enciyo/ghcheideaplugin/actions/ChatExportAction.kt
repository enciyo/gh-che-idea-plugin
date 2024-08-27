package com.github.enciyo.ghcheideaplugin.actions

import com.github.enciyo.ghcheideaplugin.GithubCopilotChatExporter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class ChatExportAction : AnAction() {


    override fun actionPerformed(e: AnActionEvent) {
        println("ChatExportAction clicked")
        GithubCopilotChatExporter.export(e.project ?: return)
    }

}


