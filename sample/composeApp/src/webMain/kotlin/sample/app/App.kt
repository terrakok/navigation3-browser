package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.ui.window.Dialog
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.github.terrakok.navigation3.browser.ConfigureBrowserBack
import com.github.terrakok.navigation3.browser.bindBackStackToBrowserHistory
import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentName
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentParameters
import org.jetbrains.skia.Surface

@OptIn(ExperimentalComposeUiApi::class)
fun main() = ComposeViewport { App() }

data object Main

data class Present(val id: Int)

@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Any>(Main) }

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
        LaunchedEffect(Unit) {
            bindBackStackToBrowserHistory(
                backStack = backStack,
                saveItem = { key ->
                    when (key) {
                        is Main -> buildBrowserHistoryFragment("main")
                        is Present -> buildBrowserHistoryFragment("present", mapOf("id" to key.id.toString()))
                        else -> null
                    }.toString()
                },
                restoreItem = { fragment ->
                    when (getBrowserHistoryFragmentName(fragment)) {
                        "main" -> Main
                        "present" -> Present(
                            getBrowserHistoryFragmentParameters(fragment).getValue("id")?.toInt()
                                ?: error("id is required")
                        )

                        else -> null
                    }
                }
            )
        }
    } else {
        ConfigureBrowserBack(
            currentDestinationName = {
                when (val key = backStack.lastOrNull()) {
                    is Main -> buildBrowserHistoryFragment("main")
                    is Present -> buildBrowserHistoryFragment("present", mapOf("id" to key.id.toString()))
                    else -> null
                }.toString()
            },
        )
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                MainScreen(
                    onBuyPresent = { backStack.add(Present(it)) }
                )
            }
            entry<Present> { key ->
                PresentScreen(
                    id = key.id,
                    onBack = { backStack.removeLast() },
                    onOpenPresent = {
                        backStack.add(Present(it))
                    },
                    onChangePresent = {
                        backStack.removeLast()
                        backStack.add(Present(it))
                    }
                )
            }
        }
    )
}

private const val PRESENTS_COUNT = 3
private val COLORS = listOf(Color.Blue, Color.Red, Color.Magenta)

@Composable
fun MainScreen(
    onBuyPresent: (id: Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        BasicText(
            text = "Select a present!",
            style = TextStyle.Default.copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
        )
        LazyColumn {
            items(PRESENTS_COUNT) { i ->
                BasicText(
                    text = "PRESENT $i",
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
fun PresentScreen(
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
            text = "PRESENT $id",
            style = TextStyle.Default.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(16.dp)
                .background(COLORS[id])
                .padding(40.dp)
        )
        Row {
            BasicText(
                "Return to the [main screen]",
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .padding(8.dp)
            )
            val otherPresent = (id + 1) % PRESENTS_COUNT
            BasicText(
                "Open [PRESENT $otherPresent]",
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .clickable(onClick = { onOpenPresent(otherPresent) })
                    .padding(8.dp)
            )
            BasicText(
                "Replace the current with [PRESENT $otherPresent]",
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .clickable(onClick = { onChangePresent(otherPresent) })
                    .padding(8.dp)
            )
        }
    }
}
