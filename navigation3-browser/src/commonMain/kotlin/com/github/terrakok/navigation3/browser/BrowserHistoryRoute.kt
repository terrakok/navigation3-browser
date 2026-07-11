package com.github.terrakok.navigation3.browser

/**
 * Represents a matched route with its parameters.
 *
 * @property route The original route pattern.
 * @property parameters A map of parameter names to their extracted values.
 */
data class BrowserHistoryRouteMatch(
    val route: String,
    val parameters: Map<String, String?>
)

/**
 * Builds a URL path and query string from a [pattern] and [parameters].
 *
 * Placeholders in the pattern (e.g., `{id}`) are replaced with values from the [parameters] map.
 * Both path segments and query parameters are URL-encoded.
 *
 * Example:
 * `buildBrowserHistoryRoute("/user/{id}?tab={tab}", mapOf("id" to "42", "tab" to "info"))`
 * → `"/user/42?tab=info"`
 *
 * @param pattern The route pattern containing optional placeholders.
 * @param parameters A map of parameter names to their values.
 * @return A formatted and encoded URL string.
 */
fun buildBrowserHistoryRoute(
    pattern: String,
    parameters: Map<String, String?> = emptyMap()
): String {
    val (pathPattern, queryPattern) = pattern.split("?", limit = 2).let {
        it[0] to it.getOrNull(1)
    }

    val path = pathPattern
        .trim('/')
        .split('/')
        .filter { it.isNotEmpty() }
        .joinToString("/", prefix = "/") { segment ->
            if (segment.isParameter()) {
                encodeURIComponent(parameters.getValue(segment.parameterName())!!)
            } else {
                encodeURIComponent(segment)
            }
        }

    if (queryPattern == null) return path

    val query = queryPattern
        .split("&")
        .filter { it.isNotEmpty() }
        .joinToString("&") { part ->
            val (key, value) = part.split("=", limit = 2)

            val encodedValue = if (value.isParameter()) {
                encodeURIComponent(parameters.getValue(value.parameterName())!!)
            } else {
                encodeURIComponent(value)
            }

            "${encodeURIComponent(key)}=$encodedValue"
        }

    return "$path?$query"
}

/**
 * Matches a [path] against a route [pattern].
 *
 * If the path matches the pattern, a [BrowserHistoryRouteMatch] is returned containing the extracted parameters.
 * Path segments and query parameters are decoded before matching.
 *
 * Example:
 * `matchBrowserHistoryRoute("/user/{id}", "/user/42")` → `BrowserHistoryRouteMatch("/user/{id}", mapOf("id" to "42"))`
 *
 * @param pattern The route pattern to match against.
 * @param path The actual URL path to match.
 * @return A [BrowserHistoryRouteMatch] if successful, or `null` otherwise.
 */
fun matchBrowserHistoryRoute(
    pattern: String,
    path: String
): BrowserHistoryRouteMatch? {
    val (patternPath, patternQuery) = pattern.split("?", limit = 2).let {
        it[0] to it.getOrNull(1)
    }

    val (actualPath, actualQuery) = path.split("?", limit = 2).let {
        it[0] to it.getOrNull(1)
    }

    val patternSegments = patternPath.trim('/').split('/').filter { it.isNotEmpty() }
    val pathSegments = actualPath.trim('/').split('/').filter { it.isNotEmpty() }

    if (patternSegments.size != pathSegments.size) {
        return null
    }

    val params = mutableMapOf<String, String>()

    // Match path
    for (i in patternSegments.indices) {
        val patternSegment = patternSegments[i]
        val pathSegment = decodeURIComponent(pathSegments[i])

        if (patternSegment.isParameter()) {
            params[patternSegment.parameterName()] = pathSegment
        } else if (patternSegment != pathSegment) {
            return null
        }
    }

    // Match query
    val patternQueryMap = parseQuery(patternQuery)
    val actualQueryMap = parseQuery(actualQuery)

    for ((key, expectedValue) in patternQueryMap) {
        val actualValue = actualQueryMap[key] ?: return null

        if (expectedValue.isParameter()) {
            params[expectedValue.parameterName()] = actualValue
        } else if (expectedValue != actualValue) {
            return null
        }
    }

    return BrowserHistoryRouteMatch(pattern, params)
}

/**
 * Checks if a [path] matches a route [pattern].
 *
 * @param pattern The route pattern to match against.
 * @param path The actual URL path to match.
 * @return `true` if the path matches the pattern, `false` otherwise.
 */
fun browserHistoryRouteMatches(
    pattern: String,
    path: String
): Boolean = matchBrowserHistoryRoute(pattern, path) != null

/**
 * Extracts and decodes individual path segments from a [path].
 *
 * Example: `getBrowserHistoryRouteSegments("/user/42/details")` → `["user", "42", "details"]`
 *
 * @param path The URL path to parse.
 * @return A list of decoded path segments.
 */
fun getBrowserHistoryRouteSegments(path: String): List<String> =
    path.substringBefore('?')
        .trim('/')
        .split('/')
        .filter { it.isNotEmpty() }
        .map(::decodeURIComponent)

/**
 * Extracts parameters from a [path] based on a route [pattern].
 *
 * @param pattern The route pattern containing placeholders.
 * @param path The actual URL path.
 * @return A map of extracted parameter names to their values, or an empty map if no match.
 */
fun getBrowserHistoryRouteParameters(
    pattern: String,
    path: String
): Map<String, String?> =
    matchBrowserHistoryRoute(pattern, path)?.parameters.orEmpty()

private fun parseQuery(query: String?): Map<String, String> {
    if (query.isNullOrEmpty()) return emptyMap()

    return query.split("&")
        .filter { it.isNotEmpty() }
        .associate { pair ->
            val (key, value) = pair.split("=", limit = 2)
            decodeURIComponent(key) to decodeURIComponent(value)
        }
}

private fun String.isParameter(): Boolean =
    startsWith("{") && endsWith("}")

private fun String.parameterName(): String =
    substring(1, length - 1)
