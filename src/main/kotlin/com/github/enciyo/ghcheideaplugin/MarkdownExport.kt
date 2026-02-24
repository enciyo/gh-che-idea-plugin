package com.github.enciyo.ghcheideaplugin

import com.github.enciyo.ghcheideaplugin.service.AppSettingsService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.application
import java.io.File


class MarkdownExport(
    private val project: Project
) {

    private val workingDirectory
        get() = project.basePath.orEmpty()

    private val service = application.service<AppSettingsService>()

    init {

    }


    private val branchName get() = normalizeFileName(service.state.fileName.orEmpty())

    fun export(chats: List<Prompt>) {
        if (chats.isEmpty()) {
            thisLogger().warn("No chats found to export. Skipping file creation.")
            return
        }
        try {
            val file = createMdFile()
            thisLogger().info("Exporting ${chats.size} chats to ${file.absolutePath}")

            val content = buildString {
                append(header(branchName))
                chats.forEach {
                    append("\n")
                    append(template(it))
                }
            }

            file.writeText(content)
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            thisLogger().info("Successfully exported ${chats.size} chats to ${file.absolutePath}")
        } catch (e: Exception) {
            thisLogger().error("Failed to export chats: ${e.message}", e)
        }
    }

    private fun normalizeFileName(fileName: String): String {
        val removeChars = listOf(" ", "/", "\\", ":", "*", "?", "\"", "<", ">", "|")
        return removeChars.fold(fileName) { acc, c -> acc.replace(c, "") }
    }

    private fun createMdFile(): File {
        makeDirectory()
        val exportPath = service.state.exportPath.orEmpty().removePrefix("/")
        val file = File(workingDirectory, "$exportPath/$branchName.md")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
    }


    private fun makeDirectory() {
        val exportPath = service.state.exportPath.orEmpty().removePrefix("/")
        val directory = File(workingDirectory, exportPath)
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
