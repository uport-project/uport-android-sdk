package me.uport.sdk.core

import org.junit.Assert.assertTrue
import org.junit.Test

class TimeProviderTest {

    @Test
    fun `default provider is close to current time`() {
        val systemTime = System.currentTimeMillis()
        val defaultProvider = SystemTimeProvider()
        //some systems have tens of milliseconds as the lowest granularity
        assertTrue(defaultProvider.now() - systemTime < 100L)
    }
}