package com.github.terrakok.navigation3.browser

import kotlin.test.*

class RouteMatcherTest {

    @Test
    fun testBasicMatch() {
        val result = RouteMatcher.match<String>("/user/42") {
            pattern("/root") { "Root" }
            pattern("/user/{id}") { params -> "User ${params["id"]}" }
        }
        assertEquals("User 42", result)
    }

    @Test
    fun testNoMatch() {
        val result = RouteMatcher.match<String>("/unknown") {
            pattern("/root") { "Root" }
        }
        assertNull(result)
    }

    @Test
    fun testFirstMatchWins() {
        val result = RouteMatcher.match<String>("/user/42") {
            pattern("/user/{id}") { "First" }
            pattern("/user/42") { "Second" }
        }
        assertEquals("First", result)
    }

    @Test
    fun testGroup() {
        val result = RouteMatcher.match<String>("/admin/users/42") {
            group("/admin") {
                group("/users") {
                    pattern("/{id}") { params -> "Admin User ${params["id"]}" }
                }
            }
        }
        assertEquals("Admin User 42", result)
    }

    @Test
    fun testGroupMismatch() {
        val result = RouteMatcher.match<String>("/user/42") {
            group("/admin") {
                pattern("/users/{id}") { "Admin" }
            }
            pattern("/user/{id}") { "User" }
        }
        assertEquals("User", result)
    }

    @Test
    fun testNestedGroups() {
        val result = RouteMatcher.match<String>("/a/b/c") {
            group("/a") {
                group("/b") {
                    pattern("/c") { "ABC" }
                }
            }
        }
        assertEquals("ABC", result)
    }
}
