package com.github.terrakok.navigation3.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Binds a Compose `backStack` to the browser history for chronological (web‑style) navigation.
 *
 * This helper keeps the browser's URL hash and history in sync with your in‑app back stack:
 * - When the user navigates using the browser's Back/Forward buttons, the back stack is restored
 *   from the previously saved browser history state or, if absent, from the current URL hash.
 * - When the in‑app back stack changes, the function updates the browser history and the URL hash.
 *
 * Usage example:
 *
 * ```kotlin
 * ChronologicalBrowserNavigation(
 *     backStack = backStack,
 *     saveKey = { key ->
 *         when (key) {
 *             is Root -> buildBrowserHistoryFragment("root")
 *             is Profile -> buildBrowserHistoryFragment("profile", mapOf("id" to key.id.toString()))
 *             else -> null
 *         }
 *     },
 *     restoreKey = { fragment ->
 *         when (getBrowserHistoryFragmentName(fragment)) {
 *             "root" -> Root
 *             "profile" -> Profile(getBrowserHistoryFragmentParameters(fragment).getValue("id")!!.toInt())
 *             else -> null
 *         }
 *     }
 * )
 * ```
 *
 * Notes and constraints:
 * - Only one type of Browser History can be used at a time within a process. If called more than once, a
 *   warning is logged to `window.console` and the subsequent calls are ignored.
 * - `saveKey` is used to serialize an item to a URL fragment (e.g. `#profile?id=42`). Return `null`
 *   to skip an item; skipped items are not saved to browser history.
 * - `restoreKey` must perform the inverse operation and return the back stack item for a given
 *   fragment string. Return `null` to indicate that the fragment cannot be restored.
 *
 * @param backStack The Compose back stack to observe and mutate.
 * @param saveKey Converts a back stack item into a URL fragment to be stored in browser history.
 * @param restoreKey Restores a back stack item from a URL fragment.
 */
@Composable
fun <T> ChronologicalBrowserNavigation(
    backStack: SnapshotStateList<T>,
    saveKey: (key: T) -> String?,
    restoreKey: (fragment: String) -> T?
) {
    LaunchedEffect(Unit) {
        bindBackStackToBrowserHistory(backStack, saveKey, restoreKey)
    }
}

/**
 * Binds a Compose `backStack` to the browser history for chronological (web‑style) navigation.
 *
 * This helper keeps the browser's URL hash and history in sync with your in‑app back stack:
 * - When the user navigates using the browser's Back/Forward buttons, the back stack is restored
 *   from the previously saved browser history state or, if absent, from the current URL hash.
 * - When the in‑app back stack changes, the function updates the browser history and the URL hash.
 *
 * Usage example:
 *
 * ```kotlin
 * ChronologicalBrowserNavigation(
 *     backStack = backStack,
 *     saveKey = { key ->
 *         when (key) {
 *             is Root -> buildBrowserHistoryFragment("root")
 *             is Profile -> buildBrowserHistoryFragment("profile", mapOf("id" to key.id.toString()))
 *             else -> null
 *         }
 *     },
 *     restoreKey = { fragment ->
 *         when (getBrowserHistoryFragmentName(fragment)) {
 *             "root" -> Root
 *             "profile" -> Profile(getBrowserHistoryFragmentParameters(fragment).getValue("id")!!.toInt())
 *             else -> null
 *         }
 *     }
 * )
 * ```
 *
 * Notes and constraints:
 * - Only one type of Browser History can be used at a time within a process. If called more than once, a
 *   warning is logged to `window.console` and the subsequent calls are ignored.
 * - `saveKey` is used to serialize an item to a URL fragment (e.g. `#profile?id=42`). Return `null`
 *   to skip an item; skipped items are not saved to browser history.
 * - `restoreKey` must perform the inverse operation and return the back stack item for a given
 *   fragment string. Return `null` to indicate that the fragment cannot be restored.
 *
 *
 * @param backStack The Compose back stack to observe and mutate.
 * @param saveKey Converts a back stack item into a URL fragment to be stored in browser history.
 * @param restoreKey Restores a back stack item from a URL fragment.
 */
@Composable
fun <T : NavKey> ChronologicalBrowserNavigation(
    backStack: NavBackStack<T>,
    saveKey: (key: T) -> String?,
    restoreKey: (fragment: String) -> T?,
) {
    LaunchedEffect(Unit) {
        bindBackStackToBrowserHistory(backStack, saveKey, restoreKey)
    }
}

