package me.uport.sdk.jwt

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.uport.sdk.signer.KPSigner
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.ITimeProvider
import me.uport.sdk.jwt.model.JwtPayload
import org.junit.Test

class JWTToolsJVMTest {

    private val tokens = listOf(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1MzUwMTY3MDIsImV4cCI6MTUzNTEwMzEwMiwiYXVkIjoiZGlkOmV0aHI6MHhhOWUzMjMyYjYxYmRiNjcyNzEyYjlhZTMzMTk1MDY5ZDhkNjUxYzFhIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvZHpqVGFpOFJvNFYzS3hrbTNTblppdjlXU1l1Tm9aNEFoIiwib3duIjp7Im5hbWUiOiJ1UG9ydCBVc2VyIn0sInJlcSI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSkZVekkxTmtzdFVpSjkuZXlKcFlYUWlPakUxTXpVd01UWTJPREVzSW1WNGNDSTZNVFV6TlRBeE56STRNU3dpY21WeGRXVnpkR1ZrSWpwYkltNWhiV1VpTENKd2FHOXVaU0lzSW1OdmRXNTBjbmtpWFN3aWNHVnliV2x6YzJsdmJuTWlPbHNpYm05MGFXWnBZMkYwYVc5dWN5SmRMQ0pqWVd4c1ltRmpheUk2SW1oMGRIQnpPaTh2WTJoaGMzRjFhUzUxY0c5eWRDNXRaUzloY0drdmRqRXZkRzl3YVdNdmJVVXpTbVpXZWxOMFNuUnFhbnBvWWpSYVRFRnhkeUlzSW1GamRDSTZJbXRsZVhCaGFYSWlMQ0owZVhCbElqb2ljMmhoY21WU1pYRWlMQ0pwYzNNaU9pSmthV1E2WlhSb2Nqb3dlR0U1WlRNeU16SmlOakZpWkdJMk56STNNVEppT1dGbE16TXhPVFV3Tmpsa09HUTJOVEZqTVdFaWZRLnVScUdGd01XNnpWSDR4OWFmTDAtS29qSEYwVF9GbW9QWnR6OG5uSjRFXzhNY2cxejBBZ21aMnplOE5iS05wVUNnRHRwTU9RNzVGSjU4WmhzbWFxQUxBRSIsImNhcGFiaWxpdGllcyI6WyJleUowZVhBaU9pSktWMVFpTENKaGJHY2lPaUpGVXpJMU5rc3RVaUo5LmV5SnBZWFFpT2pFMU16VXdNVFkzTURFc0ltVjRjQ0k2TVRVek5qTXhNamN3TVN3aVlYVmtJam9pWkdsa09tVjBhSEk2TUhoaE9XVXpNak15WWpZeFltUmlOamN5TnpFeVlqbGhaVE16TVRrMU1EWTVaRGhrTmpVeFl6RmhJaXdpZEhsd1pTSTZJbTV2ZEdsbWFXTmhkR2x2Ym5NaUxDSjJZV3gxWlNJNkltRnlianBoZDNNNmMyNXpPblZ6TFhkbGMzUXRNam94TVRNeE9UWXlNVFkxTlRnNlpXNWtjRzlwYm5RdlIwTk5MM1ZRYjNKMEx6UXpNRGsxTWpZMkxUSmhPR1F0TTJFMFpTMWlaRFV3TFRka01USm1ZVE00TWpRNFlpSXNJbWx6Y3lJNkltUnBaRHBsZEdoeU9qQjRNVEE0TWpBNVpqUXlORGRpTjJabE5qWXdOV0l3WmpVNFpqa3hORFZsWXpNeU5qbGtNREUxTkNKOS5Lc0F6TmVDeHFDaF9rMkt4aTYtWHFveFNXZjBCLWFFR0xXdi1ldHVXQlF2QU5neDFTMG5oZ0ppRkllUnRXakw4ekdnVVV3MUlsSWJtYUZrOEo5aGdhd0UiXSwiYm94UHViIjoiY2g3aGI2S3hsakJ2bXh5UDJXZENWTFNTLzQ2S1hCcmdkWG1Mcm03VEpIST0iLCJpc3MiOiJkaWQ6ZXRocjoweDEwODIwOWY0MjQ3YjdmZTY2MDViMGY1OGY5MTQ1ZWMzMjY5ZDAxNTQifQ.Ncf8B_y0Ha8gdaYyCaL5jLX2RsKTMwxTQ8KlybXFygsxKUUQm9OXo4lU65fduIaFvVyPOP6Oe2adar8m0m2aiwA",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1MzUwMTY1OTcsImV4cCI6MTUzNTEwMjk5NywiYXVkIjoiZGlkOmV0aHI6MHhhOWUzMjMyYjYxYmRiNjcyNzEyYjlhZTMzMTk1MDY5ZDhkNjUxYzFhIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvd2hGdGRtc0VVNVVWMVNCbld0RnZZcHlUcjNqNHd5TmR2Iiwib3duIjp7Im5hbWUiOiJ1UG9ydCBVc2VyIn0sInJlcSI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSkZVekkxTmtzdFVpSjkuZXlKcFlYUWlPakUxTXpVd01UWTFPRGdzSW1WNGNDSTZNVFV6TlRBeE56RTRPQ3dpY21WeGRXVnpkR1ZrSWpwYkltNWhiV1VpTENKd2FHOXVaU0lzSW1OdmRXNTBjbmtpWFN3aWNHVnliV2x6YzJsdmJuTWlPbHNpYm05MGFXWnBZMkYwYVc5dWN5SmRMQ0pqWVd4c1ltRmpheUk2SW1oMGRIQnpPaTh2WTJoaGMzRjFhUzUxY0c5eWRDNXRaUzloY0drdmRqRXZkRzl3YVdNdldtMXNOa2xuUWsxYU9XUXhWbGgwV0ZsYVJUTlBkeUlzSW1GamRDSTZJbXRsZVhCaGFYSWlMQ0owZVhCbElqb2ljMmhoY21WU1pYRWlMQ0pwYzNNaU9pSmthV1E2WlhSb2Nqb3dlR0U1WlRNeU16SmlOakZpWkdJMk56STNNVEppT1dGbE16TXhPVFV3Tmpsa09HUTJOVEZqTVdFaWZRLnRNbWh6cjFkbER0YUhUeWUtVDAxOGp2N0NUYlRhVWY4ZzlhbHNxVWJ6VGpGUkFsbV9qZ2RaR3pVVEVzeGtBY0ZmVy1ZSmpIVGtwSmtjNFNWREc3REJBQSIsImlzcyI6ImRpZDpldGhyOjB4ZThjOTFiZGU3NjI1YWIyYzBlZDlmMjE0ZGViMzk0NDBkYTdlMDNjNCJ9.-04Z_m2kgFBwF1Elh3jmv1_44jdGjEczf4x3c5Z4TxwiMP8nXZsIDVgsp3PS34DPGfpR4OkZ6LBozBBER3TABAA"
    )

