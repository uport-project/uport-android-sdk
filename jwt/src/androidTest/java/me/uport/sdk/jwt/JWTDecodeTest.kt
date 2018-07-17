package me.uport.sdk.jwt

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.assertFailsWith


class JWTDecodeTest {
    private val validShareReqToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMDM2NjQzMiwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vY2hhc3F1aS51cG9ydC5tZS9hcGkvdjEvdG9waWMvWG5IZnlldjUxeHNka0R0dSIsIm5ldCI6IjB4NCIsImV4cCI6MTUyMDM2NzAzMiwidHlwZSI6InNoYXJlUmVxIn0.C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"
    private val validTokenHeader = validShareReqToken.split('.')[0]
    private val validShareReqTokenPayload = validShareReqToken.split('.')[1]
    private val validShareReqTokenSignature = validShareReqToken.split('.')[2]

    private val validVerificationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIzNHdqc3h3dmR1YW5vN05GQzh1ak5KbkZqYmFjZ1llV0E4bSIsImlhdCI6MTQ4NTMyMTEzMywiY2xhaW1zIjp7Im5hbWUiOiJCb2IiLCJnZW5kZXIiOiJtYWxlIn0sImV4cCI6MTQ4NTQwNzUzM30.orxkk0gzk0URvAkMM2vNzgW7IRefDCKhfyM9oP4Ye3GhuXko0h4TDMggslS_eIETqrRAqfG4XmcHIX9C-S8DoA"
    private val validVerificationTokenPayload = validVerificationToken.split('.')[1]

    private val invalidTokenOnlyHeader = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ."
    private val invalidTokenEmptyPayload = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ..C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"
    private val invalidTokenEmptyHeader = ".eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMDM2NjQzMiwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vY2hhc3F1aS51cG9ydC5tZS9hcGkvdjEvdG9waWMvWG5IZnlldjUxeHNka0R0dSIsIm5ldCI6IjB4NCIsImV4cCI6MTUyMDM2NzAzMiwidHlwZSI6InNoYXJlUmVxIn0.C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"

    @Test
    fun splitCompleteToken() {
        val parts = splitToken(validShareReqToken)
        val expected = SplitEncodedToken(
                validTokenHeader,
                validShareReqTokenPayload,
                validShareReqTokenSignature)

        assertEquals(expected, parts)
    }

    @Test
    fun splitEmptyToken() {
        assertFails("Token must have 3 parts: Header, Payload, and Signature") {
            splitToken("")
        }
    }

    @Test
    fun splitIncompleteToken() {
        assertFails("Token must have 3 parts: Header, Payload, and signature") {
            splitToken(invalidTokenOnlyHeader)
        }
    }

    @Test
    fun decodesCompleteToken() {
        val (header, payload) = JWTTools().decode(validShareReqToken)
        assertEquals("JWT", header.typ)
        assertEquals("2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG", payload.iss)
        assertEquals("name", payload.requested!![0])
    }

    @Test
    fun decodesVerificationToken() {
        val (header, payload) = JWTTools().decode(validVerificationToken)
        val nameClaim = mapOf("name" to "Bob", "gender" to "male")
        assertEquals("JWT", header.typ)
        assertEquals(nameClaim, payload.claims)
        assertEquals("34wjsxwvduano7NFC8ujNJnFjbacgYeWA8m", payload.iss)
    }

    @Test
    fun decodesIncompleteToken() {
        assertFailsWith<InvalidJWTException>("JWT Payload cannot be empty") {
            JWTTools().decode((invalidTokenEmptyPayload))
        }

        assertFailsWith<InvalidJWTException>("JWT Headder cannot be empty") {
            JWTTools().decode((invalidTokenEmptyHeader))
        }
    }

    @Test
    fun decodesRandomStuff() {
        assertFailsWith<InvalidJWTException>("Invalid JSON format") {
            JWTTools().decode("blahhh.blahhh.blahhh")
        }

        assertFailsWith<InvalidJWTException>("Invalid JSON format") {
            JWTTools().decode("$validTokenHeader.blahhh.blahhh")
        }

        assertFailsWith<InvalidJWTException>("Invalid JSON format") {
            JWTTools().decode("blahhh.$validShareReqTokenPayload.blahhh")
        }

    }

}