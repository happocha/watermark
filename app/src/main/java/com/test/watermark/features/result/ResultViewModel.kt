package com.test.watermark.features.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.terrakok.cicerone.Router
import com.test.watermark.common.SingleLiveEvent

interface ResultViewModel {
    val playVideo: LiveData<Unit>

    fun onBackPressed()
    fun init()
}

class ResultViewModelImpl(
    private val router: Router
) : ResultViewModel, ViewModel() {

    override val playVideo = SingleLiveEvent<Unit>()

    override fun onBackPressed() {
        router.exit()
    }

    override fun init() {
        playVideo.call()
    }
}