    @Test
    fun `verifies simple tokens`() = runBlocking {

        tokens.forEach { token ->
            val payload = JWTTools(TestTimeProvider(1535102500000L)).verify(token)
            assert(payload).isNotNull()
        }
    }

    private val validShareReqToken1 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMDM2NjQzMiwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vY2hhc3F1aS51cG9ydC5tZS9hcGkvdjEvdG9waWMvWG5IZnlldjUxeHNka0R0dSIsIm5ldCI6IjB4NCIsImV4cCI6MTUyMDM2NzAzMiwidHlwZSI6InNoYXJlUmVxIn0.C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"
    private val expectedShareReqPayload1 = JwtPayload(iss = "2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG", iat = 1520366432, sub = null, aud = null, exp = 1520367032, callback = "https://chasqui.uport.me/api/v1/topic/XnHfyev51xsdkDtu", type = "shareReq", net = "0x4", act = null, requested = listOf("name", "phone", "country", "avatar"), verified = null, permissions = listOf("notifications"), req = null, nad = null, dad = null, own = null, capabilities = null, claims = null, ctl = null, reg = null, rel = null, fct = null, acc = null)

    private val incomingJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb21SSlpMMjNaQ1lnYzFyWnJGVnBGWEpwV29hRUV1SlVjZiIsImlhdCI6MTUxOTM1MDI1NiwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vYXBpLnVwb3J0LnNwYWNlL29sb3J1bi9jcmVhdGVJZGVudGl0eSIsIm5ldCI6IjB4MzAzOSIsImFjdCI6ImRldmljZWtleSIsImV4cCI6MTUyMjU0MDgwMCwidHlwZSI6InNoYXJlUmVxIn0.EkqNUyrZhcDbTQl73XpL2tp470lCo2saMXzuOZ91UI2y-XzpcBMzhhSeUORnoJXJhHnkGGpshZlESWUgrbuiVQ"
    private val expectedJwtPayload = JwtPayload(iss = "2omRJZL23ZCYgc1rZrFVpFXJpWoaEEuJUcf", iat = 1519350256, sub = null, aud = null, exp = 1522540800, callback = "https://api.uport.space/olorun/createIdentity", type = "shareReq", net = "0x3039", act = "devicekey", requested = null, verified = null, permissions = listOf("notifications"), req = null, nad = null, dad = null, own = null, capabilities = null, claims = null, ctl = null, reg = null, rel = null, fct = null, acc = null)

