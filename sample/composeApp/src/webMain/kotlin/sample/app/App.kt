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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.ui.window.Dialog
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.github.terrakok.navigation3.browser.*

@OptIn(ExperimentalComposeUiApi::class)
fun main() = ComposeViewport { App() }

data object Root

data class Profile(val id: Int)

@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Any>(Root) }

    val (mode, setMode) = remember { mutableStateOf<Boolean?>(null) }
    if (mode == null) {
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
                        .clickable(onClick = { setMode(true) })
                        .padding(8.dp)
                )
                BasicText(
                    text = "Hierarchical (classic android app) navigation",
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .clickable(onClick = { setMode(false) })
                        .padding(8.dp)
                )
            }
        }
    } else if (mode) {
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
    } else {
        HierarchicalBrowserNavigation(
            currentDestination = remember { derivedStateOf { backStack.lastOrNull() } },
            currentDestinationName = { key ->
                when (key) {
                    is Root -> buildBrowserHistoryFragment("root")
                    is Profile -> buildBrowserHistoryFragment("profile", mapOf("id" to key.id.toString()))
                    else -> null
                }
            },
        )
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
