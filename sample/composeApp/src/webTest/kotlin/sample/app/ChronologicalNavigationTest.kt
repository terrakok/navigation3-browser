package sample.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
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
        defaultMode = true
        setContent { App() }
        awaitIdle()

        onNodeWithText("Select a profile!").assertIsDisplayed()
        assertEquals("root", urlFragment())
        assertEquals(1, window.history.length)

        onNodeWithText("PROFILE 0").performClick()
        awaitIdle()

        onNodeWithText("PROFILE 0").assertIsDisplayed()
        onNodeWithText("Open [PROFILE 1]").assertIsDisplayed()
        assertEquals("profile?id=0", urlFragment())
        assertEquals(2, window.history.length)

        onNodeWithText("Open [PROFILE 1]").performClick()
        awaitIdle()

        onNodeWithText("PROFILE 1").assertIsDisplayed()
        onNodeWithText("Open [PROFILE 2]").assertIsDisplayed()
        assertEquals("profile?id=1", urlFragment())
        assertEquals(3, window.history.length)

        onNodeWithText("Return to the [root] screen").performClick()
        awaitIdle()

        onNodeWithText("Select a profile!").assertIsDisplayed()
        assertEquals("root", urlFragment())
        assertEquals(4, window.history.length)

        onNodeWithText("PROFILE 1").performClick()
        awaitIdle()

        onNodeWithText("PROFILE 1").assertIsDisplayed()
        onNodeWithText("Open [PROFILE 2]").assertIsDisplayed()
        assertEquals("profile?id=1", urlFragment())
        assertEquals(5, window.history.length)

        onNodeWithText("Replace the current with [PROFILE 2]").performClick()
        awaitIdle()

        onNodeWithText("PROFILE 2").assertIsDisplayed()
        onNodeWithText("Open [PROFILE 0]").assertIsDisplayed()
        assertEquals("profile?id=2", urlFragment())
        assertEquals(6, window.history.length)

        browserBack()

        onNodeWithText("PROFILE 1").assertIsDisplayed()
        onNodeWithText("Open [PROFILE 2]").assertIsDisplayed()
        assertEquals("profile?id=1", urlFragment())
        assertEquals(6, window.history.length)

        browserBack()

        onNodeWithText("Select a profile!").assertIsDisplayed()
        assertEquals("root", urlFragment())
        assertEquals(6, window.history.length)

        browserBack()

        onNodeWithText("PROFILE 1").assertIsDisplayed()
        onNodeWithText("Open [PROFILE 2]").assertIsDisplayed()
        assertEquals("profile?id=1", urlFragment())
        assertEquals(6, window.history.length)

        browserForward()

        onNodeWithText("Select a profile!").assertIsDisplayed()
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