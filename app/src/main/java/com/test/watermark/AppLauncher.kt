package com.test.watermark

import com.github.terrakok.cicerone.Router
import com.test.watermark.features.permission.PermissionScreen

interface AppLauncher {
    fun coldStart()
}

class AppLauncherImpl(
    private val router: Router,
) : AppLauncher {

    override fun coldStart() {
        router.newRootScreen(PermissionScreen.permission())
    }
}
