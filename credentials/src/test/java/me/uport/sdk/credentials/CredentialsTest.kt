package me.uport.sdk.credentials

import com.uport.sdk.signer.KPSigner
import kotlinx.coroutines.experimental.runBlocking
import me.uport.sdk.core.SystemTimeProvider
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K_R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialsTest {

    @Test
    fun `setup works`() {
        assertTrue(true)
    }

    @Test
    fun `can normalize a known string format to a DID format`() {
        val transformations = mapOf(
                //already did
                "did:example:something something" to "did:example:something something",
                "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74" to "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74",
                "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74#keys-1" to "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74#keys-1",
                "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX" to "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX",
                "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX#owner" to "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX#owner",

                //eth addr to ethrdid
                "0xf3beac30c498d9e26865f34fcaa57dbb935b0d74" to "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74",
                "0XF3BEAC30c498d9e26865f34fcaa57dbb935b0d74" to "did:ethr:0xF3BEAC30c498d9e26865f34fcaa57dbb935b0d74",
                "f3beac30c498d9e26865f34fcaa57dbb935b0d74" to "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74",

                //mnid to uport did
                "2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX" to "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX",
                "5A8bRWU3F7j3REx3vkJWxdjQPp4tqmxFPmab1Tr" to "did:uport:5A8bRWU3F7j3REx3vkJWxdjQPp4tqmxFPmab1Tr",

                //unknown is left intact
                "0x1234" to "0x1234",
                "2nQtiQG6Cgm1GYTBaaK" to "2nQtiQG6Cgm1GYTBaaK"
        )

        transformations.forEach { (orig, expected) ->
            assertEquals(expected, Credentials.normalizeKnownDID(orig))
        }
    }

    @Test
    fun `signJWT uses the correct algorithm for uport did`() = runBlocking {

        val cred = Credentials("did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX", KPSigner("0x1234"))
        val jwt = cred.signJWT(emptyMap())

        val (header, payload, signature) = JWTTools().decode(jwt)
        assertEquals(ES256K, header.alg)

    }

    @Test
    fun `signJWT uses the correct algorithm for non-uport did`() = runBlocking {

        val cred = Credentials("0xf3beac30c498d9e26865f34fcaa57dbb935b0d74", KPSigner("0x1234"))
        val jwt = cred.signJWT(emptyMap())

        val (header, payload, signature) = JWTTools().decode(jwt)
        assertEquals(ES256K_R, header.alg)

    }

    @Test
    fun `selective disclosure payload is filtered to specific fields`() {
        TODO("implement this scenario. the payload that actually gets signed should include the following fields if they are present in the parameters: requested, networkId(net), accountType(act), expiresIn(exp=iat+expiresIn), verified, callbackUrl(callback), notifications")
    }

    @Test
    fun `selective disclosure payload contains relevant fields`() = runBlocking {

        val params = SelectiveDisclosureRequestParams(
                requested = listOf("name", "country"),
                callbackUrl = "myapp://get-back-to-me-with-response.url",
                verified = listOf("email"),
                networkId = "0x4",
                accountType = RequestAccountType.keypair,
                vc = emptyList(),
                expiresInSeconds = 1234L,
                extras = mapOf(
                        "hello" to "world",
                        "type" to "expect this to be overwritten"
                )
        )

        val load = buildPayloadForShareReq(params)

        assertTrue((load["requested"] as List<*>).containsAll(listOf("name", "country")))
        assertTrue((load["verified"] as List<*>).containsAll(listOf("email")))
        assertEquals("myapp://get-back-to-me-with-response.url", load["callback"])
        assertEquals("0x4", load["net"])
        assertEquals("keypair", load["act"])
        assertTrue((load["vc"] as List<*>).isEmpty())
        assertEquals("world", load["hello"])
        assertEquals("shareReq", load["type"])
    }

}