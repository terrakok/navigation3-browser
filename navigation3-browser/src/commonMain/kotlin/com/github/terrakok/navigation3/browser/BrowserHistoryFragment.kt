package com.github.terrakok.navigation3.browser

/**
 * Builds a URL hash fragment for the browser history from a destination [name] and optional
 * [parameters].
 *
 * The result starts with `#` and encodes the name and each parameter using `encodeURIComponent`.
 * Parameters are sorted by key for stable output and joined using `?` and `&`, e.g.:
 *
 * - `buildBrowserHistoryFragment("root")` → `#root`
 * - `buildBrowserHistoryFragment("profile", mapOf("id" to "42"))` → `#profile?id=42`
 * - Null parameter values are emitted without `=`, e.g. `#screen?flag`.
 *
 * @param name Destination name (will be URL‑encoded).
 * @param parameters Optional map of query parameters; values may be null.
 * @return A URL fragment string beginning with `#`.
 */
fun buildBrowserHistoryFragment(
    name: String,
    parameters: Map<String, String?> = emptyMap()
): String = buildString {
    append("#")
    append(encodeURIComponent(name))
    if (parameters.isNotEmpty()) {
        append("?")
        append(
            parameters.entries.sortedBy { it.key }.joinToString("&") {
                val k = encodeURIComponent(it.key)
                val v = it.value
                if (v == null) k else "$k=${encodeURIComponent(v)}"
            }
        )
    }
}

/**
 * Extracts and decodes a destination name from a URL [fragment] built by
 * [buildBrowserHistoryFragment].
 *
 * Returns `null` if the fragment is malformed or empty.
 *
 * Example: `getBrowserHistoryFragmentName("#profile?id=42")` → `"profile"`.
 *
 * @param fragment A string that may contain a leading `#` and optional query.
 * @return The decoded destination name or `null` if it cannot be parsed.
 */
fun getBrowserHistoryFragmentName(fragment: String): String? {
    val res = fragment.substringAfter('#', "").substringBeforeLast('?')
    if (res.isBlank() || res.contains('#') || res.contains('?')) return null
    return decodeURIComponent(res)
}

/**
 * Parses and decodes query parameters from a URL [fragment] built by
 * [buildBrowserHistoryFragment].
 *
 * Parameters without a value are returned with `null` as their value.
 *
 * Example: `getBrowserHistoryFragmentParameters("#profile?id=42")` → `{"id": "42"}`.
 *
 * @param fragment A string that may contain a leading `#` and query after `?`.
 * @return A map of decoded parameter names to decoded values (or `null`).
 */
fun getBrowserHistoryFragmentParameters(fragment: String): Map<String, String?> {
    val paramStr = fragment.substringAfterLast('?', "")
    if (paramStr.isEmpty()) return emptyMap()
    return paramStr.split("&").filter { it.isNotEmpty() }.associate { p ->
        val split = p.split("=", limit = 2)
        val key = split[0]
        val value = split.getOrNull(1)
        decodeURIComponent(key) to value?.let { decodeURIComponent(value) }
    }
}