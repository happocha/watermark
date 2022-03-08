package com.test.watermark.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.test.watermark.App
import com.test.watermark.common.ResourceProvider
import com.test.watermark.common.ResourceProviderImpl

fun App.provideCicerone(): Cicerone<Router> = Cicerone.create()

fun App.provideRouter(): Router = cicerone.router

fun App.provideNavigatorHolder(): NavigatorHolder = cicerone.getNavigatorHolder()

fun App.provideResource(): ResourceProvider = ResourceProviderImpl(this)
