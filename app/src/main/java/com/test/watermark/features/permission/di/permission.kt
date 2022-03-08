package com.test.watermark.features.permission.di

import androidx.lifecycle.ViewModelProvider
import com.test.watermark.common.ViewModelFactory
import com.test.watermark.extensions.app
import com.test.watermark.features.permission.PermissionFragment
import com.test.watermark.features.permission.PermissionViewModel
import com.test.watermark.features.permission.PermissionViewModelImpl

fun PermissionFragment.providePermissionViewModel(): PermissionViewModel =
    ViewModelProvider(
        this,
        ViewModelFactory {
            PermissionViewModelImpl(
                router = app.router,
                resourceProvider = app.resourceProvider
            )
        }
    )[PermissionViewModelImpl::class.java]