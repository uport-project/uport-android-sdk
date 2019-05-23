package me.uport.sdk.credentials

import assertk.assertThat
import assertk.assertions.*
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.SystemTimeProvider
import me.uport.sdk.ethrdid.EthrDIDDocument
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K_R
import me.uport.sdk.signer.KPSigner
import me.uport.sdk.testhelpers.TestTimeProvider
import me.uport.sdk.testhelpers.coAssert
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDDocument
import me.uport.sdk.uportdid.UportDIDResolver
import org.junit.Test

class CredentialsTest {

    @Test
    fun `signJWT uses the correct algorithm for uport did`() = runBlocking {

        val cred = Credentials("did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX", KPSigner("0x1234"))
        val jwt = cred.signJWT(emptyMap())

        val (header, _, _) = JWTTools().decode(jwt)
        assertThat(header.alg).isEqualTo(ES256K)

    }

    @Test
    fun `create verification test with all params`() = runBlocking {

        val expectedJWT =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJzdWIiOiJkaWQ6ZXRocjoweGYzYmVhYzMwYzQ5OGQ5ZTI2ODY1ZjM0ZmNhYTU3ZGJiOTM1YjBkNzQiLCJjbGFpbSI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZ2UiOiIzNSIsImxvY2F0aW9uIjoiR2VybWFueSJ9LCJ2YyI6WyJleUpoYkdjaU9pSklVekkxTmlJc0luUjVjQ0k2SWtwWFZDSjkuZXlKemRXSWlPaUprYVdRNlpYUm9jam93ZUdZelltVmhZek13WXpRNU9HUTVaVEkyT0RZMVpqTTBabU5oWVRVM1pHSmlPVE0xWWpCa056UWlMQ0psWkhWallYUnBiMjRpT2lKTllYTjBaWEp6SWl3aWFXRjBJam94TlRFMk1qTTVNREl5ZlEud1RuUGhnTWJyU2xyV2NmUjdfX3hXYmxHLUEzbmdqTFQyYlBfTTdaOW1pWSIsImV5SmhiR2NpT2lKSVV6STFOaUlzSW5SNWNDSTZJa3BYVkNKOS5leUp6ZFdJaU9pSmthV1E2WlhSb2Nqb3dlR1l6WW1WaFl6TXdZelE1T0dRNVpUSTJPRFkxWmpNMFptTmhZVFUzWkdKaU9UTTFZakJrTnpRaUxDSnNiMk5oZEdsdmJpSTZJbFJsZUdGeklpd2lhV0YwSWpveE5URTJNak01TURJeWZRLk8yb3FZNHBnbUFtV3FlT3Q3NlBUaUIzeTlqRUdmMlphWEVoSVJlTTlJTFUiXSwiY2FsbGJhY2siOiJteWFwcDovL2dldC1iYWNrLXRvLW1lLXdpdGgtcmVzcG9uc2UudXJsIiwiaWF0IjoxMjM0NTY3OCwiZXhwIjoxMjM0ODY3OCwiaXNzIjoiZGlkOnVwb3J0OjJuUXRpUUc2Q2dtMUdZVEJhYUtBZ3I3NnVZN2lTZXhVa3FYIn0.aGy68_dqtXBi65MuDdwlVUHxJ4kBV_TjbHVKDPbyzYWyW-hCbBkO7AqLo3zN4ToiSOSZiWel4hl6p0HIBU9Hnw"

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
        assertThat(jwt).isEqualTo(expectedJWT)
    }

    @Test
    fun `create verification test with required params only`() = runBlocking {

        val expectedJWT =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJzdWIiOiJkaWQ6ZXRocjoweGYzYmVhYzMwYzQ5OGQ5ZTI2ODY1ZjM0ZmNhYTU3ZGJiOTM1YjBkNzQiLCJjbGFpbSI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZ2UiOiIzNSIsImxvY2F0aW9uIjoiR2VybWFueSJ9LCJ2YyI6W10sImNhbGxiYWNrIjoiIiwiaWF0IjoxMjM0NTY3OCwiZXhwIjoxMjM0NjI3OCwiaXNzIjoiZGlkOnVwb3J0OjJuUXRpUUc2Q2dtMUdZVEJhYUtBZ3I3NnVZN2lTZXhVa3FYIn0.C5sY_WCnSjYmqX-w3NZo9AmB6qVUy-Uwd6Fzz24CtbK0JWAYxgslqr6-JYjkB5O5Eu9IJYNS-1pKH-waNGGwmA"

        val claim = mapOf(
            "name" to "John Doe",
            "age" to "35",
            "location" to "Germany"
        )

        val timeProvider = TestTimeProvider(12345678000L)

        val cred = Credentials("did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX", KPSigner("0x1234"), timeProvider)
        val jwt = cred.createVerification("did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74", claim)

        assertThat(jwt).isEqualTo(expectedJWT)
    }

