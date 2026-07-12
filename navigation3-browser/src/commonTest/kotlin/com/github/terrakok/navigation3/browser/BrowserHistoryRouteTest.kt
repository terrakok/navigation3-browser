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
        assertEquals("/root", match1.pattern)
        assertTrue(match1.parameters.isEmpty())

        // Match with placeholders
        val match2 = matchBrowserHistoryRoute("/user/{id}", "/user/42")
        assertNotNull(match2)
        assertEquals("/user/42", match2.route)
        assertEquals("/user/{id}", match2.pattern)
        assertEquals(mapOf("id" to "42"), match2.parameters)

        // Match with multiple placeholders
        val match3 = matchBrowserHistoryRoute("/user/{id}/{tab}", "/user/42/info")
        assertNotNull(match3)
        assertEquals(mapOf("id" to "42", "tab" to "info"), match3.parameters)

        // Match with query parameters (q=kotlin is auto-extracted, query=kotlin is from pattern)
        val match4 = matchBrowserHistoryRoute("/search?q={query}", "/search?q=kotlin")
        assertNotNull(match4)
        assertEquals(mapOf("q" to "kotlin", "query" to "kotlin"), match4.parameters)

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

        // Automatic query parameter extraction (pattern doesn't have query)
        val match6 = matchBrowserHistoryRoute("/profile", "/profile?id=42&mode=edit")
        assertNotNull(match6)
        assertEquals(mapOf("id" to "42", "mode" to "edit"), match6.parameters)

        // Automatic query parameter extraction (pattern HAS different query)
        val match7 = matchBrowserHistoryRoute("/search?q={q}", "/search?q=kotlin&page=1")
        assertNotNull(match7)
        assertEquals(mapOf("q" to "kotlin", "page" to "1"), match7.parameters)
    }

    @Test
    fun testWildcardMatching() {
        // Simple trailing wildcard
        val match1 = matchBrowserHistoryRoute("/view/*", "/view/dashboard")
        assertNotNull(match1)
        assertEquals("/view/dashboard", match1.route)
        assertEquals("/view/*", match1.pattern)

        // Multi-level wildcard (trailing)
        val match2 = matchBrowserHistoryRoute("/view/*", "/view/dashboard/results/123")
        assertNotNull(match2)
        assertEquals("/view/dashboard/results/123", match2.route)

        // Mid-pattern wildcard (matches exactly one segment)
        val match3 = matchBrowserHistoryRoute("/a/*/c", "/a/b/c")
        assertNotNull(match3)
        assertEquals("/a/b/c", match3.route)

        assertNull(matchBrowserHistoryRoute("/a/*/c", "/a/b/d/c")) // too many segments

        // Root wildcard
        val match4 = matchBrowserHistoryRoute("/*", "/any/path/here")
        assertNotNull(match4)
        assertEquals("/any/path/here", match4.route)

        // Wildcard with query parameters
        val match5 = matchBrowserHistoryRoute("/search/*?q={query}", "/search/all/results?q=kotlin")
        assertNotNull(match5)
        assertEquals(mapOf("q" to "kotlin", "query" to "kotlin"), match5.parameters)
        assertEquals("/search/all/results?q=kotlin", match5.route)

        // Wildcard does not match if prefix mismatch
        assertNull(matchBrowserHistoryRoute("/view/*", "/other/dashboard"))

        // Trailing wildcard matches base path
        val match6 = matchBrowserHistoryRoute("/view/*", "/view")
        assertNotNull(match6)

        // Multiple wildcards
        val match7 = matchBrowserHistoryRoute("/a/*/c/*", "/a/b/c/d/e")
        assertNotNull(match7)
        assertEquals("/a/b/c/d/e", match7.route)
    }

    @Test
    fun testBrowserHistoryRouteMatches() {
        assertTrue(browserHistoryRouteMatches("/root", "/root"))
        assertTrue(browserHistoryRouteMatches("/user/{id}", "/user/42"))
        assertFalse(browserHistoryRouteMatches("/user/{id}", "/admin/42"))
        assertTrue(browserHistoryRouteMatches("/view/*", "/view/a/b/c"))
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
        assertEquals(mapOf("q" to "kotlin", "query" to "kotlin"), getBrowserHistoryRouteParameters("/search?q={query}", "/search?q=kotlin"))
        assertTrue(getBrowserHistoryRouteParameters("/root", "/admin").isEmpty())
        assertEquals(mapOf("q" to "1"), getBrowserHistoryRouteParameters("/view/*?q={q}", "/view/a/b?q=1"))
        
        // Automatic extraction
        assertEquals(mapOf("id" to "1"), getBrowserHistoryRouteParameters("/profile", "/profile?id=1"))
    }

    @Test
    fun testQueryParsingEdgeCases() {
        // Parameter without value
        val match = matchBrowserHistoryRoute("/test", "/test?flag")
        assertNotNull(match)
        assertEquals(mapOf("flag" to null), match.parameters)

        // Empty value
        val match2 = matchBrowserHistoryRoute("/test", "/test?key=")
        assertNotNull(match2)
        assertEquals(mapOf("key" to ""), match2.parameters)
    }
}
