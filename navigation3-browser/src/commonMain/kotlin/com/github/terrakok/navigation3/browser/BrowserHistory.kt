package com.github.terrakok.navigation3.browser

import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
suspend fun <T> bindBackStackToBrowserHistory(
    backStack: SnapshotStateList<T>,
    saveItem: (key: T) -> String?,
    restoreItem: (fragment: String) -> T?
) {
    val firstBind = BrowserHistoryIsInUse.compareAndSet(expectedValue = false, newValue = true)
    if (!firstBind) {
        val window = refBrowserWindow()
        window.console.warn("BrowserHistory has already been bound to another backstack!")
        return
    }
    coroutineScope {
        val window = refBrowserWindow()
        //listen browser navigation events and restore the backstack
        launch {
            window.popStateEvents()
                .map { it.state }
                .onStart { emit(window.history.state) } // after app start we need initial state
                .collect { state ->
                    if (state == null) {
                        // if user manually put a new address, then there is no state
                        // we try to navigate to the url fragment
                        restoreItem(window.location.hash)?.let { new ->
                            backStack.add(new)
                        } ?: run {
                            window.console.warn("Unable to parse url fragment: `${window.location.hash}`")
                        }
                    } else {
                        // navigation happened by the browser buttons
                        try {
                            val restoredBackStack = state.lines().map {
                                restoreItem(it) ?: error("Unable to restore item: `$it`")
                            }
                            backStack.clear()
                            backStack.addAll(restoredBackStack)
                        } catch (e: Exception) {
                            window.console.warn(e.message ?: "Unknown error")
                            window.console.warn("Unable to restore back stack from history: `$state`")
                        }
                    }
                }
        }

        //listen backstack's changes and update the browser history
        launch {
            snapshotFlow { backStack.toList() }.collect { keys ->
                val currentStack = keys.mapNotNull { saveItem(it) }
                if (currentStack.isEmpty()) return@collect

                val currentDestination = currentStack.last()
                val currentStackString = currentStack.joinToString("\n")

                val appAddress = with(window.location) { origin + pathname }
                val currentBrowserHistoryState = window.history.state

                when (currentBrowserHistoryState) {
                    // if the browser history state is null or equal the app state,
                    // the callback came from the popStateEvents
                    // we need to save the current state in the browser history and to update shown uri
                    null, currentStackString -> {
                        window.history.replaceState(
                            currentStackString,
                            "",
                            appAddress + currentDestination
                        )
                    }

                    // the navigation happened in the compose app,
                    // we need to push the new state to the browser history
                    else -> {
                        window.history.pushState(
                            currentStackString,
                            "",
                            appAddress + currentDestination
                        )
                    }
                }
            }
        }
    }
}
