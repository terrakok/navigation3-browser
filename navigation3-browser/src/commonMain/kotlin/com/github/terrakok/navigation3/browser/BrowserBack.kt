package com.github.terrakok.navigation3.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventHistory
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Configures browser Back button behavior for hierarchical (app‑style) navigation.
 *
 * Use this composable when your app navigation is hierarchical (like classic Android apps),
 * where pressing the browser Back button should pop the in‑app back stack rather than
 * traversing a chronological history.
 *
 * Usage example:
 *
 * ```kotlin
 * ConfigureBrowserBack(
 *     currentDestinationName = {
 *         when (val key = backStack.lastOrNull()) {
 *             is Root -> buildBrowserHistoryFragment("root")
 *             is Profile -> buildBrowserHistoryFragment("profile", mapOf("id" to key.id.toString()))
 *             else -> null
 *         }
 *     }
 * )
 * ```
 *
 * Notes and constraints:
 * - Only one type of Browser History can be used at a time within a process. If called more than once, a
 *   warning is logged to `window.console` and the subsequent calls are ignored.
 * - Provide `currentDestinationName` that returns a URL hash fragment for the current destination
 *   (use [buildBrowserHistoryFragment]).
 *
 * @param currentDestinationName A lambda that returns the current destination as a URL fragment
 * (including leading `#`), or `null` if none. See [buildBrowserHistoryFragment].
 */
@Composable
fun ConfigureBrowserBack(
    currentDestinationName: () -> String?,
) {
    val navigationEventDispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
        ?: error("NavigationEventDispatcher not found.")
    val input = remember { DirectNavigationEventInput() }
    DisposableEffect(navigationEventDispatcher) {
        navigationEventDispatcher.addInput(input)
        onDispose { navigationEventDispatcher.removeInput(input) }
    }

    LaunchedEffect(Unit) {
        configureBrowserBack(
            currentDestinationName = currentDestinationName,
            history = navigationEventDispatcher.history,
            onBack = { input.backCompleted() }
        )
    }
}

private const val ROOT_ENTRY = "compose_root_entry"
private const val CURRENT_ENTRY = "compose_current_entry"

@OptIn(ExperimentalAtomicApi::class)
private suspend fun configureBrowserBack(
    currentDestinationName: () -> String?,
    history: StateFlow<NavigationEventHistory>,
    onBack: () -> Unit,
) {
    val firstBind = BrowserHistoryIsInUse.compareAndSet(expectedValue = false, newValue = true)
    if (!firstBind) {
        val window = refBrowserWindow()
        window.console.warn("BrowserHistory has already been bound to another backstack!")
        return
    }
    coroutineScope {
        val window = refBrowserWindow()
        val appAddress = with(window.location) { origin + pathname }
        val rootDestination = currentDestinationName().orEmpty()
        window.history.replaceState(ROOT_ENTRY, "", appAddress + rootDestination)

        //listen browser navigation events
        launch {
            window.popStateEvents()
                .map { it.state }
                .collect { state ->
                    if (state == ROOT_ENTRY) {
                        if (history.value.currentIndex > 0) {
                            onBack()
                        } else {
                            window.history.go(-1)
                        }
                    } else {
                        window.history.replaceState(ROOT_ENTRY, "", appAddress + currentDestinationName().orEmpty())
                    }
                }
        }

        //listen backstack's changes
        launch {
            history
                .drop(1) //ignore init state
                .collect {
                    val newUrl = appAddress + currentDestinationName().orEmpty()
                    if (window.history.state == ROOT_ENTRY) {
                        // it was browser navigation
                        window.history.pushState(CURRENT_ENTRY, "", newUrl)
                    } else {
                        // it was compose navigation
                        window.history.replaceState(CURRENT_ENTRY, "", newUrl)
                    }
                }
        }
    }
}
