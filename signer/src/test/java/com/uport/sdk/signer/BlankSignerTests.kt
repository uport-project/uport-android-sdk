package com.uport.sdk.signer

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Test

class BlankSignerTests {

    private val tested = Signer.blank

    @Test
    fun `sign ETH calls back without error`() {
        tested.signETH("hello".toByteArray()) { err, _ ->
            Assert.assertNull(err)
        }
    }

    @Test
    fun `signJWT calls back without error`() {
        tested.signJWT("hello".toByteArray()) { err, _ ->
            Assert.assertNull(err)
        }
    }

    @Test
    fun `signETH coroutine passes`() = runBlocking {
        val sig = tested.signETH("hello".toByteArray())
        assertNotNull(sig)
    }

    @Test
    fun `signJWT coroutine passes`() = runBlocking {
        val sig = tested.signJWT("hello".toByteArray())
        assertNotNull(sig)
    }

    @Test
    fun `returned address is non null`() {
        assertNotNull(tested.getAddress())
    }
}