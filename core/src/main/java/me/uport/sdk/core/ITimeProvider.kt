package me.uport.sdk.core

/**
 * An interface for getting "current" timestamp.
 *
 * The default implementation is [SystemTimeProvider] but other implementations may be used during testing and for "was valid at" scenarios.
 */
interface ITimeProvider {
    /**
     * Returns the current timestamp in milliseconds
     * @return the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     */
    fun nowMs(): Long
}


/**
 * Default time provider
 */
object SystemTimeProvider : ITimeProvider {
    override fun nowMs() = System.currentTimeMillis()
}