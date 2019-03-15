package me.uport.sdk.credentials

import assertk.assert
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isNotNull
import com.uport.sdk.signer.KPSigner
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.SystemTimeProvider
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K_R
import me.uport.sdk.testhelpers.TestTimeProvider
import org.junit.Test

class CredentialsTest {

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
            assert(Credentials.normalizeKnownDID(orig)).isEqualTo(expected)
        }
    }

    @Test
    fun `signJWT uses the correct algorithm for uport did`() = runBlocking {

        val cred = Credentials("did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX", KPSigner("0x1234"))
        val jwt = cred.signJWT(emptyMap())

        val (header, _, _) = JWTTools().decode(jwt)
        assert(header.alg).isEqualTo(ES256K)

    }

    @Test
    fun `create verification test with all params`() = runBlocking {

        val expectedJWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJzdWIiOiJkaWQ6ZXRocjoweGYzYmVhYzMwYzQ5OGQ5ZTI2ODY1ZjM0ZmNhYTU3ZGJiOTM1YjBkNzQiLCJjbGFpbSI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZ2UiOiIzNSIsImxvY2F0aW9uIjoiR2VybWFueSJ9LCJ2YyI6eyJlZHVjYXRpb24iOiJNYXN0ZXJzIiwicHJvZmVzc2lvbiI6IkludmVzdG1lbnQgQmFua2VyIiwicGFzc29ydCI6IkdFMzQ1OEpDIn0sImNhbGxiYWNrIjoibXlhcHA6Ly9nZXQtYmFjay10by1tZS13aXRoLXJlc3BvbnNlLnVybCIsImlhdCI6MTIzNDU2NzgsImV4cCI6MTIzNDg2NzgsImlzcyI6ImRpZDp1cG9ydDoyblF0aVFHNkNnbTFHWVRCYWFLQWdyNzZ1WTdpU2V4VWtxWCJ9.nHJXDYJnTj9QBjN90VTqJOzpcldNmeZ_BMTB3ZPeDbwY9Q99iHF1P-yQGRQF7efG-WOQYsdhfpcSle5kEDQ4cg"

        val claim = mapOf("name" to "John Doe",
                "age" to "35",
                "location" to "Germany")

        val vc = mapOf("education" to "Masters",
                "profession" to "Investment Banker",
                "passort" to "GE3458JC")

        val timeProvider = TestTimeProvider(12345678000L)

        val cred = Credentials("did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX", KPSigner("0x1234"), timeProvider)
        val jwt = cred.createVerification(
                "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74",
                claim,
                "myapp://get-back-to-me-with-response.url",
                vc,
                3000L
        )

        assert(jwt).isEqualTo(expectedJWT)

    }

    @Test
    fun `create verification test with required params only`() = runBlocking {

        val expectedJWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJzdWIiOiJkaWQ6ZXRocjoweGYzYmVhYzMwYzQ5OGQ5ZTI2ODY1ZjM0ZmNhYTU3ZGJiOTM1YjBkNzQiLCJjbGFpbSI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZ2UiOiIzNSIsImxvY2F0aW9uIjoiR2VybWFueSJ9LCJ2YyI6e30sImNhbGxiYWNrIjoibXlhcHA6Ly9nZXQtYmFjay10by1tZS13aXRoLXJlc3BvbnNlLnVybCIsImlhdCI6MTIzNDU2NzgsImV4cCI6MTIzNDYyNzgsImlzcyI6ImRpZDp1cG9ydDoyblF0aVFHNkNnbTFHWVRCYWFLQWdyNzZ1WTdpU2V4VWtxWCJ9.Z0rachvLJzskxGJMuV8KwQDbMZE0mrvNbaTMQ5KgwiPFLDlpa7QtvuYQrCqcnLwfPpvWrHwrBLjgQsa8D_g8LA"

        val claim = mapOf("name" to "John Doe",
                "age" to "35",
                "location" to "Germany")

        val timeProvider = TestTimeProvider(12345678000L)

        val cred = Credentials("did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX", KPSigner("0x1234"), timeProvider)
        val jwt = cred.createVerification(
                "did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74",
                claim,
                "myapp://get-back-to-me-with-response.url",
                null,
                null
        )

        assert(jwt).isEqualTo(expectedJWT)

    }

    @Test
    fun `signJWT uses the correct algorithm for non-uport did`() = runBlocking {

        val cred = Credentials("0xf3beac30c498d9e26865f34fcaa57dbb935b0d74", KPSigner("0x1234"))
        val jwt = cred.signJWT(emptyMap())

        val (header, _, _) = JWTTools().decode(jwt)
        assert(header.alg).isEqualTo(ES256K_R)

    }

    @Test
    fun `selective disclosure request contains required fields`() = runBlocking {
        val nowSeconds = Math.floor(SystemTimeProvider.nowMs() / 1000.0).toLong()
        val cred = Credentials("did:example:issuer", KPSigner("0x1234"))

        val jwt = cred.createDisclosureRequest(SelectiveDisclosureRequestParams(emptyList(), ""))
        val (_, payload, _) = JWTTools().decode(jwt)

        assert(payload.iss).isEqualTo("did:example:issuer")
        assert(payload.iat).isNotNull {
            it.isGreaterThanOrEqualTo(nowSeconds)
        }
        assert(payload.type).isEqualTo(JWTTypes.shareReq.name)
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

        assert((load["requested"] as List<*>).containsAll(listOf("name", "country")))
        assert((load["verified"] as List<*>).containsAll(listOf("email")))

        assert(load["callback"]).isEqualTo("myapp://get-back-to-me-with-response.url")
        assert(load["net"]).isEqualTo("0x4")
        assert(load["act"]).isEqualTo("keypair")
        assert(load["hello"]).isEqualTo("world")
        assert(load["type"]).isEqualTo("shareReq")

        assert((load["vc"] as List<*>)).isEmpty()

    }

    @Test
    fun `personal sign request payload contains relevant fields`() = runBlocking {

        val params = PersonalSignRequestParams(
                data = "sign this message",
                callbackUrl = "myapp://get-back-to-me-with-response.url",
                from = "0x1122334455667788990011223344556677889900",
                riss = "did:ethr:0x1122334455667788990011223344556677889900",
                networkId = "0x4",
                vc = emptyList(),
                expiresInSeconds = 1234L,
                extras = mapOf(
                        "hello" to "world",
                        "type" to "expect this to be overwritten"
                )
        )

        val load = buildPayloadForPersonalSignReq(params)

        assert(load["type"]).isEqualTo("personalSigReq")
        assert(load["data"]).isEqualTo("sign this message")
        assert(load["callback"]).isEqualTo("myapp://get-back-to-me-with-response.url")
        assert(load["riss"]).isEqualTo("did:ethr:0x1122334455667788990011223344556677889900")
        assert(load["from"]).isEqualTo("0x1122334455667788990011223344556677889900")
        assert(load["net"]).isEqualTo("0x4")
        assert((load["vc"] as List<*>)).isEmpty()
        assert(load["hello"]).isEqualTo("world")

    }

    @Test
    fun `verified claim request payload contains relevant fields`() = runBlocking {

        val params = VerifiedClaimRequestParams(
                unsignedClaim = mapOf("name" to "John Doe"),
                callbackUrl = "myapp://get-back-to-me-with-response.url",
                riss = "did:ethr:0x1122334455667788990011223344556677889900",
                rexp = 1234L,
                aud = "did:ethr:0x9988776655443322110099887766554433221100",
                sub = "did:ethr:0xFFEEDDCCBBAA9988776655443322110099887766",
                issc = mapOf("dappName" to "testing"),
                vc = emptyList(),
                expiresInSeconds = 1234L,
                extras = mapOf(
                        "hello" to "world",
                        "type" to "expect this to be overwritten"
                )
        )

        val load = buildPayloadForVerifiedClaimReq(params)

        assert(load["type"]).isEqualTo("verReq")
        assert(load["unsignedClaim"]).isEqualTo(mapOf("name" to "John Doe"))
        assert(load["callback"]).isEqualTo("myapp://get-back-to-me-with-response.url")
        assert(load["riss"]).isEqualTo("did:ethr:0x1122334455667788990011223344556677889900")
        assert((load["vc"] as List<*>)).isEmpty()
        assert(load["hello"]).isEqualTo("world")
        assert(load["aud"]).isEqualTo("did:ethr:0x9988776655443322110099887766554433221100")
        assert(load["sub"]).isEqualTo("did:ethr:0xFFEEDDCCBBAA9988776655443322110099887766")
        assert(load["issc"]).isEqualTo(mapOf("dappName" to "testing"))
        assert(load["rexp"]).isEqualTo(1234L)
    }

}