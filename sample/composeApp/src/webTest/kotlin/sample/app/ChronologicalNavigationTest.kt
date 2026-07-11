package sample.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.browser.window
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ChronologicalNavigationTest {
    @Test
    fun simpleNavigationTest() = runComposeUiTest {
        defaultMode = Mode.Chronological
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
        assertEquals(3, window.history.length)

        onNodeWithText("Return to the [root] screen").performClick()
        awaitIdle()

        onNodeWithTag("root_screen").assertIsDisplayed()
        assertEquals("root", urlFragment())
        assertEquals(4, window.history.length)

        onNodeWithText("PROFILE 1").performClick()
        awaitIdle()

        onNodeWithTag("profile_screen_1").assertIsDisplayed()
        assertEquals("profile?id=1", urlFragment())
        assertEquals(5, window.history.length)

        onNodeWithText("Replace the current with [PROFILE 2]").performClick()
        awaitIdle()

        onNodeWithTag("profile_screen_2").assertIsDisplayed()
        assertEquals("profile?id=2", urlFragment())
        assertEquals(6, window.history.length)

        browserBack()

        onNodeWithTag("profile_screen_1").assertIsDisplayed()
        assertEquals("profile?id=1", urlFragment())
        assertEquals(6, window.history.length)

        browserBack()

        onNodeWithTag("root_screen").assertIsDisplayed()
        assertEquals("root", urlFragment())
        assertEquals(6, window.history.length)

        browserBack()

        onNodeWithTag("profile_screen_1").assertIsDisplayed()
        assertEquals("profile?id=1", urlFragment())
        assertEquals(6, window.history.length)

        browserForward()

        onNodeWithTag("root_screen").assertIsDisplayed()
        assertEquals("root", urlFragment())
        assertEquals(6, window.history.length)

        onNodeWithText("PROFILE 0").performClick()
        awaitIdle()

        assertEquals("profile?id=0", urlFragment())
        assertEquals(5, window.history.length)
        assertEquals(
            """
                #root
                #profile?id=0
            """.trimIndent(),
            window.history.state.toString()
        )
    }
}