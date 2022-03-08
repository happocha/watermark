package com.test.watermark.features.main.di

import androidx.lifecycle.ViewModelProvider
import com.test.watermark.common.ViewModelFactory
import com.test.watermark.extensions.app
import com.test.watermark.features.main.presentation.MainFragment
import com.test.watermark.features.main.presentation.MainViewModel
import com.test.watermark.features.main.presentation.MainViewModelImpl

fun MainFragment.provideMainViewModel(): MainViewModel =
    ViewModelProvider(
        this,
        ViewModelFactory {
            MainViewModelImpl(
                router = app.router
            )
        }
    )[MainViewModelImpl::class.java]
