package me.uport.sdk.transport

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ResponseParserTest {

    private val valid = listOf(
            "https://example.com#access_token=header.payload.signature",
            "https://example.com#access_token=header.payload.signature&something=else",
            "https://example.com#something=else&access_token=header.payload.signature",
            "https://example.com?something=else#access_token=header.payload.signature",
            "https://example.com/some/path?something=else#access_token=header.payload.signature",
            "https://uport-project.github.io/uport-android-sdk#access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1NDUxNDgzOTQsImV4cCI6MTU0NTIzNDc5NCwiYXVkIjoiZGlkOmV0aHI6MHhjZjAzZGQwYTg5NGVmNzljYjViNjAxYTQzYzRiMjVlM2FlNGM2N2VkIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvazFBV1F2QXZuMlluSzhCVDlRWnNVNk1TUVpKWEx5eTNLIiwib3duIjp7Im5hbWUiOiJHaWdlbCBEZWMgMTgifSwicmVxIjoiZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKRlV6STFOa3N0VWlKOS5leUpqWVd4c1ltRmpheUk2SW1oMGRIQnpPaTh2ZFhCdmNuUXRjSEp2YW1WamRDNW5hWFJvZFdJdWFXOHZkWEJ2Y25RdFlXNWtjbTlwWkMxelpHc2lMQ0p5WlhGMVpYTjBaV1FpT2xzaWJtRnRaU0lzSW1OdmRXNTBjbmtpWFN3aVlXTjBJam9pWjJWdVpYSmhiQ0lzSW5SNWNHVWlPaUp6YUdGeVpWSmxjU0lzSW1saGRDSTZNVFUwTlRFME9ETTNPQ3dpWlhod0lqb3hOVFExTVRRNE9UYzRMQ0pwYzNNaU9pSmthV1E2WlhSb2Nqb3dlR05tTUROa1pEQmhPRGswWldZM09XTmlOV0kyTURGaE5ETmpOR0l5TldVellXVTBZelkzWldRaWZRLjFUVjVnM21XTlRMMy03Q2lBdDM0X2V5TjlGOFJGZzZRejBMZlJwTzhkeHFMYzVmV0E3NDRwdEFmbWhOWGdvbmc5WTBCaFJpVTd4T2s1N1VYOVZkckVnQSIsImlzcyI6ImRpZDpldGhyOjB4ZjMwYzBjMjc5YTYyODlmNzBmZjZkZGJkMmQ0MmU1MWRkOTZiMTk2YSJ9._OGoEc-xefBq88A10J6An2PaNpdhsveg793lp3Br1yX7-w46e7w7y9vLKGbN32XsJGqJ7t_fy81kG04jMXCCFgE",
            "myapp:my-dapp.com#access_token=header.payload.signature",
            "myapp:my-dapp.com#access_token=header.payload.signature&something=else",
            "myapp:my-dapp.com#something=else&access_token=header.payload.signature",
            "myapp:my-dapp.com?something=else#access_token=header.payload.signature",
            "myapp:my-dapp.com/some/path?something=else#access_token=header.payload.signature",
            "my.app://my-dapp.com#access_token=header.payload.signature",
            "my.app://my-dapp.com#access_token=header.payload.signature&something=else",
            "my.app://my-dapp.com#something=else&access_token=header.payload.signature",
            "my.app://my-dapp.com?something=else#access_token=header.payload.signature",
            "my.app://my-dapp.com/some/path?something=else#access_token=header.payload.signature"
    )

    private val invalid = listOf(
            "my.app://my-dapp.com#access_token=header.payload.signature#degenerate-fragment",
            "my.app://my-dapp.com?access_token=header.payload.signature",
            "my.app://my-dapp.com#access_token=header.payload",
            "my.app://my-dapp.com#access_token=",
            "access_token=header.payload.signature",
            ""
    )

    @Test
    fun `parse jwt from URL`() {
        valid.forEach { redirect ->
            val token = ResponseParser.extractTokenFromRedirectUri(redirect)
            println(token)
            assertNotNull(token)
        }
    }

    @Test
    fun `reject invalid URIs`() {
        invalid.forEach { redirect ->
            val token = ResponseParser.extractTokenFromRedirectUri(redirect)
            assertNull("expected parsing to fail at url: $redirect", token)
        }
    }

}