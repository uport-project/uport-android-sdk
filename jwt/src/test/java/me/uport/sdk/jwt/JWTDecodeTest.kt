package me.uport.sdk.jwt

import assertk.assert
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import org.junit.Test


class JWTDecodeTest {
    private val validShareReqToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMDM2NjQzMiwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vY2hhc3F1aS51cG9ydC5tZS9hcGkvdjEvdG9waWMvWG5IZnlldjUxeHNka0R0dSIsIm5ldCI6IjB4NCIsImV4cCI6MTUyMDM2NzAzMiwidHlwZSI6InNoYXJlUmVxIn0.C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"
    private val validTokenHeader = validShareReqToken.split('.')[0]
    private val validShareReqTokenPayload = validShareReqToken.split('.')[1]
    private val validShareReqTokenSignature = validShareReqToken.split('.')[2]

    private val validVerificationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIzNHdqc3h3dmR1YW5vN05GQzh1ak5KbkZqYmFjZ1llV0E4bSIsImlhdCI6MTQ4NTMyMTEzMywiY2xhaW1zIjp7Im5hbWUiOiJCb2IiLCJnZW5kZXIiOiJtYWxlIn0sImV4cCI6MTQ4NTQwNzUzM30.orxkk0gzk0URvAkMM2vNzgW7IRefDCKhfyM9oP4Ye3GhuXko0h4TDMggslS_eIETqrRAqfG4XmcHIX9C-S8DoA"

    private val invalidTokenOnlyHeader = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ."
    private val invalidTokenEmptyPayload = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ..C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"
    private val invalidTokenEmptyHeader = ".eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMDM2NjQzMiwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vY2hhc3F1aS51cG9ydC5tZS9hcGkvdjEvdG9waWMvWG5IZnlldjUxeHNka0R0dSIsIm5ldCI6IjB4NCIsImV4cCI6MTUyMDM2NzAzMiwidHlwZSI6InNoYXJlUmVxIn0.C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"

    @Test
    fun `can split complete token`() {
        val parts = splitToken(validShareReqToken)
        val expected = SplitEncodedToken(
                validTokenHeader,
                validShareReqTokenPayload,
                validShareReqTokenSignature)

        assert(parts).isEqualTo(expected)
    }

    @Test
    fun `throws when splitting empty token`() {
        assert { splitToken("") }
                .thrownError { isInstanceOf(IllegalArgumentException::class) }
    }

    @Test
    fun `throws when splitting incomplete token`() {
        assert { splitToken(invalidTokenOnlyHeader) }
                .thrownError { isInstanceOf(IllegalArgumentException::class) }
    }

    @Test
    fun `decodes complete token`() {
        val (header, payload) = JWTTools().decode(validShareReqToken)
        assert(header.typ).isEqualTo("JWT")
        assert(payload.iss).isEqualTo("2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG")
        assert(payload.requested!![0]).isEqualTo("name")
    }

    @Test
    fun `decodes token with claims`() {
        val (header, payload) = JWTTools().decode(validVerificationToken)
        val nameClaim = mapOf("name" to "Bob", "gender" to "male")
        assert(header.typ).isEqualTo("JWT")
        assert(payload.claims).isEqualTo(nameClaim)
        assert(payload.iss).isEqualTo("34wjsxwvduano7NFC8ujNJnFjbacgYeWA8m")
    }

    @Test
    fun `throws when decoding incomplete token`() {
        assert { JWTTools().decode((invalidTokenEmptyPayload)) }
                .thrownError {
                    isInstanceOf(InvalidJWTException::class)
                    hasMessage("Payload cannot be empty")
                }

        assert { JWTTools().decode((invalidTokenEmptyHeader)) }
                .thrownError {
                    isInstanceOf(InvalidJWTException::class)
                    hasMessage("Header cannot be empty")
                }
    }

    @Test
    fun `throws on random token parts`() {
        assert { JWTTools().decode("blahhh.blahhh.blahhh") }
                .thrownError {
                    isInstanceOf(InvalidJWTException::class)
                }

        assert { JWTTools().decode("$validTokenHeader.blahhh.blahhh") }
                .thrownError {
                    isInstanceOf(InvalidJWTException::class)
                }

        assert { JWTTools().decode("blahhh.$validShareReqTokenPayload.blahhh") }
                .thrownError {
                    isInstanceOf(InvalidJWTException::class)
                }

    }

}