package com.test.watermark.features.result

import androidx.lifecycle.ViewModel
import com.github.terrakok.cicerone.Router

interface ResultViewModel {
    fun onBackPressed()
}

class ResultViewModelImpl(
    private val router: Router
) : ResultViewModel, ViewModel() {

    override fun onBackPressed() {
        router.exit()
    }
}
