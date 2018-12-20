package me.uport.sdk.jwt.model

import me.uport.sdk.core.ITimeProvider

class TestTimeProvider(private val currentTime: Long) : ITimeProvider {
    override fun now(): Long = currentTime

}