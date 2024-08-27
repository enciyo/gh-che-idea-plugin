package com.github.enciyo.ghcheideaplugin.listener

import com.github.enciyo.ghcheideaplugin.service.AppSettingsService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.BranchChangeListener
import com.intellij.util.application


@Service(Service.Level.PROJECT)
class AppBranchChangeListener(project: Project) : BranchChangeListener {

    private val service = application.service<AppSettingsService>()

    init {
        val messageBusConnection = project.messageBus.connect()
        messageBusConnection.subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, this)
    }

    override fun branchHasChanged(p0: String) {
        service.onChangedBranch(p0)
    }

    override fun branchWillChange(p0: String) {

    }

}

