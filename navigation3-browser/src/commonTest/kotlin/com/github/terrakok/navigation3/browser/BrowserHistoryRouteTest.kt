package com.github.terrakok.navigation3.browser

import kotlin.test.*

class BrowserHistoryRouteTest {

    @Test
    fun testBuildBrowserHistoryRoute() {
        // Simple path
        assertEquals("/root", buildBrowserHistoryRoute("root"))
        assertEquals("/root", buildBrowserHistoryRoute("/root"))

        // Path with placeholders
        assertEquals("/user/42", buildBrowserHistoryRoute("/user/{id}", mapOf("id" to "42")))
        assertEquals("/user/42/details", buildBrowserHistoryRoute("/user/{id}/{tab}", mapOf("id" to "42", "tab" to "details")))

        // Encoding in path
        assertEquals("/test%20name", buildBrowserHistoryRoute("/{name}", mapOf("name" to "test name")))
        assertEquals("/test%2Bname", buildBrowserHistoryRoute("/{name}", mapOf("name" to "test+name")))

        // Query parameters
        assertEquals("/search?q=kotlin", buildBrowserHistoryRoute("/search?q=kotlin"))
        assertEquals("/search?q=kotlin", buildBrowserHistoryRoute("/search?q={query}", mapOf("query" to "kotlin")))
        assertEquals("/search?q=kotlin%20lang", buildBrowserHistoryRoute("/search?q={query}", mapOf("query" to "kotlin lang")))

        // Multiple query parameters
        assertEquals("/api?id=1&type=user", buildBrowserHistoryRoute("/api?id={id}&type={type}", mapOf("id" to "1", "type" to "user")))

        // Mix of path and query placeholders
        assertEquals("/user/42?tab=info", buildBrowserHistoryRoute("/user/{id}?tab={tab}", mapOf("id" to "42", "tab" to "info")))
    }

    @Test
    fun testMatchBrowserHistoryRoute() {
        // Exact match
        val match1 = matchBrowserHistoryRoute("/root", "/root")
        assertNotNull(match1)
        assertEquals("/root", match1.route)
        assertTrue(match1.parameters.isEmpty())

        // Match with placeholders
        val match2 = matchBrowserHistoryRoute("/user/{id}", "/user/42")
        assertNotNull(match2)
        assertEquals("/user/{id}", match2.route)
        assertEquals(mapOf("id" to "42"), match2.parameters)

        // Match with multiple placeholders
        val match3 = matchBrowserHistoryRoute("/user/{id}/{tab}", "/user/42/info")
        assertNotNull(match3)
        assertEquals(mapOf("id" to "42", "tab" to "info"), match3.parameters)

        // Match with query parameters
        val match4 = matchBrowserHistoryRoute("/search?q={query}", "/search?q=kotlin")
        assertNotNull(match4)
        assertEquals(mapOf("query" to "kotlin"), match4.parameters)

        // Mismatched path length
        assertNull(matchBrowserHistoryRoute("/user/{id}", "/user/42/details"))
        assertNull(matchBrowserHistoryRoute("/user/{id}/details", "/user/42"))

        // Mismatched static segments
        assertNull(matchBrowserHistoryRoute("/user/{id}", "/admin/42"))

        // Mismatched query parameter key
        assertNull(matchBrowserHistoryRoute("/search?q={query}", "/search?p=kotlin"))

        // Mismatched query parameter value (constant)
        assertNull(matchBrowserHistoryRoute("/search?type=user", "/search?type=admin"))

        // Decoding during match
        val match5 = matchBrowserHistoryRoute("/user/{name}", "/user/test%20name")
        assertNotNull(match5)
        assertEquals(mapOf("name" to "test name"), match5.parameters)
    }

    @Test
    fun testBrowserHistoryRouteMatches() {
        assertTrue(browserHistoryRouteMatches("/root", "/root"))
        assertTrue(browserHistoryRouteMatches("/user/{id}", "/user/42"))
        assertFalse(browserHistoryRouteMatches("/user/{id}", "/admin/42"))
    }

    @Test
    fun testGetBrowserHistoryRouteSegments() {
        assertEquals(listOf("root"), getBrowserHistoryRouteSegments("/root"))
        assertEquals(listOf("user", "42"), getBrowserHistoryRouteSegments("/user/42"))
        assertEquals(listOf("user", "test name"), getBrowserHistoryRouteSegments("/user/test%20name"))
        assertEquals(listOf("user", "42"), getBrowserHistoryRouteSegments("/user/42?tab=info"))
    }

    @Test
    fun testGetBrowserHistoryRouteParameters() {
        assertEquals(mapOf("id" to "42"), getBrowserHistoryRouteParameters("/user/{id}", "/user/42"))
        assertEquals(mapOf("query" to "kotlin"), getBrowserHistoryRouteParameters("/search?q={query}", "/search?q=kotlin"))
        assertTrue(getBrowserHistoryRouteParameters("/root", "/admin").isEmpty())
    }
}
