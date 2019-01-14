@file:Suppress("ReplaceCallWithBinaryOperator")

package me.uport.sdk.core

import assertk.assert
import assertk.assertions.isEqualTo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test

class ExtensionsKtTest {

    @Before
    fun `run before every test`() {
        stubUiContext()
    }

    @Test
    fun `can use stubbed UI context in unit test`() {

        suspend fun whatever(): String = withContext(UI) { "hello world" }

        runBlocking {
            assert(whatever()).isEqualTo("hello world")
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
            assert(it).isEqualTo(String(it.toBase64().decodeBase64()))
            assert(it.toByteArray()).isEqualTo(it.toBase64().decodeBase64())
            assert(it.toByteArray()).isEqualTo(it.toBase64UrlSafe().decodeBase64())
        }

        val bytes = ByteArray(255) { it.toByte() }
        for (i in 0..bytes.size) {
            val tested = bytes.copyOfRange(0, i)
            assert(tested).isEqualTo(tested.toBase64().decodeBase64())
            assert(tested).isEqualTo(tested.toBase64UrlSafe().decodeBase64())
        }
    }
}