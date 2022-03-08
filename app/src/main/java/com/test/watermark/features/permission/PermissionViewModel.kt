package com.test.watermark.features.permission

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.terrakok.cicerone.Router
import com.test.watermark.R
import com.test.watermark.common.ResourceProvider
import com.test.watermark.common.SingleLiveEvent
import com.test.watermark.features.main.MainScreen

interface PermissionViewModel {
    val showDialogMessage: LiveData<String>
    val setupCamera: LiveData<Unit>

    fun onBackPressed()
    fun permissionUpdated(permission: Boolean)
}

class PermissionViewModelImpl(
    private val router: Router,
    private val resourceProvider: ResourceProvider
) : PermissionViewModel, ViewModel() {

    override val showDialogMessage = SingleLiveEvent<String>()
    override val setupCamera = SingleLiveEvent<Unit>()

    override fun onBackPressed() {
        router.exit()
    }

    override fun permissionUpdated(permission: Boolean) {
        if (permission.not()) {
            showDialogMessage.value = resourceProvider.getString(R.string.error_camera_permission)
        } else {
            router.newRootScreen(MainScreen.main())
        }
    }
}
