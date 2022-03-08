package com.test.watermark.features.main.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import com.test.watermark.extensions.showToast
import com.test.watermark.extensions.systemCameraManager

interface MainCamera {
    fun stop()
    fun startPreview(surfaceTexture: SurfaceTexture?, width: Int, height: Int)
}

class MainCameraImpl(
    private val context: Context
) : MainCamera {

    private var cameraManager: CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraHandler: Handler? = null

    private val cameraHandlerThread = HandlerThread("MainCamera")

    init {
        cameraHandlerThread.start()
        cameraHandler = Handler(cameraHandlerThread.looper)
        setUp()
    }

    override fun startPreview(surfaceTexture: SurfaceTexture?, width: Int, height: Int) = runOnCameraThread {
            try {
                val previewRequestBuilder: CaptureRequest.Builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)!!
                surfaceTexture?.setDefaultBufferSize(height, width)
                val previewSurface = Surface(surfaceTexture)
                previewRequestBuilder.addTarget(previewSurface)
                @Suppress("DEPRECATION")
                cameraDevice?.createCaptureSession(
                    listOf(previewSurface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            try {
                                previewRequestBuilder.set(
                                    CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                                )
                                val previewRequest = previewRequestBuilder.build()
                                session.setRepeatingRequest(
                                    previewRequest,
                                    null,
                                    cameraHandler
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            context.showToast("camera configure failed")
                        }
                    }, cameraHandler
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

    override fun stop() {
        cameraDevice?.close()
        cameraHandler = null
        cameraDevice = null
        cameraManager = null
        cameraHandlerThread.quitSafely()
    }

    private fun setUp() = runOnCameraThread {
        cameraManager = context.systemCameraManager
        cameraManager?.let {
            val allIds: Array<String> = it.cameraIdList
            var params: CameraCharacteristics?
            for (id in allIds) {
                params = it.getCameraCharacteristics(id)
                if (params.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    openCamera(id)
                    break
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(id: String) = runOnCameraThread {
        cameraManager?.openCamera(id, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
            }

            override fun onDisconnected(camera: CameraDevice) {
                cameraDevice?.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
            }
        }, null)
    }

    private fun runOnCameraThread(r: Runnable) {
        cameraHandler?.post(r)
    }
}
