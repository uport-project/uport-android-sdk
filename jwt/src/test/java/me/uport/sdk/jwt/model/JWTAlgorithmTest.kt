package me.uport.sdk.jwt.model

import org.junit.Test

import org.junit.Assert.*

class JWTAlgorithmTest {

    @Test
    fun `can encode to json`() {
        val result = JwtHeader("hello", "world").toJson()
        //language=JSON
        assertEquals("""{"typ":"hello","alg":"world"}""", result)
    }

    @Test
    fun `can decode json`() {
        //language=JSON
        val result = JwtHeader.fromJson("""{"typ":"hello","alg":"world"}""")
        assertNotNull(result)
    }

    @Test
    fun `can decode lenient json`() {
        val result = JwtHeader.fromJson("""{typ:"hello",alg:"world"}""")
        assertNotNull(result)
    }
}