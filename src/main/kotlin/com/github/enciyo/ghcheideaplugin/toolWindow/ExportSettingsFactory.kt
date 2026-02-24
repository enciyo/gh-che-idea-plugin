package com.github.enciyo.ghcheideaplugin.toolWindow

import com.github.enciyo.ghcheideaplugin.Github
import com.github.enciyo.ghcheideaplugin.GithubCopilotChatExporter
import com.github.enciyo.ghcheideaplugin.listener.AppBranchChangeListener
import com.github.enciyo.ghcheideaplugin.service.AppSettingsService
import com.github.enciyo.ghcheideaplugin.service.AppState
import com.intellij.ide.BrowserUtil
import com.intellij.icons.AllIcons
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
            row {
                label("Export Configuration").bold()
            }.bottomGap(BottomGap.SMALL)

            // Author Section
            row {
                label("Author name")
            }.bottomGap(BottomGap.NONE)
            row {
                textField()
                    .align(AlignX.FILL)
                    .text(state.author.orEmpty())
                    .applyToComponent { emptyText.text = "e.g., John Doe" }
                    .onChanged { state.author = it.text }
            }
            row {
                comment("Displayed in the <b>#### Author</b> section of each chat.")
            }.bottomGap(BottomGap.SMALL)

            // Filename Section
            row {
                label("Filename / Header")
            }.bottomGap(BottomGap.NONE)
            row {
                textField()
                    .align(AlignX.FILL)
                    .text(state.fileName.orEmpty())
                    .applyToComponent { emptyText.text = "e.g., my-feature-name" }
                    .onChanged { state.fileName = it.text }
            }
            row {
                comment("Used as the <b>.md file name</b> and the main header.")
            }.bottomGap(BottomGap.SMALL)

            // Export Path Section
            row {
                label("Export Directory")
            }.bottomGap(BottomGap.NONE)
            row {
                textField()
                    .align(AlignX.FILL)
                    .text(state.exportPath.orEmpty())
                    .applyToComponent { emptyText.text = "e.g., /ai/copilot/prompts" }
                    .onChanged { state.exportPath = it.text }
            }
            row {
                comment("The relative path within your project where files will be saved.")
            }.bottomGap(BottomGap.SMALL)

            // Filter Section
            row {
                label("Filter Regex (Optional)")
            }.bottomGap(BottomGap.NONE)
            row {
                textField()
                    .align(AlignX.FILL)
                    .text(state.regex.orEmpty())
                    .applyToComponent { emptyText.text = "e.g., [A-Z]+\\-\\d+" }
                    .onChanged { state.regex = it.text }
            }
            row {
                comment("Extract data (like Jira IDs) from your branch name.")
            }.bottomGap(BottomGap.SMALL)

            row {
                checkBox("Update filename automatically via regex")
                    .selected(state.useRegex)
                    .onChanged {
                        state.useRegex = it.isSelected
                        branchChangeListener.branchHasChanged(github.getCurrentBranchName())
                    }
            }

            separator().topGap(TopGap.MEDIUM)

            row {
                button("Export to Markdown") {
                    GithubCopilotChatExporter.export(toolWindow.project)
                }.align(AlignX.FILL)
                    .applyToComponent { icon = AllIcons.Actions.Download }
            }.topGap(TopGap.SMALL)

            row {
                link("Report Issue") {
                    BrowserUtil.browse("https://github.com/enciyo/gh-che-idea-plugin/issues")
                }.applyToComponent { icon = AllIcons.Actions.Help }
                    .align(AlignX.FILL)

                link("Star Project") {
                    BrowserUtil.browse("https://github.com/enciyo/gh-che-idea-plugin")
                }.applyToComponent { icon = AllIcons.Nodes.Favorite }
                    .align(AlignX.FILL)
            }
        }
    }
}
