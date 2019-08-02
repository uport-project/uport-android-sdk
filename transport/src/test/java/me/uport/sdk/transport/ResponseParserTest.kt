package me.uport.sdk.transport

import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import org.junit.Test

class ResponseParserTest {

    private val validAccessTokenTypeURLs = listOf(
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

    private val invalidAccessTokenTypeURLs = listOf(
            "my.app://my-dapp.com#access_token=header.payload.signature#degenerate-fragment",
            "my.app://my-dapp.com?access_token=header.payload.signature",
            "my.app://my-dapp.com#access_token=header.payload",
            "my.app://my-dapp.com#access_token=",
            "access_token=header.payload.signature",
            ""
    )

    @Test
    fun `parse jwt from URL with access_token key`() {
        validAccessTokenTypeURLs.forEach { redirect ->
            val token = ResponseParser.extractTokenFromRedirectUri(redirect)
            assertThat(token).isNotNull()
        }
    }

    @Test
    fun `reject invalid URIs with access_token key`() {
        invalidAccessTokenTypeURLs.forEach { redirect ->
            assertThat {
                ResponseParser.extractTokenFromRedirectUri(redirect)
            }.thrownError {
                isInstanceOf(IllegalArgumentException::class)
            }
        }
    }


    private val validVerificationTypeURLs = listOf(
            "https://example.com#verification=header.payload.signature",
            "https://example.com#verification=header.payload.signature&something=else",
            "https://example.com#something=else&verification=header.payload.signature",
            "https://example.com?something=else#verification=header.payload.signature",
            "https://example.com/some/path?something=else#verification=header.payload.signature",
            "https://uport-project.github.io/uport-android-sdk#verification=eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1NDUxNDgzOTQsImV4cCI6MTU0NTIzNDc5NCwiYXVkIjoiZGlkOmV0aHI6MHhjZjAzZGQwYTg5NGVmNzljYjViNjAxYTQzYzRiMjVlM2FlNGM2N2VkIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvazFBV1F2QXZuMlluSzhCVDlRWnNVNk1TUVpKWEx5eTNLIiwib3duIjp7Im5hbWUiOiJHaWdlbCBEZWMgMTgifSwicmVxIjoiZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKRlV6STFOa3N0VWlKOS5leUpqWVd4c1ltRmpheUk2SW1oMGRIQnpPaTh2ZFhCdmNuUXRjSEp2YW1WamRDNW5hWFJvZFdJdWFXOHZkWEJ2Y25RdFlXNWtjbTlwWkMxelpHc2lMQ0p5WlhGMVpYTjBaV1FpT2xzaWJtRnRaU0lzSW1OdmRXNTBjbmtpWFN3aVlXTjBJam9pWjJWdVpYSmhiQ0lzSW5SNWNHVWlPaUp6YUdGeVpWSmxjU0lzSW1saGRDSTZNVFUwTlRFME9ETTNPQ3dpWlhod0lqb3hOVFExTVRRNE9UYzRMQ0pwYzNNaU9pSmthV1E2WlhSb2Nqb3dlR05tTUROa1pEQmhPRGswWldZM09XTmlOV0kyTURGaE5ETmpOR0l5TldVellXVTBZelkzWldRaWZRLjFUVjVnM21XTlRMMy03Q2lBdDM0X2V5TjlGOFJGZzZRejBMZlJwTzhkeHFMYzVmV0E3NDRwdEFmbWhOWGdvbmc5WTBCaFJpVTd4T2s1N1VYOVZkckVnQSIsImlzcyI6ImRpZDpldGhyOjB4ZjMwYzBjMjc5YTYyODlmNzBmZjZkZGJkMmQ0MmU1MWRkOTZiMTk2YSJ9._OGoEc-xefBq88A10J6An2PaNpdhsveg793lp3Br1yX7-w46e7w7y9vLKGbN32XsJGqJ7t_fy81kG04jMXCCFgE",
            "myapp:my-dapp.com#verification=header.payload.signature",
            "myapp:my-dapp.com#verification=header.payload.signature&something=else",
            "myapp:my-dapp.com#something=else&verification=header.payload.signature",
            "myapp:my-dapp.com?something=else#verification=header.payload.signature",
            "myapp:my-dapp.com/some/path?something=else#verification=header.payload.signature",
            "my.app://my-dapp.com#verification=header.payload.signature",
            "my.app://my-dapp.com#verification=header.payload.signature&something=else",
            "my.app://my-dapp.com#something=else&verification=header.payload.signature",
            "my.app://my-dapp.com?something=else#verification=header.payload.signature",
            "my.app://my-dapp.com/some/path?something=else#verification=header.payload.signature"
    )

    private val invalidVerificationTypeURLs = listOf(
            "my.app://my-dapp.com#verification=header.payload.signature#degenerate-fragment",
            "my.app://my-dapp.com?verification=header.payload.signature",
            "my.app://my-dapp.com#verification=header.payload",
            "my.app://my-dapp.com#verification=",
            "verification=header.payload.signature",
            ""
    )

    @Test
    fun `parse jwt from URL with verification key`() {
        validVerificationTypeURLs.forEach { redirect ->
            val token = ResponseParser.extractTokenFromRedirectUri(redirect)
            assertThat(token).isNotNull()
        }
    }

    @Test
    fun `reject invalid URIs with verification key`() {
        invalidVerificationTypeURLs.forEach { redirect ->
            assertThat {
                ResponseParser.extractTokenFromRedirectUri(redirect)
            }.thrownError {
                isInstanceOf(IllegalArgumentException::class)
            }
        }
    }


    private val validTypedDataSigTypeURLs = listOf(
            "https://example.com#typedDataSig=header.payload.signature",
            "https://example.com#typedDataSig=header.payload.signature&something=else",
            "https://example.com#something=else&typedDataSig=header.payload.signature",
            "https://example.com?something=else#typedDataSig=header.payload.signature",
            "https://example.com/some/path?something=else#typedDataSig=header.payload.signature",
            "https://uport-project.github.io/uport-android-sdk#typedDataSig=eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1NDUxNDgzOTQsImV4cCI6MTU0NTIzNDc5NCwiYXVkIjoiZGlkOmV0aHI6MHhjZjAzZGQwYTg5NGVmNzljYjViNjAxYTQzYzRiMjVlM2FlNGM2N2VkIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvazFBV1F2QXZuMlluSzhCVDlRWnNVNk1TUVpKWEx5eTNLIiwib3duIjp7Im5hbWUiOiJHaWdlbCBEZWMgMTgifSwicmVxIjoiZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKRlV6STFOa3N0VWlKOS5leUpqWVd4c1ltRmpheUk2SW1oMGRIQnpPaTh2ZFhCdmNuUXRjSEp2YW1WamRDNW5hWFJvZFdJdWFXOHZkWEJ2Y25RdFlXNWtjbTlwWkMxelpHc2lMQ0p5WlhGMVpYTjBaV1FpT2xzaWJtRnRaU0lzSW1OdmRXNTBjbmtpWFN3aVlXTjBJam9pWjJWdVpYSmhiQ0lzSW5SNWNHVWlPaUp6YUdGeVpWSmxjU0lzSW1saGRDSTZNVFUwTlRFME9ETTNPQ3dpWlhod0lqb3hOVFExTVRRNE9UYzRMQ0pwYzNNaU9pSmthV1E2WlhSb2Nqb3dlR05tTUROa1pEQmhPRGswWldZM09XTmlOV0kyTURGaE5ETmpOR0l5TldVellXVTBZelkzWldRaWZRLjFUVjVnM21XTlRMMy03Q2lBdDM0X2V5TjlGOFJGZzZRejBMZlJwTzhkeHFMYzVmV0E3NDRwdEFmbWhOWGdvbmc5WTBCaFJpVTd4T2s1N1VYOVZkckVnQSIsImlzcyI6ImRpZDpldGhyOjB4ZjMwYzBjMjc5YTYyODlmNzBmZjZkZGJkMmQ0MmU1MWRkOTZiMTk2YSJ9._OGoEc-xefBq88A10J6An2PaNpdhsveg793lp3Br1yX7-w46e7w7y9vLKGbN32XsJGqJ7t_fy81kG04jMXCCFgE",
            "myapp:my-dapp.com#typedDataSig=header.payload.signature",
            "myapp:my-dapp.com#typedDataSig=header.payload.signature&something=else",
            "myapp:my-dapp.com#something=else&typedDataSig=header.payload.signature",
            "myapp:my-dapp.com?something=else#typedDataSig=header.payload.signature",
            "myapp:my-dapp.com/some/path?something=else#typedDataSig=header.payload.signature",
            "my.app://my-dapp.com#typedDataSig=header.payload.signature",
            "my.app://my-dapp.com#typedDataSig=header.payload.signature&something=else",
            "my.app://my-dapp.com#something=else&typedDataSig=header.payload.signature",
            "my.app://my-dapp.com?something=else#typedDataSig=header.payload.signature",
            "my.app://my-dapp.com/some/path?something=else#typedDataSig=header.payload.signature"
    )

    private val invalidTypedDataSigTypeURLs = listOf(
            "my.app://my-dapp.com#typedDataSig=header.payload.signature#degenerate-fragment",
            "my.app://my-dapp.com?typedDataSig=header.payload.signature",
            "my.app://my-dapp.com#typedDataSig=header.payload",
            "my.app://my-dapp.com#typedDataSig=",
            "typedDataSig=header.payload.signature",
            ""
    )

    @Test
    fun `parse jwt from URL with typedDataSig key`() {
        validTypedDataSigTypeURLs.forEach { redirect ->
            val token = ResponseParser.extractTokenFromRedirectUri(redirect)
            assertThat(token).isNotNull()
        }
    }

    @Test
    fun `reject invalid URIs with typedDataSig key`() {
        invalidTypedDataSigTypeURLs.forEach { redirect ->
            assertThat {
                ResponseParser.extractTokenFromRedirectUri(redirect)
            }.thrownError {
                isInstanceOf(IllegalArgumentException::class)
            }
        }
    }


    private val validPersonalSigTypeURLs = listOf(
            "https://example.com#personalSig=header.payload.signature",
            "https://example.com#personalSig=header.payload.signature&something=else",
            "https://example.com#something=else&personalSig=header.payload.signature",
            "https://example.com?something=else#personalSig=header.payload.signature",
            "https://example.com/some/path?something=else#personalSig=header.payload.signature",
            "https://uport-project.github.io/uport-android-sdk#personalSig=eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1NDUxNDgzOTQsImV4cCI6MTU0NTIzNDc5NCwiYXVkIjoiZGlkOmV0aHI6MHhjZjAzZGQwYTg5NGVmNzljYjViNjAxYTQzYzRiMjVlM2FlNGM2N2VkIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvazFBV1F2QXZuMlluSzhCVDlRWnNVNk1TUVpKWEx5eTNLIiwib3duIjp7Im5hbWUiOiJHaWdlbCBEZWMgMTgifSwicmVxIjoiZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKRlV6STFOa3N0VWlKOS5leUpqWVd4c1ltRmpheUk2SW1oMGRIQnpPaTh2ZFhCdmNuUXRjSEp2YW1WamRDNW5hWFJvZFdJdWFXOHZkWEJ2Y25RdFlXNWtjbTlwWkMxelpHc2lMQ0p5WlhGMVpYTjBaV1FpT2xzaWJtRnRaU0lzSW1OdmRXNTBjbmtpWFN3aVlXTjBJam9pWjJWdVpYSmhiQ0lzSW5SNWNHVWlPaUp6YUdGeVpWSmxjU0lzSW1saGRDSTZNVFUwTlRFME9ETTNPQ3dpWlhod0lqb3hOVFExTVRRNE9UYzRMQ0pwYzNNaU9pSmthV1E2WlhSb2Nqb3dlR05tTUROa1pEQmhPRGswWldZM09XTmlOV0kyTURGaE5ETmpOR0l5TldVellXVTBZelkzWldRaWZRLjFUVjVnM21XTlRMMy03Q2lBdDM0X2V5TjlGOFJGZzZRejBMZlJwTzhkeHFMYzVmV0E3NDRwdEFmbWhOWGdvbmc5WTBCaFJpVTd4T2s1N1VYOVZkckVnQSIsImlzcyI6ImRpZDpldGhyOjB4ZjMwYzBjMjc5YTYyODlmNzBmZjZkZGJkMmQ0MmU1MWRkOTZiMTk2YSJ9._OGoEc-xefBq88A10J6An2PaNpdhsveg793lp3Br1yX7-w46e7w7y9vLKGbN32XsJGqJ7t_fy81kG04jMXCCFgE",
            "myapp:my-dapp.com#personalSig=header.payload.signature",
            "myapp:my-dapp.com#personalSig=header.payload.signature&something=else",
            "myapp:my-dapp.com#something=else&personalSig=header.payload.signature",
            "myapp:my-dapp.com?something=else#personalSig=header.payload.signature",
            "myapp:my-dapp.com/some/path?something=else#personalSig=header.payload.signature",
            "my.app://my-dapp.com#personalSig=header.payload.signature",
            "my.app://my-dapp.com#personalSig=header.payload.signature&something=else",
            "my.app://my-dapp.com#something=else&personalSig=header.payload.signature",
            "my.app://my-dapp.com?something=else#personalSig=header.payload.signature",
            "my.app://my-dapp.com/some/path?something=else#personalSig=header.payload.signature"
    )

    private val invalidPersonalSigTypeURLs = listOf(
            "my.app://my-dapp.com#personalSig=header.payload.signature#degenerate-fragment",
            "my.app://my-dapp.com?personalSig=header.payload.signature",
            "my.app://my-dapp.com#personalSig=header.payload",
            "my.app://my-dapp.com#personalSig=",
            "personalSig=header.payload.signature",
            ""
    )

    @Test
    fun `parse jwt from URL with personalSig key`() {
        validPersonalSigTypeURLs.forEach { redirect ->
            val token = ResponseParser.extractTokenFromRedirectUri(redirect)
            assertThat(token).isNotNull()
        }
    }

    @Test
    fun `reject invalid URIs with personalSig key`() {
        invalidPersonalSigTypeURLs.forEach { redirect ->
            assertThat {
                ResponseParser.extractTokenFromRedirectUri(redirect)
            }.thrownError {
                isInstanceOf(IllegalArgumentException::class)
            }
        }
    }


    private val validTxTypeURLs = listOf(
            "https://example.com#tx=0x537597ce1ca672ad0316c9d298f2af91084316e6d9aedbdb7747ec2a0017a3e8",
            "https://example.com#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0&something=else",
            "https://example.com#something=else&tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "https://example.com?something=else#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "https://example.com/some/path?something=else#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "myapp:my-dapp.com#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "myapp:my-dapp.com#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0&something=else",
            "myapp:my-dapp.com#something=else&tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "myapp:my-dapp.com?something=else#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "myapp:my-dapp.com/some/path?something=else#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "my.app://my-dapp.com#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "my.app://my-dapp.com#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0&something=else",
            "my.app://my-dapp.com#something=else&tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "my.app://my-dapp.com?something=else#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0",
            "my.app://my-dapp.com/some/path?something=else#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0"
    )

    private val invalidTxTypeURLs = listOf(
            "my.app://my-dapp.com#tx=0x537597ce1ca672ad02af91084316e6d9aedbdb7747ec2a0017a3e8#degenerate-fragment",
            "my.app://my-dapp.com?tx=header.payload.signature",
            "my.app://my-dapp.com#tx=0x537597ce1ca672ad0316c9d298f2af9mq84316e6d9aedbdb7747ec2a0017a3e8",
            "my.app://my-dapp.com#tx=537597ce1ca672ad0316c9dx8f2af91084316e6d9aedbdb7747ec2a0017a3e8",
            "my.app://my-dapp.com#tx=",
            "tx=header.payload.signature",
            ""
    )

    @Test
    fun `parse jwt from URL with tx key`() {
        validTxTypeURLs.forEach { redirect ->
            val token = ResponseParser.extractTokenFromRedirectUri(redirect)
            assertThat(token).isNotNull()
        }
    }

    @Test
    fun `reject invalid URIs with tx key`() {
        invalidTxTypeURLs.forEach { redirect ->
            assertThat {
                ResponseParser.extractTokenFromRedirectUri(redirect)
            }.thrownError {
                isInstanceOf(IllegalArgumentException::class)
            }
        }
    }
}