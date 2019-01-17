package me.uport.sdk.testhelpers

import me.uport.sdk.core.ITimeProvider

/**
 * For testing purposes only.
 * A fixed time provider where the developer has to provide the current timestamp.
 */
class TestTimeProvider(private val currentTimeMs: Long) : ITimeProvider {
    override fun nowMs(): Long = currentTimeMs
}