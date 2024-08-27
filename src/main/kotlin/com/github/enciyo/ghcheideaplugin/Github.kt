package com.github.enciyo.ghcheideaplugin

import com.intellij.openapi.project.Project


class Github(
    private val project: Project
) {

    private val workingDirectory
        get() = project.basePath.orEmpty()


    fun getCurrentBranchName(): String {
        val process = ProcessBuilder("git", "branch", "--show-current")
            .directory(workingDirectory.toFile())
            .start()
        val reader = process.inputStream.bufferedReader()
        return reader.readLine()
    }

    fun getCurrentConfigName(): String {
        val process = ProcessBuilder("git", "config", "--get", "user.name")
            .directory(workingDirectory.toFile())
            .start()
        val reader = process.inputStream.bufferedReader()
        return reader.readLine()
    }


}