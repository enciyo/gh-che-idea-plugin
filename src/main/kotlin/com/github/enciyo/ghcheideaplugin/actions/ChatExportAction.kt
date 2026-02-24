package com.github.enciyo.ghcheideaplugin.actions

import com.github.enciyo.ghcheideaplugin.GithubCopilotChatExporter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger


class ChatExportAction : AnAction() {


    override fun actionPerformed(e: AnActionEvent) {
        thisLogger().debug("ChatExportAction triggered via actionPerformed")
        val project = e.project
        if (project == null) {
            thisLogger().error("ChatExportAction: Project is null, cannot proceed with export")
            return
        }
        GithubCopilotChatExporter.export(project)
    }

}