/**
 * Binds a Compose `backStack` to the browser history for chronological (web‑style) navigation
 * using full URL routes (path and query).
 *
 * This helper keeps the browser's URL and history in sync with your in‑app back stack:
 * - When the user navigates using the browser's Back/Forward buttons, the back stack is restored
 *   from the previously saved browser history state or, if absent, from the current URL path.
 * - When the in‑app back stack changes, the function updates the browser history and the URL path.
 *
 * Usage example:
 *
 * ```kotlin
 * ChronologicalBrowserRouteNavigation(
 *     backStack = backStack,
 *     saveKey = { key ->
 *         when (key) {
 *             is Root -> buildBrowserHistoryRoute("/root")
 *             is Profile -> buildBrowserHistoryRoute("/profile/{id}", mapOf("id" to key.id.toString()))
 *             else -> null
 *         }
 *     },
 *     restoreKey = { route ->
 *         when {
 *             browserHistoryRouteMatches("/root", route) -> Root
 *             matchBrowserHistoryRoute("/profile/{id}", route) != null -> {
 *                 val params = getBrowserHistoryRouteParameters("/profile/{id}", route)
 *                 Profile(params.getValue("id")!!.toInt())
 *             }
 *             else -> null
 *         }
 *     }
 * )
 * ```
 *
 * Alternative usage with `RouteMatcher`:
 *
 * ```kotlin
 * ChronologicalBrowserRouteNavigation(
 *     backStack = backStack,
 *     saveKey = { /* ... */ },
 *     restoreKey = { route ->
 *         RouteMatcher.match(route) {
 *             pattern("/root") { Root }
 *             pattern("/profile/{id}") { params ->
 *                 Profile(params.getValue("id")!!.toInt())
 *             }
 *         }
 *     }
 * )
 * ```
 *
 * Notes and constraints:
 * - Only one type of Browser History can be used at a time within a process. If called more than once, a
 *   warning is logged to `window.console` and the subsequent calls are ignored.
 * - `saveKey` is used to serialize an item to a URL path (e.g. `/profile/42`). Return `null`
 *   to skip an item; skipped items are not saved to browser history.
 * - `restoreKey` must perform the inverse operation and return the back stack item for a given
 *   route string. Return `null` to indicate that the route cannot be restored.
 *
 * @param backStack The Compose back stack to observe and mutate.
 * @param saveKey Converts a back stack item into a URL route to be stored in browser history.
 * @param restoreKey Restores a back stack item from a URL route.
 */
@Composable
fun <T> ChronologicalBrowserRouteNavigation(
    backStack: SnapshotStateList<T>,
    saveKey: (key: T) -> String?,
    restoreKey: (route: String) -> T?
) {
    LaunchedEffect(Unit) {
        bindBackStackToBrowserHistory(backStack, saveKey, restoreKey, isRouteBased = true)
    }
}

/**
 * Binds a Compose `backStack` to the browser history for chronological (web‑style) navigation
 * using full URL routes (path and query).
 *
 * This helper keeps the browser's URL and history in sync with your in‑app back stack:
 * - When the user navigates using the browser's Back/Forward buttons, the back stack is restored
 *   from the previously saved browser history state or, if absent, from the current URL path.
 * - When the in‑app back stack changes, the function updates the browser history and the URL path.
 *
 * Usage example:
 *
 * ```kotlin
 * ChronologicalBrowserRouteNavigation(
 *     backStack = backStack,
 *     saveKey = { key ->
 *         when (key) {
 *             is Root -> buildBrowserHistoryRoute("/root")
 *             is Profile -> buildBrowserHistoryRoute("/profile/{id}", mapOf("id" to key.id.toString()))
 *             else -> null
 *         }
 *     },
 *     restoreKey = { route ->
 *         when {
 *             browserHistoryRouteMatches("/root", route) -> Root
 *             matchBrowserHistoryRoute("/profile/{id}", route) != null -> {
 *                 val params = getBrowserHistoryRouteParameters("/profile/{id}", route)
 *                 Profile(params.getValue("id")!!.toInt())
 *             }
 *             else -> null
 *         }
 *     }
 * )
 * ```
 *
 * Alternative usage with `RouteMatcher`:
 *
 * ```kotlin
 * ChronologicalBrowserRouteNavigation(
 *     backStack = backStack,
 *     saveKey = { /* ... */ },
 *     restoreKey = { route ->
 *         RouteMatcher.match(route) {
 *             pattern("/root") { Root }
 *             pattern("/profile/{id}") { params ->
 *                 Profile(params.getValue("id")!!.toInt())
 *             }
 *         }
 *     }
 * )
 * ```
 *
 * Notes and constraints:
 * - Only one type of Browser History can be used at a time within a process. If called more than once, a
 *   warning is logged to `window.console` and the subsequent calls are ignored.
 * - `saveKey` is used to serialize an item to a URL path (e.g. `/profile/42`). Return `null`
 *   to skip an item; skipped items are not saved to browser history.
 * - `restoreKey` must perform the inverse operation and return the back stack item for a given
 *   route string. Return `null` to indicate that the route cannot be restored.
 *
 * @param backStack The Compose back stack to observe and mutate.
 * @param saveKey Converts a back stack item into a URL route to be stored in browser history.
 * @param restoreKey Restores a back stack item from a URL route.
 */
@Composable
fun <T : NavKey> ChronologicalBrowserRouteNavigation(
    backStack: NavBackStack<T>,
    saveKey: (key: T) -> String?,
    restoreKey: (route: String) -> T?,
) {
    LaunchedEffect(Unit) {
        bindBackStackToBrowserHistory(backStack, saveKey, restoreKey, isRouteBased = true)
    }
}

@OptIn(ExperimentalAtomicApi::class)
private suspend fun <T> bindBackStackToBrowserHistory(
    backStack: MutableList<T>,
    saveKey: (key: T) -> String?,
    restoreKey: (fragment: String) -> T?,
    isRouteBased: Boolean = false,
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
                        // we try to navigate to the url fragment or path + search
                        restoreKey(
                            if (isRouteBased) window.location.pathname + window.location.search
                            else window.location.hash
                        )?.let { new ->
                            backStack.add(new)
                        } ?: run {
                            val urlPart =
                                if (isRouteBased) window.location.pathname + window.location.search
                                else window.location.hash
                            window.console.warn("Unable to parse url: `$urlPart`")
                        }
                    } else {
                        // navigation happened by the browser buttons
                        try {
                            val restoredBackStack = state.lines().map {
                                restoreKey(it) ?: error("Unable to restore item: `$it`")
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
                val currentStack = keys.mapNotNull { saveKey(it) }
                if (currentStack.isEmpty()) return@collect

                val currentDestination = currentStack.last()
                val currentStackString = currentStack.joinToString("\n")

                val appAddress = with(window.location) {
                    if (isRouteBased) origin else origin + pathname
                }
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
