package com.github.enciyo.ghcheideaplugin

import java.awt.Component
import java.awt.Container
import javax.swing.text.JTextComponent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.externalSystem.autoimport.AutoImportProjectTracker
import java.io.File

private const val USER_MESSAGE_COMPONENT = "UserMessageComponent"
private const val COPILOT_MESSAGE_COMPONENT = "CopilotMessageComponent"
private const val MESSAGE_CONTENT_PANEL = "MessageContentPanel"
private const val HTML_CONTENT_COMPONENT = "HtmlContentComponent"
private const val TOGGLE_BUTTONS = "MutuallyExclusiveToggleActionButtonGroup"

fun Container.findComponentsByClassName(className: String): List<Component> {
    val components = mutableListOf<Component>()
    for (component in this.components) {
        if (component.javaClass.simpleName == className) {
            components.add(component)
        }
        if (component is Container) {
            val childComponents = component.findComponentsByClassName(className)
            components.addAll(childComponents)
        }
    }
    return components
}

fun Container.printAllChildren() {
    for (component in this.components) {
        println(component.javaClass.name)
        if (component is Container) {
            component.printAllChildren()
        }
    }
}

fun Component.asContainer(): Container {
    return this as Container
}

fun List<Component>.asContainer(): List<Container> {
    return this.map { it as Container }
}


fun Container.findText(): JTextComponent {
    return findComponentsByClassName(MESSAGE_CONTENT_PANEL)
        .asContainer()
        .map {
            it.findComponentsByClassName(HTML_CONTENT_COMPONENT).first() as JTextComponent
        }
        .first()
}

fun Container.findVote(): String {
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


fun Container.findChat(): List<Prompt> {
    val users = findComponentsByClassName(USER_MESSAGE_COMPONENT)
    val copilots = findComponentsByClassName(COPILOT_MESSAGE_COMPONENT)
    val chats = mutableListOf<Prompt>()

    users.forEachIndexed { index, component ->
        val prompt = component.asContainer().findText()
        val answer = copilots[index].asContainer().findText()
        val vote = copilots[index].asContainer().findVote()
        chats.add(
            Prompt(
                user = prompt.text,
                copilot = answer.text,
                vote = vote
            )
        )
    }
    return chats
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
