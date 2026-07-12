package sample.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.browser.window
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class HierarchicalNavigationTest {
    @Test
    fun simpleNavigationTest() = runComposeUiTest {
        defaultMode = Mode.Hierarchical
        setContent { App() }
        awaitIdle()

        onNodeWithTag("root_screen").assertIsDisplayed()
        assertEquals("root", urlFragment())
        assertEquals(1, window.history.length)

        onNodeWithText("PROFILE 0").performClick()
        awaitIdle()

        onNodeWithTag("profile_screen_0").assertIsDisplayed()
        assertEquals("profile?id=0", urlFragment())
        assertEquals(2, window.history.length)

        onNodeWithText("Open [PROFILE 1]").performClick()
        awaitIdle()

        onNodeWithTag("profile_screen_1").assertIsDisplayed()
        assertEquals("profile?id=1", urlFragment())
        assertEquals(2, window.history.length)

        browserBack()
        awaitIdle()
        waitHistoryPopState()

        onNodeWithTag("profile_screen_0").assertIsDisplayed()
        assertEquals("profile?id=0", urlFragment())
        assertEquals(2, window.history.length)

        onNodeWithText("Replace the current with [PROFILE 1]").performClick()
        awaitIdle()

        onNodeWithTag("profile_screen_1").assertIsDisplayed()
        assertEquals("profile?id=1", urlFragment())
        assertEquals(2, window.history.length)

        browserBack()
        awaitIdle()
        waitHistoryPopState()
        awaitIdle()
        waitHistoryPopState()

        onNodeWithTag("root_screen").assertIsDisplayed()
        assertEquals("root", urlFragment())
        assertEquals(2, window.history.length)
    }
}