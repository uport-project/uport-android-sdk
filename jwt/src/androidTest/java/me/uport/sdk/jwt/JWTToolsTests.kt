package me.uport.sdk.jwt

/**
 * Created by aldi on 3/10/18.
 */
import android.content.Context
import android.support.test.InstrumentationRegistry
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jwt.model.JwtPayload
import me.uport.sdk.testhelpers.TestTimeProvider
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDDocument
import me.uport.sdk.uportdid.UportDIDResolver
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class JWTToolsTests {

    private val referenceSeedPhrase = "notice suffer eagle style exclude burst write mechanic junior crater crystal seek"

    private lateinit var appContext: Context
    private lateinit var rootHandle: String

    @Before
    fun run_before_every_test() {
        appContext = InstrumentationRegistry.getTargetContext()
        val (handle, _) = runBlocking { ensureSeedIsImported(appContext, referenceSeedPhrase) }
        rootHandle = handle
    }

    @Test
    fun can_create_token_with_typed_payload() {
        val rpc = mockk<JsonRPC>()
        val resolver = spyk(UportDIDResolver(rpc)) {
            coEvery { resolve(eq("2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH")) }.returns(
                    UportDIDDocument.fromJson("""{"id":"did:uport:2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH","publicKey":[{"id":"did:uport:2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH#keys-1","type":"Secp256k1VerificationKey2018","owner":"did:uport:2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH","publicKeyHex":"0437c6a2555984b5a2c3a8947b0bd623eea1cfa3c13264b32345ffd771896d48395976849aaeb97460119f8b8965e04404d579662cd742f3438f74f0844c7c419c"},{"id":"did:uport:2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH#keys-2","type":"Curve25519EncryptionPublicKey","owner":"did:uport:2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH","publicKeyBase64":"LVfRrP9BVRkfglZueCpNLz5yCC6s583lQtneykGFrzU="}],"authentication":[{"type":"Secp256k1SignatureAuthentication2018","publicKey":"did:uport:2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH#keys-1"}],"service":[],"@context":"https://w3id.org/did/v1","uportProfile":{"@type":"Person"}}""")!!
            )
        }
        UniversalDID.registerResolver(resolver)

        val payload = JwtPayload(iss = "2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH",
                aud = "2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG",
                type = "shareResp",
                nad = "2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH",
                own = mapOf(
                        "name" to "Identity 1",
                        "phone" to "10000",
                        "country" to ""
                ),
                req = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMjEwMDM4OCwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vZGVtby51cG9ydC5tZS8jIiwibmV0IjoiMHg0IiwiZXhwIjoxNTIyMTAwOTg4LCJ0eXBlIjoic2hhcmVSZXEifQ.3Lkbz4zG0kcDaOkhThFY9iGhvqagKmXHos15JHGl3IvSn6-fXJJTf6xsYCL_wYDHI3GctbNUHhTuCYkJRF5NPw",
                capabilities = listOf("eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb3VmRUEzNXk3R2lBcGNkeUw4N0xwNXpUVjdOUk5DRjZISCIsImlhdCI6MTUyMjEwMDM5NiwiYXVkIjoiMm9lWHVmSEdEcFU1MWJmS0JzWkRkdTdKZTl3ZUozcjdzVkciLCJ0eXBlIjoibm90aWZpY2F0aW9ucyIsInZhbHVlIjoiYXJuOmF3czpzbnM6dXMtd2VzdC0yOjExMzE5NjIxNjU1ODplbmRwb2ludC9HQ00vdVBvcnQvMTkxZTBiMjctZWFmZi0zMWVkLTk4NGUtNTg2ZjU1OWYzMDEyIiwiZXhwIjoxNTIzMzk2Mzk2fQ.XyjR2iM0ZUgvolEhcP9n50g7JAJ9VMjS5_ASqj29-riOV1sEnYiLcH44E2joPo-clcFoA0owW19OcyRLpur50g")
        )

        val latch = CountDownLatch(1)

        //XXX: even though it's deprecated, it doesn't hurt to keep this test around until it's completely removed
        @Suppress("DEPRECATION")
        JWTTools().create(context = appContext, payload = payload, rootHandle = rootHandle, derivationPath = UportHDSigner.UPORT_ROOT_DERIVATION_PATH, prompt = "", callback = { err, newJwt ->

            assert(err).isNull()

            val verifiedPayload = runBlocking { JWTTools().verify(newJwt) }
            assert(verifiedPayload).isEqualTo(payload)

            latch.countDown()

        })
        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun can_create_token_from_map_payload() = runBlocking {
        val timeProvider = TestTimeProvider(12345678000L)
        val tested = JWTTools(timeProvider)

        val payload = mapOf<String, Any>(
                "claims" to mapOf("name" to "R Daneel Olivaw")
        )
        val signer = UportHDSignerImpl(
                appContext,
                UportHDSigner(),
                rootHandle,
                rootHandle
        )
        val issuerDID = "did:ethr:${signer.getAddress()}"

        val jwt = tested.createJWT(payload, issuerDID, signer)
        val expected = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjbGFpbXMiOnsibmFtZSI6IlIgRGFuZWVsIE9saXZhdyJ9LCJpYXQiOjEyMzQ1Njc4LCJleHAiOjEyMzQ1OTc4LCJpc3MiOiJkaWQ6ZXRocjoweDQxMjNjYmQxNDNiNTVjMDZlNDUxZmYyNTNhZjA5Mjg2YjY4N2E5NTAifQ.o6eDKYjHJnak1ylkpe9g8krxvK9UEhKf-1T0EYhH8pGyb8MjOEepRJi8DYlVEnZno0DkVYXQCf3u1i_HThBKtAA"
        assert(jwt).isEqualTo(expected)
        val (_, decodedPayload, _) = tested.decode(expected)
        assert(decodedPayload.iat).isEqualTo(12345678L)
    }

}


