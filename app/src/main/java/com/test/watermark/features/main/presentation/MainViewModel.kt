package com.test.watermark.features.main.presentation

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.terrakok.cicerone.Router
import com.test.watermark.common.SingleLiveEvent
import com.test.watermark.features.result.ResultScreen

interface MainViewModel {
    val recordingState: LiveData<MainScreenState>
    val recordingText: LiveData<Boolean>

    fun onBackPressed()
    fun onRecordButtonPressed()
    fun onRecordButtonReleased()
}

class MainViewModelImpl(
    private val router: Router
) : MainViewModel, ViewModel() {

    override val recordingState = SingleLiveEvent<MainScreenState>()
    override val recordingText = SingleLiveEvent<Boolean>()

    override fun onBackPressed() {
        router.exit()
    }

    override fun onRecordButtonPressed() {
        recordingState.value = MainScreenState.StartRecording
        recordingText.value = true
    }

    override fun onRecordButtonReleased() {
        recordingState.value = MainScreenState.StopRecording
        recordingText.value = false
        Handler(Looper.getMainLooper()).postDelayed({
            router.navigateTo(ResultScreen.result())
        }, 300)
    }
}
