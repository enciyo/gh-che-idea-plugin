package com.github.enciyo.ghcheideaplugin.service

import com.github.enciyo.ghcheideaplugin.Github
import com.intellij.configurationStore.setStateAndCloneIfNeeded
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.projectsDataDir

@Service
@State(
    name = "com.github.enciyo.ghcheideaplugin.AppState",
    storages = [com.intellij.openapi.components.Storage(
        StoragePathMacros.WORKSPACE_FILE,
    )],
)
class AppSettingsService : SimplePersistentStateComponent<AppState>(AppState()) {

    var onUpdateState = { state: AppState -> }

    fun onChangedBranch(branchName: String) {
        val regex = state.regex.orEmpty()
        if (state.useRegex && regex.isNotEmpty()) {
            regex.toRegex().matchEntire(branchName)?.let {
                state.fileName = it.value
                return
            }
            state.fileName = "No Match"
        } else {
            state.fileName = branchName
        }
        onUpdateState(state)
    }

    fun initialize(project: Project) {
        val github = Github(project)
        if (state.author.isNullOrEmpty()){
            state.author = github.getCurrentConfigName()
            onUpdateState(state)
        }

        onChangedBranch(github.getCurrentBranchName())
    }




}