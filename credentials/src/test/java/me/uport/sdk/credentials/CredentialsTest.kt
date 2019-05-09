package me.uport.sdk.credentials

import assertk.assert
import assertk.assertions.*
import com.uport.sdk.signer.KPSigner
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.SystemTimeProvider
import me.uport.sdk.ethrdid.EthrDIDDocument
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jwt.JWTAuthenticationException
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K_R
import me.uport.sdk.testhelpers.TestTimeProvider
import me.uport.sdk.testhelpers.coAssert
import me.uport.sdk.universaldid.UniversalDID
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

        val expectedJWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJzdWIiOiJkaWQ6ZXRocjoweGYzYmVhYzMwYzQ5OGQ5ZTI2ODY1ZjM0ZmNhYTU3ZGJiOTM1YjBkNzQiLCJjbGFpbSI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZ2UiOiIzNSIsImxvY2F0aW9uIjoiR2VybWFueSJ9LCJ2YyI6WyJleUpoYkdjaU9pSklVekkxTmlJc0luUjVjQ0k2SWtwWFZDSjkuZXlKemRXSWlPaUprYVdRNlpYUm9jam93ZUdZelltVmhZek13WXpRNU9HUTVaVEkyT0RZMVpqTTBabU5oWVRVM1pHSmlPVE0xWWpCa056UWlMQ0psWkhWallYUnBiMjRpT2lKTllYTjBaWEp6SWl3aWFXRjBJam94TlRFMk1qTTVNREl5ZlEud1RuUGhnTWJyU2xyV2NmUjdfX3hXYmxHLUEzbmdqTFQyYlBfTTdaOW1pWSIsImV5SmhiR2NpT2lKSVV6STFOaUlzSW5SNWNDSTZJa3BYVkNKOS5leUp6ZFdJaU9pSmthV1E2WlhSb2Nqb3dlR1l6WW1WaFl6TXdZelE1T0dRNVpUSTJPRFkxWmpNMFptTmhZVFUzWkdKaU9UTTFZakJrTnpRaUxDSnNiMk5oZEdsdmJpSTZJbFJsZUdGeklpd2lhV0YwSWpveE5URTJNak01TURJeWZRLk8yb3FZNHBnbUFtV3FlT3Q3NlBUaUIzeTlqRUdmMlphWEVoSVJlTTlJTFUiXSwiY2FsbGJhY2siOiJteWFwcDovL2dldC1iYWNrLXRvLW1lLXdpdGgtcmVzcG9uc2UudXJsIiwiaWF0IjoxMjM0NTY3OCwiZXhwIjoxMjM0ODY3OCwiaXNzIjoiZGlkOnVwb3J0OjJuUXRpUUc2Q2dtMUdZVEJhYUtBZ3I3NnVZN2lTZXhVa3FYIn0.aGy68_dqtXBi65MuDdwlVUHxJ4kBV_TjbHVKDPbyzYWyW-hCbBkO7AqLo3zN4ToiSOSZiWel4hl6p0HIBU9Hnw"

        val claim = mapOf(
                "name" to "John Doe",
                "age" to "35",
                "location" to "Germany"
        )

        val vc = listOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkaWQ6ZXRocjoweGYzYmVhYzMwYzQ5OGQ5ZTI2ODY1ZjM0ZmNhYTU3ZGJiOTM1YjBkNzQiLCJlZHVjYXRpb24iOiJNYXN0ZXJzIiwiaWF0IjoxNTE2MjM5MDIyfQ.wTnPhgMbrSlrWcfR7__xWblG-A3ngjLT2bP_M7Z9miY",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkaWQ6ZXRocjoweGYzYmVhYzMwYzQ5OGQ5ZTI2ODY1ZjM0ZmNhYTU3ZGJiOTM1YjBkNzQiLCJsb2NhdGlvbiI6IlRleGFzIiwiaWF0IjoxNTE2MjM5MDIyfQ.O2oqY4pgmAmWqeOt76PTiB3y9jEGf2ZaXEhIReM9ILU"
        )

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

        val expectedJWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJzdWIiOiJkaWQ6ZXRocjoweGYzYmVhYzMwYzQ5OGQ5ZTI2ODY1ZjM0ZmNhYTU3ZGJiOTM1YjBkNzQiLCJjbGFpbSI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZ2UiOiIzNSIsImxvY2F0aW9uIjoiR2VybWFueSJ9LCJ2YyI6W10sImNhbGxiYWNrIjoiIiwiaWF0IjoxMjM0NTY3OCwiZXhwIjoxMjM0NjI3OCwiaXNzIjoiZGlkOnVwb3J0OjJuUXRpUUc2Q2dtMUdZVEJhYUtBZ3I3NnVZN2lTZXhVa3FYIn0.C5sY_WCnSjYmqX-w3NZo9AmB6qVUy-Uwd6Fzz24CtbK0JWAYxgslqr6-JYjkB5O5Eu9IJYNS-1pKH-waNGGwmA"

        val claim = mapOf("name" to "John Doe",
                "age" to "35",
                "location" to "Germany")

        val timeProvider = TestTimeProvider(12345678000L)

        val cred = Credentials("did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX", KPSigner("0x1234"), timeProvider)
        val jwt = cred.createVerification("did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74", claim)

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


    @Test
    fun `successfully authenticates selective disclosure response`() = runBlocking {

        val map = mapOf<String, Any>(
                "iat" to 1556541978,
                "exp" to 1656628378,
                "aud" to "did:ethr:0xcf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed",
                "net" to "0x4",
                "own" to mapOf(
                        "name" to "Mike Gunn",
                        "email" to "mgunn@uport.me"
                ),
                "permissions" to listOf("notifications"),
                "req" to "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjYWxsYmFjayI6Imh0dHBzOi8vdXBvcnQtcHJvamVjdC5naXRodWIuaW8vdXBvcnQtYW5kcm9pZC1zZGsvY2FsbGJhY2tzIiwicmVxdWVzdGVkIjpbIm5hbWUiXSwiYWN0IjoiZ2VuZXJhbCIsInR5cGUiOiJzaGFyZVJlcSIsImlhdCI6MTU1NzM5NTExOCwiZXhwIjo5MDAwMDAwMDAxNTU3Mzk1MTE4LCJpc3MiOiJkaWQ6ZXRocjoweGNmMDNkZDBhODk0ZWY3OWNiNWI2MDFhNDNjNGIyNWUzYWU0YzY3ZWQifQ.6UVkmO5vXyNtn6gy_RKz1Wjx1eWqik_124aBfcmKFr_jv6T96xxPKIda8AMxFWvaqQ0BJo6rec-S-USBBhYFcgA"
        )

        val signer = KPSigner("0x1234")
        val issuer = "did:ethr:${signer.getAddress()}"

        val resolver = spyk(EthrDIDResolver(JsonRPC("")))

        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson("""
            {
              "@context": "https://w3id.org/did/v1",
              "id": "$issuer",
              "publicKey": [{
                   "id": "$issuer#owner",
                   "type": "Secp256k1VerificationKey2018",
                   "owner": "$issuer",
                   "ethereumAddress": "${signer.getAddress()}"}],
              "authentication": [{
                   "type": "Secp256k1SignatureAuthentication2018",
                   "publicKey": "$issuer#owner"}]
            }
        """.trimIndent())

        UniversalDID.registerResolver(resolver)

        val token = JWTTools().createJWT(map, issuer, signer)

        val authenticatedPayload = Credentials(issuer, signer).authenticateDisclosure(token)

        assert(authenticatedPayload).isNotNull()
    }

    @Test
    fun `throws error when req token is missing`() = runBlocking {

        val map = mapOf<String, Any>(
                "iat" to 1556541978,
                "exp" to 1656628378,
                "aud" to "did:ethr:0xcf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed",
                "net" to "0x4",
                "own" to mapOf(
                        "name" to "Mike Gunn",
                        "email" to "mgunn@uport.me"
                ),
                "permissions" to listOf("notifications")
        )

        val signer = KPSigner("0x1234")
        val issuer = "did:ethr:${signer.getAddress()}"

        val resolver = spyk(EthrDIDResolver(JsonRPC("")))

        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson("""
            {
              "@context": "https://w3id.org/did/v1",
              "id": "$issuer",
              "publicKey": [{
                   "id": "$issuer#owner",
                   "type": "Secp256k1VerificationKey2018",
                   "owner": "$issuer",
                   "ethereumAddress": "${signer.getAddress()}"}],
              "authentication": [{
                   "type": "Secp256k1SignatureAuthentication2018",
                   "publicKey": "$issuer#owner"}]
            }
        """.trimIndent())

        UniversalDID.registerResolver(resolver)

        val token = JWTTools().createJWT(map, issuer, signer)

        coAssert {
            Credentials(issuer, signer).authenticateDisclosure(token)
        }.thrownError {
            isInstanceOf(JWTAuthenticationException::class)
        }
    }


    @Test
    fun `throws error when request type is not a shareReq`() = runBlocking {

        val map = mapOf<String, Any>(
                "iat" to 1556541978,
                "exp" to 1656628378,
                "aud" to "did:ethr:0xcf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed",
                "net" to "0x4",
                "own" to mapOf(
                        "name" to "Mike Gunn",
                        "email" to "mgunn@uport.me"
                ),
                "permissions" to listOf("notifications"),
                "req" to "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjYWxsYmFjayI6Imh0dHBzOi8vdXBvcnQtcHJvamVjdC5naXRodWIuaW8vdXBvcnQtYW5kcm9pZC1zZGsvY2FsbGJhY2tzIiwic3ViIjoiZGlkOmV0aHI6MHgzZmYyNTExN2MwZTE3MGNhNTMwYmQ1ODkxODk5YzE4Mzk0NGRiNDMxIiwidHlwZSI6InZlclJlcSIsInVuc2lnbmVkQ2xhaW0iOnsiY2l0aXplbiBvZiBDbGV2ZXJsYW5kIjp0cnVlfSwiaWF0IjoxNTU3NDE3MTczLCJleHAiOjkwMDAwMDAwMDE1NTc0MTcxNzMsImlzcyI6ImRpZDpldGhyOjB4Y2YwM2RkMGE4OTRlZjc5Y2I1YjYwMWE0M2M0YjI1ZTNhZTRjNjdlZCJ9.hR0hmw-63WrTK-dsdzfdGhnleDY59zTIbFYk-V-L2yEoe4fn6piFBbkGeBBfVwxs7iTi679BtY8jA3pDl3OLdwA"
        )

        val signer = KPSigner("0x1234")
        val issuer = "did:ethr:${signer.getAddress()}"

        val resolver = spyk(EthrDIDResolver(JsonRPC("")))

        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson("""
            {
              "@context": "https://w3id.org/did/v1",
              "id": "$issuer",
              "publicKey": [{
                   "id": "$issuer#owner",
                   "type": "Secp256k1VerificationKey2018",
                   "owner": "$issuer",
                   "ethereumAddress": "${signer.getAddress()}"}],
              "authentication": [{
                   "type": "Secp256k1SignatureAuthentication2018",
                   "publicKey": "$issuer#owner"}]
            }
        """.trimIndent())

        UniversalDID.registerResolver(resolver)

        val token = JWTTools().createJWT(map, issuer, signer)

        coAssert {
            Credentials(issuer, signer).authenticateDisclosure(token)
        }.thrownError {
            isInstanceOf(JWTAuthenticationException::class)
        }
    }

    @Test
    fun `throws error when request issuer did in the request does not match issuer in the credentials`() = runBlocking {

        val map = mapOf<String, Any>(
                "iat" to 1556541978,
                "exp" to 1656628378,
                "aud" to "did:ethr:0xcf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed",
                "net" to "0x4",
                "own" to mapOf(
                        "name" to "Mike Gunn",
                        "email" to "mgunn@uport.me"
                ),
                "permissions" to listOf("notifications"),
                "req" to "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjbGFpbXMiOnsibmFtZSI6IlIgRGFuZWVsIE9saXZhdyJ9LCJpYXQiOjE1NDgxNjM2ODgsImV4cCI6MjE3ODg4MzY4OCwiaXNzIjoiZGlkOmV0aHI6MHg0MTIzY2JkMTQzYjU1YzA2ZTQ1MWZmMjUzYWYwOTI4NmI2ODdhOTUwIn0.Tral9PIGcNIH-3LrC9sAasPokbtnny3LPw9wrEGPqARXLQREGH6l8GI9JXL3o6_qjY3KF9Nbz0wi7g-pdlC-rgA"
        )

        val signer = KPSigner("0x1234")
        val issuer = "did:ethr:${signer.getAddress()}"

        val resolver = spyk(EthrDIDResolver(JsonRPC("")))

        // Mock [EtherDIDDocument] for credentials issuer DID
        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson("""
            {
              "@context": "https://w3id.org/did/v1",
              "id": "$issuer",
              "publicKey": [{
                   "id": "$issuer#owner",
                   "type": "Secp256k1VerificationKey2018",
                   "owner": "$issuer",
                   "ethereumAddress": "${signer.getAddress()}"}],
              "authentication": [{
                   "type": "Secp256k1SignatureAuthentication2018",
                   "publicKey": "$issuer#owner"}]
            }
        """.trimIndent())


        // Mock [EtherDIDDocument] for issuer DID in the request token embeded in the response map above
        coEvery { resolver.resolve("did:ethr:0x4123cbd143b55c06e451ff253af09286b687a950") } returns EthrDIDDocument.fromJson("""
            {
                "id": "did:ethr:0x4123cbd143b55c06e451ff253af09286b687a950",
                "publicKey": [
                    {
                        "id": "did:ethr:0x4123cbd143b55c06e451ff253af09286b687a950#owner",
                        "type": "Secp256k1VerificationKey2018",
                        "owner": "did:ethr:0x4123cbd143b55c06e451ff253af09286b687a950",
                        "ethereumAddress": "0x4123cbd143b55c06e451ff253af09286b687a950",
                        "publicKeyHex": null,
                        "publicKeyBase64": null,
                        "publicKeyBase58": null,
                        "value": null
                    }
                ],
                "authentication": [
                    {
                        "type": "Secp256k1SignatureAuthentication2018",
                        "publicKey": "did:ethr:0x4123cbd143b55c06e451ff253af09286b687a950#owner"
                    }
                ],
                "service": [
                ],
                "@context": "https://w3id.org/did/v1"
            }
        """.trimIndent())

        UniversalDID.registerResolver(resolver)

        val token = JWTTools().createJWT(map, issuer, signer)

        coAssert {
            Credentials(issuer, signer).authenticateDisclosure(token)
        }.thrownError {
            isInstanceOf(JWTAuthenticationException::class)
        }
    }
}