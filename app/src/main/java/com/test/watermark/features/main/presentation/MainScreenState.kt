package com.test.watermark.features.main.presentation

sealed class MainScreenState {
    object StartRecording : MainScreenState()
    object StopRecording : MainScreenState()
    object Recording : MainScreenState()
    object WaitRecording : MainScreenState()
}
