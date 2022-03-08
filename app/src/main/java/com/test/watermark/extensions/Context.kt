package com.test.watermark.extensions

import android.content.Context
import android.hardware.camera2.CameraManager
import android.widget.Toast

val Context.systemCameraManager: CameraManager
    get() = getAndroidSystemService(Context.CAMERA_SERVICE)

private inline fun <reified T> Context.getAndroidSystemService(name: String) =
    getSystemService(name) as T

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
