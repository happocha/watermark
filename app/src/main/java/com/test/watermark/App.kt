package com.test.watermark

import android.app.Application
import com.test.watermark.di.provideCicerone
import com.test.watermark.di.provideNavigatorHolder
import com.test.watermark.di.provideResource
import com.test.watermark.di.provideRouter

class App : Application() {
    val resourceProvider by lazy(::provideResource)
    val cicerone by lazy(::provideCicerone)
    val router by lazy(::provideRouter)
    val navigatorHolder by lazy(::provideNavigatorHolder)
}
