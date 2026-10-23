package eu.kanade.tachiyomi.network

import com.nilemnod.mangareader.core.network.JavaScriptEngine as CoreJavaScriptEngine

/**
 * Util for evaluating JavaScript in sources.
 */
@Suppress("UNUSED")
class JavaScriptEngine(private val core: CoreJavaScriptEngine) {

    /**
     * Evaluate arbitrary JavaScript code and get the result as a primtive type
     * (e.g., String, Int).
     *
     * @since extensions-lib 1.4
     * @param script JavaScript to execute.
     * @return Result of JavaScript code as a primitive type.
     */
    suspend fun <T> evaluate(script: String): T = core.evaluate(script)
}
