package me.uport.sdk.jwt

/**
 * Created by aldi on 3/10/18.
 */
import android.content.Context
import android.support.test.InstrumentationRegistry
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.HttpClient
import me.uport.sdk.core.Networks
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jwt.model.JwtPayload
import me.uport.sdk.testhelpers.TestTimeProvider
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDResolver
import org.junit.Assert.assertNull
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

        val http = mockk<HttpClient>()
        val rpc = spyk(JsonRPC(Networks.rinkeby.rpcUrl, http))
        UniversalDID.registerResolver(UportDIDResolver(rpc))

        //language=json
        coEvery { rpc.ethCall(any(), any()) } returns """{"jsonrpc":"2.0","id":1,"result":"0x807a7cb8b670125774d70cf94d35e2355bb18bb51cf604f376c9996057f92fbf"}"""
        //language=json
        coEvery { http.urlGet(any()) } returns """{"@context":"http://schema.org","@type":"Person","publicKey":"0x04e8989d1826cd6258906cfaa71126e2db675eaef47ddeb9310ee10db69b339ab960649e1934dc1e1eac1a193a94bd7dc5542befc5f7339845265ea839b9cbe56f","publicEncKey":"k8q5G4YoIMP7zvqMC9q84i7xUBins6dXGt8g5H007F0="}"""

        val latch = CountDownLatch(1)

        val ownObj = mapOf(
                Pair("name", "Identity 1"),
                Pair("phone", "10000"),
                Pair("country", "")
        )
        val capabilities = listOf("eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb3VmRUEzNXk3R2lBcGNkeUw4N0xwNXpUVjdOUk5DRjZISCIsImlhdCI6MTUyMjEwMDM5NiwiYXVkIjoiMm9lWHVmSEdEcFU1MWJmS0JzWkRkdTdKZTl3ZUozcjdzVkciLCJ0eXBlIjoibm90aWZpY2F0aW9ucyIsInZhbHVlIjoiYXJuOmF3czpzbnM6dXMtd2VzdC0yOjExMzE5NjIxNjU1ODplbmRwb2ludC9HQ00vdVBvcnQvMTkxZTBiMjctZWFmZi0zMWVkLTk4NGUtNTg2ZjU1OWYzMDEyIiwiZXhwIjoxNTIzMzk2Mzk2fQ.XyjR2iM0ZUgvolEhcP9n50g7JAJ9VMjS5_ASqj29-riOV1sEnYiLcH44E2joPo-clcFoA0owW19OcyRLpur50g")
        @Suppress("UNUSED_VARIABLE")
        val expectedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb3VmRUEzNXk3R2lBcGNkeUw4N0xwNXpUVjdOUk5DRjZISCIsImlhdCI6MTUyMjEwMDM5NiwiYXVkIjoiMm9lWHVmSEdEcFU1MWJmS0JzWkRkdTdKZTl3ZUozcjdzVkciLCJ0eXBlIjoic2hhcmVSZXNwIiwibmFkIjoiMm91ZkVBMzV5N0dpQXBjZHlMODdMcDV6VFY3TlJOQ0Y2SEgiLCJvd24iOnsibmFtZSI6IklkZW50aXR5IDEiLCJwaG9uZSI6IjEwMDAwIiwiY291bnRyeSI6IiIsImF2YXRhciI6bnVsbH0sInJlcSI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSkZVekkxTmtzaWZRLmV5SnBjM01pT2lJeWIyVllkV1pJUjBSd1ZUVXhZbVpMUW5OYVJHUjFOMHBsT1hkbFNqTnlOM05XUnlJc0ltbGhkQ0k2TVRVeU1qRXdNRE00T0N3aWNtVnhkV1Z6ZEdWa0lqcGJJbTVoYldVaUxDSndhRzl1WlNJc0ltTnZkVzUwY25raUxDSmhkbUYwWVhJaVhTd2ljR1Z5YldsemMybHZibk1pT2xzaWJtOTBhV1pwWTJGMGFXOXVjeUpkTENKallXeHNZbUZqYXlJNkltaDBkSEJ6T2k4dlpHVnRieTUxY0c5eWRDNXRaUzhqSWl3aWJtVjBJam9pTUhnMElpd2laWGh3SWpveE5USXlNVEF3T1RnNExDSjBlWEJsSWpvaWMyaGhjbVZTWlhFaWZRLjNMa2J6NHpHMGtjRGFPa2hUaEZZOWlHaHZxYWdLbVhIb3MxNUpIR2wzSXZTbjYtZlhKSlRmNnhzWUNMX3dZREhJM0djdGJOVUhoVHVDWWtKUkY1TlB3IiwiY2FwYWJpbGl0aWVzIjpbImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSkZVekkxTmtzaWZRLmV5SnBjM01pT2lJeWIzVm1SVUV6TlhrM1IybEJjR05rZVV3NE4weHdOWHBVVmpkT1VrNURSalpJU0NJc0ltbGhkQ0k2TVRVeU1qRXdNRE01Tml3aVlYVmtJam9pTW05bFdIVm1TRWRFY0ZVMU1XSm1TMEp6V2tSa2RUZEtaVGwzWlVvemNqZHpWa2NpTENKMGVYQmxJam9pYm05MGFXWnBZMkYwYVc5dWN5SXNJblpoYkhWbElqb2lZWEp1T21GM2N6cHpibk02ZFhNdGQyVnpkQzB5T2pFeE16RTVOakl4TmpVMU9EcGxibVJ3YjJsdWRDOUhRMDB2ZFZCdmNuUXZNVGt4WlRCaU1qY3RaV0ZtWmkwek1XVmtMVGs0TkdVdE5UZzJaalUxT1dZek1ERXlJaXdpWlhod0lqb3hOVEl6TXprMk16azJmUS5YeWpSMmlNMFpVZ3ZvbEVoY1A5bjUwZzdKQUo5Vk1qUzVfQVNxajI5LXJpT1Yxc0VuWWlMY0g0NEUyam9Qby1jbGNGb0Ewb3dXMTlPY3lSTHB1cjUwZyJdLCJleHAiOjE1MjIxODY3OTZ9.jOnbFK1b56Oeey1JMbRMm9ef8QtR5XGz8KqjKh9PUKoCAaa8mVCT5VYwqFS5g4ZfGVe0DS7EjbQS2Gsv5_WeLQ"
        val payload = JwtPayload(iss = "2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH",
                iat = 1522100396,
                aud = "2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG",
                exp = 1545314699,
                type = "shareResp",
                nad = "2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH",
                own = ownObj,
                req = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMjEwMDM4OCwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vZGVtby51cG9ydC5tZS8jIiwibmV0IjoiMHg0IiwiZXhwIjoxNTIyMTAwOTg4LCJ0eXBlIjoic2hhcmVSZXEifQ.3Lkbz4zG0kcDaOkhThFY9iGhvqagKmXHos15JHGl3IvSn6-fXJJTf6xsYCL_wYDHI3GctbNUHhTuCYkJRF5NPw",
                capabilities = capabilities)

        //XXX: even though it's deprecated, it doesn't hurt to keep this test around until it's completely removed
        @Suppress("DEPRECATION")
        JWTTools().create(context = appContext, payload = payload, rootHandle = rootHandle, derivationPath = UportHDSigner.UPORT_ROOT_DERIVATION_PATH, prompt = "", callback = { err, newJwt ->
            assertNull(err)

            runBlocking {
                // but we should be able to verify the newly created token

                val timeProvider = TestTimeProvider(1532095437000L)
                val newJwtPayload = JWTTools(timeProvider).verify(newJwt!!)
                assert(newJwtPayload).isNotNull()
                latch.countDown()
            }

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
        val decoded = tested.decode(expected)
        assert(decoded.second.iat).isEqualTo(12345678L)
    }

}


