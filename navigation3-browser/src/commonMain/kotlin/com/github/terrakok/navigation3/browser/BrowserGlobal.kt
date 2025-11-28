package com.github.terrakok.navigation3.browser

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi


@OptIn(ExperimentalAtomicApi::class)
internal val BrowserHistoryIsInUse = AtomicBoolean(false)

private const val DEBUG = false
internal fun log(msg: String) {
    if (DEBUG) println(msg)
}