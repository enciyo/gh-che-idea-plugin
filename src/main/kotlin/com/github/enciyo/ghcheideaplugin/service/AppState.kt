package com.github.enciyo.ghcheideaplugin.service

import com.intellij.openapi.components.BaseState

class AppState : BaseState() {
    var author by string("")
    var fileName by string()
    var regex by string()
    var useRegex by property(false)


}

