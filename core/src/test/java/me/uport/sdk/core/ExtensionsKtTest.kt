package me.uport.sdk.core

import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class ExtensionsKtTest {

    @Before
    fun `run before every test`() {
        stubUiContext()
    }

    @Test
    fun `can use stubbed UI context in unit test`() {

        suspend fun whatever(): String = withContext(UI) { "hello world" }

        runBlocking {
            assertEquals("hello world", whatever())
        }
    }

    @Test
    fun `base 64 works both ways`() {
        val strings = listOf(
                "",
                "f",
                "fo",
                "foo",
                "foo ",
                "foo b",
                "foo ba",
                "foo bar",
                "foo bar ",
                "foo bar b",
                "foo bar ba",
                "foo bar baz"
        )
        strings.forEach {
            assertEquals(it, String(it.toBase64().decodeBase64()))
            assertArrayEquals(it.toByteArray(), it.toBase64().decodeBase64())
            assertEquals(it, String(it.toBase64().decodeBase64()))
            assertArrayEquals(it.toByteArray(), it.toBase64UrlSafe().decodeBase64())
        }

        val bytes = ByteArray(255) { it.toByte() }
        for (i in 0..bytes.size) {
            val tested = bytes.copyOfRange(0, i)
            assertArrayEquals(tested, tested.toBase64().decodeBase64())
            assertArrayEquals(tested, tested.toBase64UrlSafe().decodeBase64())
        }
    }
}