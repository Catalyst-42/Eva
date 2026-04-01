package com.grace.eva

import androidx.compose.ui.window.ComposeUIViewController
import com.grace.eva.di.IosRootController
import com.grace.eva.di.createAppContainer

@Suppress("UNUSED") // Used in iOS
fun mainViewController() = ComposeUIViewController {
    val appContainer = createAppContainer()
    App(appContainer)
}.apply {
    IosRootController.rootViewController = this
}