package com.github.enciyo.ghcheideaplugin

import com.intellij.openapi.project.Project


class Github(
    private val project: Project
) {

    private val workingDirectory
        get() = project.basePath.orEmpty()


    fun getCurrentBranchName(): String {
        return try {
            val process = ProcessBuilder("git", "branch", "--show-current")
                .directory(workingDirectory.toFile())
                .start()
            val reader = process.inputStream.bufferedReader()
            reader.readLine() ?: "master"
        } catch (e: Exception) {
            "master"
        }
    }

    fun getCurrentConfigName(): String {
        return try {
            val process = ProcessBuilder("git", "config", "--get", "user.name")
                .directory(workingDirectory.toFile())
                .start()
            val reader = process.inputStream.bufferedReader()
            reader.readLine() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }


}
