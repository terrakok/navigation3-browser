package sample.app

import kotlinx.browser.window
import org.w3c.dom.AddEventListenerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal suspend fun browserBack() {
    window.history.back()
    waitHistoryPopState()
}

internal suspend fun browserForward() {
    window.history.forward()
    waitHistoryPopState()
}

internal suspend fun waitHistoryPopState() = suspendCoroutine { cont ->
    window.addEventListener(
        type = "popstate",
        callback = { cont.resume(Unit) },
        options = AddEventListenerOptions(passive = false, once = true)
    )
}

internal fun urlFragment(): String {
    return window.location.toString().substringAfter("#")
}