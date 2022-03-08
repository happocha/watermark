package com.test.watermark.features.result.di

import androidx.lifecycle.ViewModelProvider
import com.test.watermark.common.ViewModelFactory
import com.test.watermark.extensions.app
import com.test.watermark.features.result.ResultFragment
import com.test.watermark.features.result.ResultViewModel
import com.test.watermark.features.result.ResultViewModelImpl

fun ResultFragment.provideResultViewModel(): ResultViewModel =
    ViewModelProvider(
        this,
        ViewModelFactory {
            ResultViewModelImpl(
                router = app.router
            )
        }
    )[ResultViewModelImpl::class.java]
