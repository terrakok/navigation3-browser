package com.github.terrakok.navigation3.browser

@OptIn(ExperimentalWasmJsInterop::class)
internal actual fun refBrowserWindow(): BrowserWindow = js("window")