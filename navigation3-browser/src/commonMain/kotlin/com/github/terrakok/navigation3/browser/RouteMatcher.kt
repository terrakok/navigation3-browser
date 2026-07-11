package com.github.terrakok.navigation3.browser

/**
 * A DSL for matching a URL [path] against multiple route patterns.
 *
 * Patterns are checked in the order they are defined. Once a pattern matches, its result
 * is stored and subsequent patterns are ignored ("first-match-wins").
 *
 * @param T The type of result produced by the match blocks (e.g., a screen key).
 * @property path The actual URL path to match against.
 */
class RouteMatcher<T> internal constructor(
    val path: String
) {
    internal var result: T? = null
    private var prefix = ""

    /**
     * Defines a route [pattern] to match against the current [path].
     *
     * If the pattern matches (taking into account any parent [group] prefixes), the [block]
     * is executed with the extracted parameters, and its result is used as the final match result.
     *
     * @param pattern The route pattern (e.g., "/user/{id}").
     * @param block A lambda that takes the extracted parameters and returns a result of type [T].
     */
    fun pattern(
        pattern: String,
        block: (Map<String, String?>) -> T
    ) {
        if (result != null) return

        val fullPattern = joinPath(prefix, pattern)
        val match = matchBrowserHistoryRoute(fullPattern, path) ?: return

        result = block(match.parameters)
    }

    /**
     * Groups multiple patterns under a common [prefix].
     *
     * Prefixes are combined with nested patterns and child groups using path joining logic.
     *
     * @param prefix The path prefix to apply to all patterns defined within the [block].
     * @param block A configuration block where nested [pattern]s and [group]s are defined.
     */
    fun group(
        prefix: String,
        block: RouteMatcher<T>.() -> Unit
    ) {
        if (result != null) return

        val previous = this.prefix
        this.prefix = joinPath(previous, prefix)

        try {
            block()
        } finally {
            this.prefix = previous
        }
    }

    private fun joinPath(left: String, right: String): String {
        val l = left.trimEnd('/')
        val r = right.trimStart('/')

        return when {
            l.isEmpty() -> "/$r"
            r.isEmpty() -> l
            else -> "$l/$r"
        }
    }

    companion object {
        /**
         * Evaluates the [block] against the given [path] and returns the result of the first matching pattern.
         *
         * Usage example:
         *
         * ```kotlin
         * val key = RouteMatcher.match(path) {
         *     pattern("/root") { Root }
         *     group("/admin") {
         *         pattern("/users/{id}") { params -> AdminUser(params.getValue("id")!!) }
         *     }
         * }
         * ```
         *
         * @param path The URL path to match.
         * @param block A configuration block for the [RouteMatcher].
         * @return The result from the first matching pattern, or `null` if no patterns match.
         */
        fun <T> match(
            path: String,
            block: RouteMatcher<T>.() -> Unit
        ): T? {
            return RouteMatcher<T>(path).apply(block).result
        }
    }
}
