package com.github.terrakok.navigation3.browser

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal external interface BrowserLocation {
    val origin: String
    val pathname: String
    val hash: String
    val search: String
}

internal external interface BrowserHistory {
    val state: String?
    fun pushState(data: String?, title: String, url: String?)
    fun replaceState(data: String?, title: String, url: String?)
    fun go(delta: Int)
}

internal external interface BrowserEvent
internal external interface BrowserPopStateEvent : BrowserEvent {
    val state: String?
}

internal external interface BrowserEventTarget {
    fun addEventListener(type: String, callback: ((BrowserEvent) -> Unit)?)
    fun removeEventListener(type: String, callback: ((BrowserEvent) -> Unit)?)
}

internal external interface BrowserWindow : BrowserEventTarget {
    val location: BrowserLocation
    val history: BrowserHistory
    val console: BrowserConsole
}

internal external interface BrowserConsole {
    fun warn(msg: String)
}

internal external fun decodeURIComponent(str: String): String
internal external fun encodeURIComponent(str: String): String

internal expect fun refBrowserWindow(): BrowserWindow

@OptIn(DelicateCoroutinesApi::class)
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal fun BrowserWindow.popStateEvents(): Flow<BrowserPopStateEvent> = callbackFlow {
    val localWindow = this@popStateEvents
    val callback: (BrowserEvent) -> Unit = { event: BrowserEvent ->
        if (!isClosedForSend) {
            (event as? BrowserPopStateEvent)?.let { trySend(it) }
        }
    }

    localWindow.addEventListener("popstate", callback)
    awaitClose {
        localWindow.removeEventListener("popstate", callback)
    }
}