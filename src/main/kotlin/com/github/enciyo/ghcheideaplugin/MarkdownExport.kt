package com.github.enciyo.ghcheideaplugin

import com.github.enciyo.ghcheideaplugin.service.AppSettingsService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.util.application
import java.io.File


class MarkdownExport(
    private val project: Project
) {

    companion object {
        private const val DIRECTORY = "/ai/copilot/prompts"
    }

    private val workingDirectory
        get() = project.basePath.orEmpty()

    private val service = application.service<AppSettingsService>()

    init {
        println("Working Directory: $workingDirectory")

    }


    private val branchName get() = normalizeFileName(service.state.fileName.orEmpty())

    fun export(chats: List<Prompt>) {
        val file = createMdFile()
        thisLogger().warn("Exporting ${chats.size} chats")
        thisLogger().warn("Exporting to $file")
        file.appendText(header(branchName))
        chats.forEach {
            file.appendText("\n")
            file.appendText(template(it))
        }
        thisLogger().warn("Exported ${chats.size} chats")
    }

    private fun normalizeFileName(fileName: String): String {
        val removeChars = listOf(" ", "/", "\\", ":", "*", "?", "\"", "<", ">", "|")
        return removeChars.fold(fileName) { acc, c -> acc.replace(c, "") }
    }

    private fun createMdFile(): File {
        makeDirectory()
        val file = File(workingDirectory, "$DIRECTORY/$branchName.md")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
    }


    private fun makeDirectory() {
        val directory = File(workingDirectory, DIRECTORY)
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }


    private fun header(header: String) = "#$header\n\n"

    private fun template(chat: Prompt) = """
#### Author
${application.service<AppSettingsService>().state.author}
#### Prompt
${chat.user}
#### Answer ${chat.vote}
${chat.copilot}

---
""".trimIndent()


}