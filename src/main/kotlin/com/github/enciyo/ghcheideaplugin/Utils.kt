package com.github.enciyo.ghcheideaplugin

import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.LanguageTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Component
import java.awt.Container
import java.io.File
import javax.swing.text.JTextComponent

private const val COPILOT_AGENT_MESSAGE_COMPONENT = "CopilotAgentMessageComponent"
private const val MESSAGE_CONTENT_PANEL = "MessageContentPanel"
private const val MARKDOWN_PANE = "MarkdownPane"
private const val HTML_CONTENT_COMPONENT = "HtmlContentComponent"
private const val MyEditorTextField = "MyEditorTextField"
private const val TOGGLE_BUTTONS = "MutuallyExclusiveToggleActionButtonGroup"

suspend fun Container.findComponentsByClassName(className: String): List<Component> = withContext(Dispatchers.Default) {
    val components = mutableListOf<Component>()
    for (component in this@findComponentsByClassName.components) {
        val simpleName = component.javaClass.simpleName
        val fullName = component.javaClass.name
        if (simpleName == className || fullName == className || fullName.endsWith(".$className")) {
            components.add(component)
        }
        if (component is Container) {
            val childComponents = component.findComponentsByClassName(className)
            components.addAll(childComponents)
        }
    }
    return@withContext components
}


fun Container.getBeautyContainerTree(level: Int = 0): String {
    val sb = StringBuilder()
    sb.append("  ".repeat(level))
    sb.append(this.javaClass.name).append(" (").append(this.javaClass.simpleName).append(")\n")
    for (component in this.components) {
        if (component is Container) {
            sb.append(component.getBeautyContainerTree(level + 1))
        } else {
            for (i in 0 until level + 1) {
                sb.append("  ")
            }
            sb.append(component.javaClass.name).append(" (").append(component.javaClass.simpleName).append(")\n")
        }
    }
    return sb.toString()
}

fun Component.asContainer(): Container {
    return this as Container
}

fun List<Component>.asContainer(): List<Container> {
    return this.map { it as Container }
}


suspend fun Container.findText(): String {
    var message = ""

    // Try to find MarkdownPane first (Newer version)
    val markdownPanes = findComponentsByClassName(MARKDOWN_PANE)
    if (markdownPanes.isNotEmpty()) {
        markdownPanes.forEach {
            (it as? JTextComponent)?.let { textComp ->
                message += textComp.text + "\n"
            }
        }
        if (message.isNotBlank()) {
            return message
        }
    }

    // Fallback to HtmlContentComponent or other structures
    findComponentsByClassName(MESSAGE_CONTENT_PANEL)
        .asContainer()
        .onEach { panel ->
            panel.components.forEach {
                val className = it::class.java.simpleName
                when (className) {
                    HTML_CONTENT_COMPONENT -> {
                        (it as? JTextComponent)?.let { textComp ->
                            val wrappedData = textComp.text.lines().joinToString("")
                            message += "${wrappedData}\n"
                        }
                    }

                    "CodeBlockContainer" -> {
                        (it.asContainer().findComponentsByClassName(MyEditorTextField)
                            .firstOrNull() as? LanguageTextField)?.let { editor ->
                            message += "\n```\n" + editor.text + "\n```\n\n"
                        }
                    }
                }
            }
        }

    return message
}


suspend fun Container.findVote(): String {
    val toggleButtons = findComponentsByClassName(TOGGLE_BUTTONS)
    if (toggleButtons.isEmpty()) return "**"

    val votes = toggleButtons
        .first()
        .asContainer()
        .findComponentsByClassName(ActionButton::class.java.simpleName)
        .map { it as ActionButton }

    if (votes.isEmpty()) return "**"

    return when {
        votes.firstOrNull()?.isSelected == true -> "***"
        votes.lastOrNull()?.isSelected == true -> "*"
        else -> "**"
    }
}


suspend fun Container.findChat(): List<Prompt> = withContext(Dispatchers.Default) {
    thisLogger().debug("Starting findChat analysis")
    val allMessages = findComponentsByClassName(COPILOT_AGENT_MESSAGE_COMPONENT)

    thisLogger().debug("Found ${allMessages.size} total agent message components.")

    if (allMessages.isEmpty()) {
        val tree = getBeautyContainerTree()
        thisLogger().debug(
            "\n" +
                    "##################################################\n" +
                    "#           UI STRUCTURE DEBUG START             #\n" +
                    "##################################################\n" +
                    tree + "\n" +
                    "##################################################\n" +
                    "#            UI STRUCTURE DEBUG END              #\n" +
                    "##################################################"
        )
        return@withContext emptyList<Prompt>()
    }

    val chats = mutableListOf<Prompt>()
    var currentUserPrompt = ""

    allMessages.forEachIndexed { index, component ->
        val container = component.asContainer()
        val text = container.findText()

        // In the new UI, user messages and copilot messages are both CopilotAgentMessageComponent.
        val isUserMessage = container.getBeautyContainerTree().contains("MessageContentBubble")

        if (isUserMessage) {
            thisLogger().debug("Message #$index: USER PROMPT detected (length: ${text.length})")
            currentUserPrompt = text
        } else {
            thisLogger().debug("Message #$index: COPILOT ANSWER detected (length: ${text.length})")
            // It's a copilot response
            if (currentUserPrompt.isNotBlank()) {
                val vote = container.findVote()
                chats.add(
                    Prompt(
                        user = currentUserPrompt,
                        copilot = text,
                        vote = vote
                    )
                )
                thisLogger().debug("Message #$index: Paired with previous prompt. Total chats: ${chats.size}")
                currentUserPrompt = "" // Reset for next pair
            } else {
                thisLogger().debug("Message #$index: Copilot answer found but no preceding user prompt!")
            }
        }
    }

    thisLogger().debug("findChat analysis finished. Extracted ${chats.size} chat pairs.")
    return@withContext chats
}


data class Prompt(
    val user: String,
    val copilot: String,
    val vote: String
) {
    override fun toString(): String {
        return "$user\n$copilot\n$vote"
    }
}


fun String.toFile() = File(this)
