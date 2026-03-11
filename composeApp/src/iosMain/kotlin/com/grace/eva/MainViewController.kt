package com.grace.eva

import androidx.compose.ui.window.ComposeUIViewController
import com.grace.eva.di.createAppContainer

fun MainViewController() = ComposeUIViewController {
    val appContainer = createAppContainer()
    App(appContainer)
}