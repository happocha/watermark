package com.test.watermark.features.main

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.test.watermark.features.main.presentation.MainFragment

object MainScreen {
    fun main() = FragmentScreen("MainFragment") { MainFragment() }
}
