package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.ui.window.Dialog
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.github.terrakok.navigation3.browser.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@OptIn(ExperimentalComposeUiApi::class)
fun main() = ComposeViewport { App() }

@Serializable
data object Root : NavKey

@Serializable
data class Profile(val id: Int) : NavKey

val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Root::class, Root.serializer())
            subclass(Profile::class, Profile.serializer())
        }
    }
}

enum class Mode {
    None, Chronological, ChronologicalRoute, Hierarchical, HierarchicalRoute
}

internal var defaultMode: Mode = Mode.None

@Composable
fun App() {
    val backStack = rememberNavBackStack(config, Root)

    val (mode, setMode) = remember { mutableStateOf(defaultMode) }
    when (mode) {
        Mode.None -> {
            Dialog(
                onDismissRequest = {}
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.background(Color.White, shape = RoundedCornerShape(16.dp)).padding(16.dp)
                ) {
                    BasicText(
                        text = "Select navigation mode:",
                        style = TextStyle.Default.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    )
                    BasicText(
                        text = "Chronological (classic web) navigation",
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .clickable(onClick = { setMode(Mode.Chronological) })
                            .padding(8.dp)
                    )
                    BasicText(
                        text = "Chronological (classic web) route navigation",
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .clickable(onClick = { setMode(Mode.ChronologicalRoute) })
                            .padding(8.dp)
                    )
                    BasicText(
                        text = "Hierarchical (classic android app) navigation",
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .clickable(onClick = { setMode(Mode.Hierarchical) })
                            .padding(8.dp)
                    )
                    BasicText(
                        text = "Hierarchical (classic android app) route navigation",
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .clickable(onClick = { setMode(Mode.HierarchicalRoute) })
                            .padding(8.dp)
                    )
                }
            }
        }

        Mode.Chronological -> {
            ChronologicalBrowserNavigation(
                backStack = backStack,
                saveKey = { key ->
                    when (key) {
                        is Root -> buildBrowserHistoryFragment("root")
                        is Profile -> buildBrowserHistoryFragment("profile", mapOf("id" to key.id.toString()))
                        else -> null
                    }
                },
                restoreKey = { fragment ->
                    when (getBrowserHistoryFragmentName(fragment)) {
                        "root" -> Root
                        "profile" -> Profile(
                            getBrowserHistoryFragmentParameters(fragment).getValue("id")?.toInt()
                                ?: error("id is required")
                        )

                        else -> null
                    }
                }
            )
        }

        Mode.Hierarchical -> {
            HierarchicalBrowserNavigation {
                when (val key = backStack.lastOrNull()) {
                    is Root -> buildBrowserHistoryFragment("root")
                    is Profile -> buildBrowserHistoryFragment("profile", mapOf("id" to key.id.toString()))
                    else -> null
                }
            }
        }

        Mode.ChronologicalRoute -> {
            ChronologicalBrowserRouteNavigation(
                backStack = backStack,
                saveKey = { key ->
                    when (key) {
                        is Root -> buildBrowserHistoryRoute("root")
                        is Profile -> buildBrowserHistoryRoute(
                            pattern = "profile/{id}", parameters = mapOf("id" to key.id.toString())
                        )

                        else -> null
                    }
                },
                restoreKey = { route ->
                    RouteMatcher.match(route) {
                        pattern("root") { Root }
                        pattern("profile/{id}") { params ->
                            val id = params.getValue("id")?.toInt() ?: error("id is required")
                            Profile(id)
                        }
                    }
                }
            )
        }

        Mode.HierarchicalRoute -> {
            HierarchicalBrowserRouteNavigation {
                when (val key = backStack.lastOrNull()) {
                    is Root -> buildBrowserHistoryRoute("root")
                    is Profile -> buildBrowserHistoryRoute(
                        pattern = "profile?id={id}", parameters = mapOf("id" to key.id.toString())
                    )

                    else -> null
                }
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Root> {
                RootScreen(
                    onBuyPresent = { backStack.add(Profile(it)) }
                )
            }
            entry<Profile> { key ->
                ProfileScreen(
                    id = key.id,
                    onBack = {
                        backStack.clear()
                        backStack.add(Root)
                    },
                    onOpenPresent = {
                        backStack.add(Profile(it))
                    },
                    onChangePresent = {
                        backStack.removeLast()
                        backStack.add(Profile(it))
                    }
                )
            }
        }
    )
}

private const val PROFILES_COUNT = 3
private val COLORS = listOf(Color.Blue, Color.Red, Color.Magenta)

@Composable
fun RootScreen(
    onBuyPresent: (id: Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        BasicText(
            text = "Select a profile!",
            style = TextStyle.Default.copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
            modifier = Modifier.testTag("root_screen")
        )
        LazyColumn {
            items(PROFILES_COUNT) { i ->
                BasicText(
                    text = "PROFILE $i",
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .clickable(onClick = { onBuyPresent(i) })
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    id: Int,
    onBack: () -> Unit,
    onOpenPresent: (Int) -> Unit,
    onChangePresent: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        BasicText(
            text = "PROFILE $id",
            style = TextStyle.Default.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(16.dp)
                .background(COLORS[id])
                .padding(40.dp)
                .testTag("profile_screen_$id")
        )
        BasicText(
            "Return to the [root] screen",
            modifier = Modifier
                .padding(8.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .clickable(onClick = onBack)
                .padding(8.dp)
        )
        val otherPresent = (id + 1) % PROFILES_COUNT
        BasicText(
            "Open [PROFILE $otherPresent]",
            modifier = Modifier
                .padding(8.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .clickable(onClick = { onOpenPresent(otherPresent) })
                .padding(8.dp)
        )
        BasicText(
            "Replace the current with [PROFILE $otherPresent]",
            modifier = Modifier
                .padding(8.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .clickable(onClick = { onChangePresent(otherPresent) })
                .padding(8.dp)
        )
    }
}