    @Test
    fun `signJWT uses the correct algorithm for non-uport did`() = runBlocking {

        val cred = Credentials("0xf3beac30c498d9e26865f34fcaa57dbb935b0d74", KPSigner("0x1234"))
        val jwt = cred.signJWT(emptyMap())

        val (header, _, _) = JWTTools().decode(jwt)
        assertThat(header.alg).isEqualTo(ES256K_R)

    }

    @Test
    fun `selective disclosure request contains required fields`() = runBlocking {
        val nowSeconds = Math.floor(SystemTimeProvider.nowMs() / 1000.0).toLong()
        val cred = Credentials("did:example:issuer", KPSigner("0x1234"))

        val jwt = cred.createDisclosureRequest(SelectiveDisclosureRequestParams(emptyList(), ""))
        val (_, payload, _) = JWTTools().decode(jwt)

        assertThat(payload.iss).isEqualTo("did:example:issuer")
        assertThat(payload.iat)
            .isNotNull()
            .isGreaterThanOrEqualTo(nowSeconds)
        assertThat(payload.type).isEqualTo(JWTTypes.shareReq.name)
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

        assertThat((load["requested"] as List<*>).containsAll(listOf("name", "country")))
        assertThat((load["verified"] as List<*>).containsAll(listOf("email")))

        assertThat(load["callback"]).isEqualTo("myapp://get-back-to-me-with-response.url")
        assertThat(load["net"]).isEqualTo("0x4")
        assertThat(load["act"]).isEqualTo("keypair")
        assertThat(load["hello"]).isEqualTo("world")
        assertThat(load["type"]).isEqualTo("shareReq")

        assertThat((load["vc"] as List<*>)).isEmpty()

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

        assertThat(load["type"]).isEqualTo("personalSigReq")
        assertThat(load["data"]).isEqualTo("sign this message")
        assertThat(load["callback"]).isEqualTo("myapp://get-back-to-me-with-response.url")
        assertThat(load["riss"]).isEqualTo("did:ethr:0x1122334455667788990011223344556677889900")
        assertThat(load["from"]).isEqualTo("0x1122334455667788990011223344556677889900")
        assertThat(load["net"]).isEqualTo("0x4")
        assertThat((load["vc"] as List<*>)).isEmpty()
        assertThat(load["hello"]).isEqualTo("world")

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

        assertThat(load["type"]).isEqualTo("verReq")
        assertThat(load["unsignedClaim"]).isEqualTo(mapOf("name" to "John Doe"))
        assertThat(load["callback"]).isEqualTo("myapp://get-back-to-me-with-response.url")
        assertThat(load["riss"]).isEqualTo("did:ethr:0x1122334455667788990011223344556677889900")
        assertThat((load["vc"] as List<*>)).isEmpty()
        assertThat(load["hello"]).isEqualTo("world")
        assertThat(load["aud"]).isEqualTo("did:ethr:0x9988776655443322110099887766554433221100")
        assertThat(load["sub"]).isEqualTo("did:ethr:0xFFEEDDCCBBAA9988776655443322110099887766")
        assertThat(load["issc"]).isEqualTo(mapOf("dappName" to "testing"))
        assertThat(load["rexp"]).isEqualTo(1234L)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `can return uport profile from jwt payload`() = runBlocking {

        val map = mapOf(
            "iat" to 1556541978,
            "exp" to 1656628378,
            "aud" to "did:ethr:0xcf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed",
            "net" to "0x4",
            "own" to mapOf(
                "name" to "Mike Gunn",
                "email" to "mgunn@uport.me"
            ),
            "verified" to listOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTU1NjcwMTA3NCwiZXhwIjoxNzIwMzY2NDMyLCJuZXQiOiIweDQiLCJ0eXBlIjoic2hhcmVSZXEifQ.PjsCopgtHxfTkGrQUT1ID7P8bfXyeCZoy0GnHw5p8xv6mJYDE9MAVQK6sjEivXyOQhb2bWs4Pm9vWl4dFEhpGwE",
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjbGFpbXMiOnsibmFtZSI6IlIgRGFuZWVsIE9saXZhdyJ9LCJpYXQiOjEyMzQ1Njc4LCJleHAiOjEyMzQ1OTc4LCJpc3MiOiJkaWQ6ZXRocjoweDQxMjNjYmQxNDNiNTVjMDZlNDUxZmYyNTNhZjA5Mjg2YjY4N2E5NTAifQ.o6eDKYjHJnak1ylkpe9g8krxvK9UEhKf-1T0EYhH8pGyb8MjOEepRJi8DYlVEnZno0DkVYXQCf3u1i_HThBKtAA"
            ),
            "permissions" to listOf("notifications"),
            "req" to "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjYWxsYmFjayI6Imh0dHBzOi8vdXBvcnQtcHJvamVjdC5naXRodWIuaW8vdXBvcnQtYW5kcm9pZC1zZGsvY2FsbGJhY2tzIiwicmVxdWVzdGVkIjpbIm5hbWUiXSwiYWN0IjoiZ2VuZXJhbCIsInR5cGUiOiJzaGFyZVJlcSIsImlhdCI6MTU1NjcyMTQxMywiZXhwIjoxNTU2NzIyMDEzLCJpc3MiOiJkaWQ6ZXRocjoweGNmMDNkZDBhODk0ZWY3OWNiNWI2MDFhNDNjNGIyNWUzYWU0YzY3ZWQifQ.KfDgkaOWZxxfprgBxPvC2wSd-BrhdjN-gTf7br5Li4LtTgSmk9I55dE2xWekSSWTaQxC74DDRCxrEsVH3I1bWwE"
        )

        val signer = KPSigner("0x1234")
        val issuer = "did:ethr:${signer.getAddress()}"

        val ethrDidResolver = spyk(EthrDIDResolver(JsonRPC("")))
        coEvery { ethrDidResolver.resolve(eq(issuer)) } returns
                EthrDIDDocument.fromJson(""" { "@context": "https://w3id.org/did/v1", "id": "$issuer", "publicKey": [{ "id": "$issuer#owner", "type": "Secp256k1VerificationKey2018", "owner": "$issuer", "ethereumAddress": "${signer.getAddress()}"}], "authentication": [{ "type": "Secp256k1SignatureAuthentication2018", "publicKey": "$issuer#owner"}] } """)

        val uportDidResolver = spyk(UportDIDResolver(JsonRPC("")))
        coEvery { uportDidResolver.resolve(eq("2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG")) } returns
                //language=json
                UportDIDDocument.fromJson("""{"id":"did:uport:2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG","publicKey":[{"id":"did:uport:2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG#keys-1","type":"Secp256k1VerificationKey2018","owner":"did:uport:2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG","ethereumAddress":"0x476c88ed464efd251a8b18eb84785f7c46807873"}],"authentication":[{"type":"Secp256k1SignatureAuthentication2018","publicKey":"did:uport:2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG#keys-1"}],"service":[],"@context":"https://w3id.org/did/v1", "uportProfile" : {"@type": "Person"}}""")

        UniversalDID.registerResolver(ethrDidResolver)
        UniversalDID.registerResolver(uportDidResolver)

        val token = JWTTools().createJWT(map, issuer, signer)

        val uPortProfile = Credentials(issuer, signer).verifyDisclosure(token)

        assertThat(uPortProfile).isNotNull()
        assertThat(uPortProfile.did).isEqualTo(issuer)
        assertThat(uPortProfile.networkId).isEqualTo("0x4")
        assertThat(uPortProfile.name).isEqualTo("Mike Gunn")
        assertThat(uPortProfile.email).isEqualTo("mgunn@uport.me")
        assertThat(uPortProfile.invalid.size + uPortProfile.valid.size).isEqualTo((map["verified"] as List<String>?)?.size)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `can fetch networkId from request token`() = runBlocking {

        val map = mapOf(
            "iat" to 1556541978,
            "exp" to 1656628378,
            "aud" to "did:ethr:0xcf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed",
            "own" to mapOf(
                "name" to "Mike Gunn",
                "email" to "mgunn@uport.me"
            ),
            "permissions" to listOf("notifications"),
            "req" to "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjYWxsYmFjayI6Imh0dHBzOi8vdXBvcnQtcHJvamVjdC5naXRodWIuaW8vdXBvcnQtYW5kcm9pZC1zZGsvY2FsbGJhY2tzIiwicmVxdWVzdGVkIjpbIm5hbWUiLCJib3hQdWIiLCJuZXQiXSwibmV0IjoiMHg0IiwiYWN0IjoiZ2VuZXJhbCIsInR5cGUiOiJzaGFyZVJlcSIsImlhdCI6MTU1NzEzNzczMCwiZXhwIjoxNTU3MTM4MzMwLCJpc3MiOiJkaWQ6ZXRocjoweGNmMDNkZDBhODk0ZWY3OWNiNWI2MDFhNDNjNGIyNWUzYWU0YzY3ZWQifQ.xPnTKcFsE3MRxkm6I__xSRVSFQgIT5tUgfL_O7q0hfPTNxnA8DM53yqMdl_1stqzoIv2VKgMC40oFfYh9Ql-TAA"
        )

        val signer = KPSigner("0x1234")
        val issuer = "did:ethr:${signer.getAddress()}"

        val resolver = spyk(EthrDIDResolver(JsonRPC("")))

        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson(
            """
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
        """.trimIndent()
        )

        UniversalDID.registerResolver(resolver)

        val token = JWTTools().createJWT(map, issuer, signer)

        val uPortProfile = Credentials(issuer, signer).verifyDisclosure(token)

        assertThat(uPortProfile).isNotNull()
        assertThat(uPortProfile.networkId).isEqualTo("0x4")
    }

    @Test
    fun `can return uport profile from jwt payload without all properties`() = runBlocking {

        val map = mapOf(
            "iat" to 1556541978,
            "exp" to 1656628378,
            "aud" to "did:ethr:0xcf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed",
            "permissions" to listOf("notifications"),
            "req" to "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjYWxsYmFjayI6Imh0dHBzOi8vdXBvcnQtcHJvamVjdC5naXRodWIuaW8vdXBvcnQtYW5kcm9pZC1zZGsvY2FsbGJhY2tzIiwicmVxdWVzdGVkIjpbIm5hbWUiXSwiYWN0IjoiZ2VuZXJhbCIsInR5cGUiOiJzaGFyZVJlcSIsImlhdCI6MTU1NjcyMTQxMywiZXhwIjoxNTU2NzIyMDEzLCJpc3MiOiJkaWQ6ZXRocjoweGNmMDNkZDBhODk0ZWY3OWNiNWI2MDFhNDNjNGIyNWUzYWU0YzY3ZWQifQ.KfDgkaOWZxxfprgBxPvC2wSd-BrhdjN-gTf7br5Li4LtTgSmk9I55dE2xWekSSWTaQxC74DDRCxrEsVH3I1bWwE"
        )

        val signer = KPSigner("0x1234")
        val issuer = "did:ethr:${signer.getAddress()}"

        val resolver = spyk(EthrDIDResolver(JsonRPC("")))

        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson(
            """
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
        """.trimIndent()
        )

        UniversalDID.registerResolver(resolver)

        val token = JWTTools().createJWT(map, issuer, signer)

        val uPortProfile = Credentials(issuer, signer).verifyDisclosure(token)

        assertThat(uPortProfile).isNotNull()
        assertThat(uPortProfile.did).isEqualTo(issuer)
    }

    @Test
    fun `successfully authenticates selective disclosure response`() = runBlocking {

        val map = mapOf(
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

        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson(
            """
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
        """.trimIndent()
        )

        UniversalDID.registerResolver(resolver)

        val token = JWTTools().createJWT(map, issuer, signer)

        val authenticatedPayload = Credentials(issuer, signer).authenticateDisclosure(token)

        assertThat(authenticatedPayload).isNotNull()
        Unit
    }

    @Test
    fun `throws error when req token is missing`() = runBlocking {

        val map = mapOf(
            "iat" to 1556541978,
            "exp" to 1656628378,
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

        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson(
            """
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
        """.trimIndent()
        )

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

        val map = mapOf(
            "iat" to 1556541978,
            "exp" to 1656628378,
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

        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson(
            """
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
        """.trimIndent()
        )

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

        val map = mapOf(
            "iat" to 1556541978,
            "exp" to 1656628378,
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
        coEvery { resolver.resolve(eq(issuer)) } returns EthrDIDDocument.fromJson(
            """
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
        """.trimIndent()
        )


        // Mock [EtherDIDDocument] for issuer DID in the request token embeded in the response map above
        coEvery { resolver.resolve("did:ethr:0x4123cbd143b55c06e451ff253af09286b687a950") } returns EthrDIDDocument.fromJson(
            """
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
        """.trimIndent()
        )

        UniversalDID.registerResolver(resolver)

        val token = JWTTools().createJWT(map, issuer, signer)

        coAssert {
            Credentials(issuer, signer).authenticateDisclosure(token)
        }.thrownError {
            isInstanceOf(JWTAuthenticationException::class)
        }
    }

    @Test
    fun `claims request params builds request successfully`() = runBlocking {
        val claim = ClaimsRequestParams()
            .addVerifiable(
                "email",
                VerifiableParams(
                    "We need to be able to email you",
                    true
                )
                    .addIssuer("did:web:uport.claims", "https://uport.claims/email")
                    .addIssuer("did:web:sobol.io", "https://sobol.io/verify")
            )
            .addVerifiable(
                "nationalIdentity",
                VerifiableParams(
                    "To legally be able to open your account"
                )
                    .addIssuer("did:web:idverifier.claims", "https://idverifier.example")
            )
            .addUserInfo("name", UserInfoParams("Show your name to other users", true))
            .addUserInfo("country", UserInfoParams("Show your country to other users", true))
            .build()

        val params = SelectiveDisclosureRequestParams(
            requested = listOf("name", "country"),
            callbackUrl = "myapp://get-back-to-me-with-response.url",
            claims = claim
        )

        val load = buildPayloadForShareReq(params)

        assertThat((load.get("claims") as Map<*, *>).containsKey("verifiable")).isEqualTo(true)
        assertThat((load.get("claims") as Map<*, *>).containsKey("user_info")).isEqualTo(true)

        assertThat(((load.get("claims") as Map<*, *>).get("verifiable") as Map<*, *>).containsKey("email")).isEqualTo(
            true
        )
        assertThat(((load.get("claims") as Map<*, *>).get("verifiable") as Map<*, *>).containsKey("nationalIdentity")).isEqualTo(
            true
        )

        assertThat(((load.get("claims") as Map<*, *>).get("user_info") as Map<*, *>).containsKey("name")).isEqualTo(true)
        assertThat(((load.get("claims") as Map<*, *>).get("user_info") as Map<*, *>).containsKey("country")).isEqualTo(
            true
        )
    }

    @Test
    fun `claims request params builds request successfully with minimal params`() = runBlocking {
        val claim = ClaimsRequestParams()
            .addVerifiable(
                "email",
                null
            )
            .addVerifiable(
                "nationalIdentity",
                VerifiableParams(
                    null
                )
                    .addIssuer("did:web:idverifier.claims")
            )
            .addUserInfo("name", UserInfoParams(null))
            .addUserInfo("country", null)
            .build()

        val params = SelectiveDisclosureRequestParams(
            requested = listOf("name", "country"),
            callbackUrl = "myapp://get-back-to-me-with-response.url",
            claims = claim
        )

        val load = buildPayloadForShareReq(params)

        assertThat((load.get("claims") as Map<*, *>).containsKey("verifiable")).isEqualTo(true)
        assertThat((load.get("claims") as Map<*, *>).containsKey("user_info")).isEqualTo(true)

        assertThat(((load.get("claims") as Map<*, *>).get("verifiable") as Map<*, *>).get("email")).isNull()
        assertThat(((load.get("claims") as Map<*, *>).get("user_info") as Map<*, *>).get("country")).isNull()
    }

    @Test
    fun `can decode a jwt with claims field into a map`() = runBlocking {
        val claim = ClaimsRequestParams()
            .addVerifiable(
                "email",
                VerifiableParams(
                    "We need to be able to email you",
                    true
                )
                    .addIssuer("did:web:uport.claims", "https://uport.claims/email")
                    .addIssuer("did:web:sobol.io", "https://sobol.io/verify")
            )
            .addVerifiable(
                "nationalIdentity",
                VerifiableParams(
                    "To legally be able to open your account"
                )
                    .addIssuer("did:web:idverifier.claims", "https://idverifier.example")
            )
            .addUserInfo("name", UserInfoParams("Show your name to other users", true))
            .addUserInfo("country", UserInfoParams("Show your country to other users", true))
            .build()

        val params = SelectiveDisclosureRequestParams(
            requested = listOf("name", "country"),
            callbackUrl = "myapp://get-back-to-me-with-response.url",
            claims = claim
        )

        val signer = KPSigner("0x1234")
        val issuer = "did:ethr:${signer.getAddress()}"

        val cred = Credentials(issuer, signer)
        val requestJWT = cred.createDisclosureRequest(params)

        val (_, payloadMap, _) = JWTTools().decodeRaw(requestJWT)

        assertThat(payloadMap.containsKey("claims")).isEqualTo(true)
    }
}