    @Test
    fun `returns correct payload after successful verification`() = runBlocking {
        val shareReqPayload = JWTTools(TestTimeProvider(1520366666000L)).verify(validShareReqToken1)
        assert(shareReqPayload).isEqualTo(expectedShareReqPayload1)

        val incomingJwtPayload = JWTTools(TestTimeProvider(1522540300000L)).verify(incomingJwt)
        assert(incomingJwtPayload).isEqualTo(expectedJwtPayload)
    }

    @Test
    fun `throws when given an unknown algorithm to create tokens`() {
        val tested = JWTTools()

        val payload = emptyMap<String, Any>()
        val signer = KPSigner("0x1234")
        val issuerDID = "did:ethr:${signer.getAddress()}"

        coAssert {
            tested.createJWT(payload, issuerDID, signer, algorithm = "some fancy but unknown algorithm")
        }.thrownError {
            isInstanceOf(JWTEncodingException::class)
        }
    }


    @Test
    fun `throws when a token is issued in the future`() {
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJkaWQ6ZXRocjoweGE5ZTMyMzJiNjFiZGI2NzI3MTJiOWFlMzMxOTUwNjlkOGQ2NTFjMWEiLCJpYXQiOjE1NDU1Njk1NDEsImV4cCI6MTU0NjA4Nzk0MSwiYXVkIjoiZGlkOmV0aHI6MHgxMDgyMDlmNDI0N2I3ZmU2NjA1YjBmNThmOTE0NWVjMzI2OWQwMTU0Iiwic3ViIjoiIn0.Bt9Frc1QabJfpXYBoU4sns8WPeRLdKU87FncgMFq1lY"

        coAssert {
            JWTTools(TestTimeProvider(977317692000L)).verify(token)
        }.thrownError {
            isInstanceOf(InvalidJWTException::class)
        }
    }

    @Test
    fun `throws when a token is expired`() {
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJkaWQ6ZXRocjoweGE5ZTMyMzJiNjFiZGI2NzI3MTJiOWFlMzMxOTUwNjlkOGQ2NTFjMWEiLCJpYXQiOjE1NDU1Njk1NDEsImV4cCI6MTU0NjA4Nzk0MSwiYXVkIjoiZGlkOmV0aHI6MHgxMDgyMDlmNDI0N2I3ZmU2NjA1YjBmNThmOTE0NWVjMzI2OWQwMTU0Iiwic3ViIjoiIn0.Bt9Frc1QabJfpXYBoU4sns8WPeRLdKU87FncgMFq1lY"

        coAssert {
            JWTTools(TestTimeProvider(1576847292000L)).verify(token)
        }.thrownError {
            isInstanceOf(InvalidJWTException::class)
        }
    }

    @Test
    fun `throws when the signature does not match any public keys belonging to the issuer`() {

        // JWT token with a Signer that doesn't have anything to do with the issuerDID.
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1NDY4NTkxNTksImV4cCI6MTg2MjIxOTE1OSwiaXNzIjoiZGlkOmV0aHI6MHg2OTg1YTExMGRmMzc1NTUyMzVkN2QwZGUwYTBmYjI4Yzk4NDhkZmE5In0.fe1rvAHsoJsJzwSFAmVFTz9uxhncNY65jpbb2cS9jcY08xphpU3rOy1N85_IbEjhIZw-FrPeFgxJLoDLw6itcgE"

        coAssert {
            JWTTools(TestTimeProvider(1547818630000L)).verify(token)
        }.thrownError {
            isInstanceOf(InvalidJWTException::class)
        }
    }

    class TestTimeProvider(private val currentTime: Long) : ITimeProvider {
        override fun now(): Long = currentTime
    }
}


