package com.github.enciyo.ghcheideaplugin

import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.ui.LanguageTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Component
import java.awt.Container
import java.io.File
import javax.swing.text.JTextComponent

private const val USER_MESSAGE_COMPONENT = "UserMessageComponent"
private const val COPILOT_MESSAGE_COMPONENT = "CopilotMessageComponent"
private const val MESSAGE_CONTENT_PANEL = "MessageContentPanel"
private const val MyEditorTextField = "MyEditorTextField"
private const val TOGGLE_BUTTONS = "MutuallyExclusiveToggleActionButtonGroup"

suspend fun Container.findComponentsByClassName(className: String): List<Component> = withContext(Dispatchers.Default) {
    val components = mutableListOf<Component>()
    for (component in this@findComponentsByClassName.components) {
        if (component.javaClass.simpleName == className) {
            components.add(component)
        }
        if (component is Container) {
            val childComponents = component.findComponentsByClassName(className)
            components.addAll(childComponents)
        }
    }
    return@withContext components
}


fun printBeautyContainerTree(container: Container, level: Int = 0) {
    for (i in 0 until level) {
        print("  ")
    }
    println(container.javaClass.simpleName)
    for (component in container.components) {
        if (component is Container) {
            printBeautyContainerTree(component, level + 1)
        }
    }
}

fun Component.asContainer(): Container {
    return this as Container
}

fun List<Component>.asContainer(): List<Container> {
    return this.map { it as Container }
}


suspend fun Container.findText(): String {
    var message = ""
    findComponentsByClassName(MESSAGE_CONTENT_PANEL)
        .asContainer()
        .onEach {
            it.components.forEach {
                when (it::class.qualifiedName) {
                    "com.github.copilot.chat.message.HtmlContentComponent" -> {
                        (it as? JTextComponent?)?.let {
                            val wrappedData = it.text.lines().joinToString("")
                            message += "${wrappedData}\n"
                        }
                    }
                    "com.github.copilot.chat.message.codeblock.CodeBlockContainer" -> {
                        (it.asContainer().findComponentsByClassName(MyEditorTextField)
                            .firstOrNull() as? LanguageTextField)?.let {
                            message += "\n```" + it.text + "\n```\n\n"
                        }
                    }
                }
            }
        }

    return message
}


suspend fun Container.findVote(): String {
    val votes = findComponentsByClassName(TOGGLE_BUTTONS)
        .first()
        .asContainer()
        .findComponentsByClassName(ActionButton::class.java.simpleName)
        .map { it as ActionButton }

    return when {
        votes.first().isSelected -> "***"
        votes.last().isSelected -> "*"
        else -> "**"
    }
}


suspend fun Container.findChat(): List<Prompt> = withContext(Dispatchers.Default){
    val users = findComponentsByClassName(USER_MESSAGE_COMPONENT)
    val copilots = findComponentsByClassName(COPILOT_MESSAGE_COMPONENT)
    val chats = mutableListOf<Prompt>()


    users.forEachIndexed { index, component ->
        val prompt = component.asContainer().findText()
        val answer = copilots[index].asContainer().findText()
        val vote = copilots[index].asContainer().findVote()
        chats.add(
            Prompt(
                user = prompt,
                copilot = answer,
                vote = vote
            )
        )
    }
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
