package com.github.enciyo.ghcheideaplugin.toolWindow

import com.github.enciyo.ghcheideaplugin.Github
import com.github.enciyo.ghcheideaplugin.GithubCopilotChatExporter
import com.github.enciyo.ghcheideaplugin.listener.AppBranchChangeListener
import com.github.enciyo.ghcheideaplugin.service.AppSettingsService
import com.github.enciyo.ghcheideaplugin.service.AppState
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.*
import com.intellij.util.application


class ExportSettingsFactory : ToolWindowFactory {

    private val service = application.service<AppSettingsService>()


    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        service.initialize(project)
        val myToolWindow = ExportSettings(toolWindow)
        val panel = myToolWindow.getContent()
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
        service.onUpdateState = {
            panel.reset()
        }
    }


    override fun shouldBeAvailable(project: Project) = true

    class ExportSettings(private val toolWindow: ToolWindow) {

        private val branchChangeListener = toolWindow.project.service<AppBranchChangeListener>()
        private val github = Github(toolWindow.project)

        private val settings = application.service<AppSettingsService>()

        private val state: AppState
            get() = settings.state


        fun getContent() = panel {

            row("Author (Default: Git Config user.name)") {
                textField()
                    .text(state.author.orEmpty())
                    .align(AlignX.FILL)
                    .onChanged {
                        state.author = it.text
                    }
            }
            row("File and Header Name (Default: Current Branch Name)") {
                textField()
                    .bindText(
                        { state.fileName.orEmpty() },
                        { state.fileName = it }
                    )
                    .onChanged {
                        state.fileName = it.text
                    }
                    .align(AlignX.FILL)
            }
            row("Regex for file name (By Branch Name)") {
                textField()
                    .text(state.regex.orEmpty())
                    .align(AlignX.FILL)
                    .onChanged {
                        state.regex = it.text
                    }

            }
            row {
                checkBox("Use Regex for file Name")
                    .selected(state.useRegex)
                    .onChanged {
                        state.useRegex = it.isSelected
                        branchChangeListener.branchHasChanged(github.getCurrentBranchName())
                    }
            }

            row {
                button("Export") {
                    GithubCopilotChatExporter.export(toolWindow.project)
                }
            }
        }
    }
}

