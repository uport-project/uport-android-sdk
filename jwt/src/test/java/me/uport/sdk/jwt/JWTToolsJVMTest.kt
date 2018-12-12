package me.uport.sdk.jwt

import com.uport.sdk.signer.KPSigner
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.Networks
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDResolver
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class JWTToolsJVMTest {

    @Before
    fun `run before every test`() {
//        stubUiContext()
        //this initialization is needed until JWTTools has some defaults setup
        UniversalDID.registerResolver(UportDIDResolver())
        val defaultRPC = JsonRPC(Networks.rinkeby.rpcUrl)
        UniversalDID.registerResolver(EthrDIDResolver(defaultRPC))
    }


    private val tokens = listOf(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1MzUwMTY3MDIsImV4cCI6MTUzNTEwMzEwMiwiYXVkIjoiZGlkOmV0aHI6MHhhOWUzMjMyYjYxYmRiNjcyNzEyYjlhZTMzMTk1MDY5ZDhkNjUxYzFhIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvZHpqVGFpOFJvNFYzS3hrbTNTblppdjlXU1l1Tm9aNEFoIiwib3duIjp7Im5hbWUiOiJ1UG9ydCBVc2VyIn0sInJlcSI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSkZVekkxTmtzdFVpSjkuZXlKcFlYUWlPakUxTXpVd01UWTJPREVzSW1WNGNDSTZNVFV6TlRBeE56STRNU3dpY21WeGRXVnpkR1ZrSWpwYkltNWhiV1VpTENKd2FHOXVaU0lzSW1OdmRXNTBjbmtpWFN3aWNHVnliV2x6YzJsdmJuTWlPbHNpYm05MGFXWnBZMkYwYVc5dWN5SmRMQ0pqWVd4c1ltRmpheUk2SW1oMGRIQnpPaTh2WTJoaGMzRjFhUzUxY0c5eWRDNXRaUzloY0drdmRqRXZkRzl3YVdNdmJVVXpTbVpXZWxOMFNuUnFhbnBvWWpSYVRFRnhkeUlzSW1GamRDSTZJbXRsZVhCaGFYSWlMQ0owZVhCbElqb2ljMmhoY21WU1pYRWlMQ0pwYzNNaU9pSmthV1E2WlhSb2Nqb3dlR0U1WlRNeU16SmlOakZpWkdJMk56STNNVEppT1dGbE16TXhPVFV3Tmpsa09HUTJOVEZqTVdFaWZRLnVScUdGd01XNnpWSDR4OWFmTDAtS29qSEYwVF9GbW9QWnR6OG5uSjRFXzhNY2cxejBBZ21aMnplOE5iS05wVUNnRHRwTU9RNzVGSjU4WmhzbWFxQUxBRSIsImNhcGFiaWxpdGllcyI6WyJleUowZVhBaU9pSktWMVFpTENKaGJHY2lPaUpGVXpJMU5rc3RVaUo5LmV5SnBZWFFpT2pFMU16VXdNVFkzTURFc0ltVjRjQ0k2TVRVek5qTXhNamN3TVN3aVlYVmtJam9pWkdsa09tVjBhSEk2TUhoaE9XVXpNak15WWpZeFltUmlOamN5TnpFeVlqbGhaVE16TVRrMU1EWTVaRGhrTmpVeFl6RmhJaXdpZEhsd1pTSTZJbTV2ZEdsbWFXTmhkR2x2Ym5NaUxDSjJZV3gxWlNJNkltRnlianBoZDNNNmMyNXpPblZ6TFhkbGMzUXRNam94TVRNeE9UWXlNVFkxTlRnNlpXNWtjRzlwYm5RdlIwTk5MM1ZRYjNKMEx6UXpNRGsxTWpZMkxUSmhPR1F0TTJFMFpTMWlaRFV3TFRka01USm1ZVE00TWpRNFlpSXNJbWx6Y3lJNkltUnBaRHBsZEdoeU9qQjRNVEE0TWpBNVpqUXlORGRpTjJabE5qWXdOV0l3WmpVNFpqa3hORFZsWXpNeU5qbGtNREUxTkNKOS5Lc0F6TmVDeHFDaF9rMkt4aTYtWHFveFNXZjBCLWFFR0xXdi1ldHVXQlF2QU5neDFTMG5oZ0ppRkllUnRXakw4ekdnVVV3MUlsSWJtYUZrOEo5aGdhd0UiXSwiYm94UHViIjoiY2g3aGI2S3hsakJ2bXh5UDJXZENWTFNTLzQ2S1hCcmdkWG1Mcm03VEpIST0iLCJpc3MiOiJkaWQ6ZXRocjoweDEwODIwOWY0MjQ3YjdmZTY2MDViMGY1OGY5MTQ1ZWMzMjY5ZDAxNTQifQ.Ncf8B_y0Ha8gdaYyCaL5jLX2RsKTMwxTQ8KlybXFygsxKUUQm9OXo4lU65fduIaFvVyPOP6Oe2adar8m0m2aiwA",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1MzUwMTY1OTcsImV4cCI6MTUzNTEwMjk5NywiYXVkIjoiZGlkOmV0aHI6MHhhOWUzMjMyYjYxYmRiNjcyNzEyYjlhZTMzMTk1MDY5ZDhkNjUxYzFhIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvd2hGdGRtc0VVNVVWMVNCbld0RnZZcHlUcjNqNHd5TmR2Iiwib3duIjp7Im5hbWUiOiJ1UG9ydCBVc2VyIn0sInJlcSI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSkZVekkxTmtzdFVpSjkuZXlKcFlYUWlPakUxTXpVd01UWTFPRGdzSW1WNGNDSTZNVFV6TlRBeE56RTRPQ3dpY21WeGRXVnpkR1ZrSWpwYkltNWhiV1VpTENKd2FHOXVaU0lzSW1OdmRXNTBjbmtpWFN3aWNHVnliV2x6YzJsdmJuTWlPbHNpYm05MGFXWnBZMkYwYVc5dWN5SmRMQ0pqWVd4c1ltRmpheUk2SW1oMGRIQnpPaTh2WTJoaGMzRjFhUzUxY0c5eWRDNXRaUzloY0drdmRqRXZkRzl3YVdNdldtMXNOa2xuUWsxYU9XUXhWbGgwV0ZsYVJUTlBkeUlzSW1GamRDSTZJbXRsZVhCaGFYSWlMQ0owZVhCbElqb2ljMmhoY21WU1pYRWlMQ0pwYzNNaU9pSmthV1E2WlhSb2Nqb3dlR0U1WlRNeU16SmlOakZpWkdJMk56STNNVEppT1dGbE16TXhPVFV3Tmpsa09HUTJOVEZqTVdFaWZRLnRNbWh6cjFkbER0YUhUeWUtVDAxOGp2N0NUYlRhVWY4ZzlhbHNxVWJ6VGpGUkFsbV9qZ2RaR3pVVEVzeGtBY0ZmVy1ZSmpIVGtwSmtjNFNWREc3REJBQSIsImlzcyI6ImRpZDpldGhyOjB4ZThjOTFiZGU3NjI1YWIyYzBlZDlmMjE0ZGViMzk0NDBkYTdlMDNjNCJ9.-04Z_m2kgFBwF1Elh3jmv1_44jdGjEczf4x3c5Z4TxwiMP8nXZsIDVgsp3PS34DPGfpR4OkZ6LBozBBER3TABAA"
    )

    @Test
    fun verify() = runBlocking {

        tokens.forEach { token ->
            val payload = JWTTools().verify(token)
            assertNotNull(payload)
        }

    }

    @Suppress("UNUSED_VARIABLE")
    @Test(expected = JWTEncodingException::class)
    fun throws_when_algorithm_is_wrong() = runBlocking {
        val tested = JWTTools()

        val payload = emptyMap<String, Any>()
        val signer = KPSigner("0x1234")
        val issuerDID = "did:ethr:${signer.getAddress()}"

        //should throw a JWTEncodingException
        val unused = tested.createJWT(payload, issuerDID, signer, algorithm = "some fancy but unknown algorithm")

    }
}

