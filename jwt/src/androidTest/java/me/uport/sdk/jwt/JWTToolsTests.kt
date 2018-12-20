package me.uport.sdk.jwt

/**
 * Created by aldi on 3/10/18.
 */
import android.support.test.rule.ActivityTestRule
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import com.uport.sdk.signer.encryption.KeyProtection
import com.uport.sdk.signer.importHDSeed
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.ITimeProvider
import me.uport.sdk.jwt.model.JwtPayload
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class JWTToolsTests {

    @get:Rule
    val mActivityRule: ActivityTestRule<TestDummyActivity> = ActivityTestRule(TestDummyActivity::class.java)

    private val validShareReqToken1 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMDM2NjQzMiwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vY2hhc3F1aS51cG9ydC5tZS9hcGkvdjEvdG9waWMvWG5IZnlldjUxeHNka0R0dSIsIm5ldCI6IjB4NCIsImV4cCI6MTUyMDM2NzAzMiwidHlwZSI6InNoYXJlUmVxIn0.C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"
    private val expectedShareReqPayload1 = JwtPayload(iss = "2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG", iat = 1520366432, sub = null, aud = null, exp = 1520367032, callback = "https://chasqui.uport.me/api/v1/topic/XnHfyev51xsdkDtu", type = "shareReq", net = "0x4", act = null, requested = listOf("name", "phone", "country", "avatar"), verified = null, permissions = listOf("notifications"), req = null, nad = null, dad = null, own = null, capabilities = null, claims = null, ctl = null, reg = null, rel = null, fct = null, acc = null)

    private val incomingJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb21SSlpMMjNaQ1lnYzFyWnJGVnBGWEpwV29hRUV1SlVjZiIsImlhdCI6MTUxOTM1MDI1NiwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vYXBpLnVwb3J0LnNwYWNlL29sb3J1bi9jcmVhdGVJZGVudGl0eSIsIm5ldCI6IjB4MzAzOSIsImFjdCI6ImRldmljZWtleSIsImV4cCI6MTUyMjU0MDgwMCwidHlwZSI6InNoYXJlUmVxIn0.EkqNUyrZhcDbTQl73XpL2tp470lCo2saMXzuOZ91UI2y-XzpcBMzhhSeUORnoJXJhHnkGGpshZlESWUgrbuiVQ"
    private val expectedJwtPayload = JwtPayload(iss = "2omRJZL23ZCYgc1rZrFVpFXJpWoaEEuJUcf", iat = 1519350256, sub = null, aud = null, exp = 1522540800, callback = "https://api.uport.space/olorun/createIdentity", type = "shareReq", net = "0x3039", act = "devicekey", requested = null, verified = null, permissions = listOf("notifications"), req = null, nad = null, dad = null, own = null, capabilities = null, claims = null, ctl = null, reg = null, rel = null, fct = null, acc = null)

    @Test
    fun testVerifyToken() = runBlocking {
        val shareReqPayload = JWTTools(TestTimeProvider(1520366666)).verify(validShareReqToken1)
        assertEquals(expectedShareReqPayload1, shareReqPayload)

        val incomingJwtPayload = JWTTools(TestTimeProvider(1522540300)).verify(incomingJwt)
        assertEquals(expectedJwtPayload, incomingJwtPayload)
    }

    @Test
    fun testCreateToken() {
        val activity = mActivityRule.activity
        val latch = CountDownLatch(1)

        val address = "0x4123cbd143b55c06e451ff253af09286b687a950"
        val referenceSeedPhrase = "notice suffer eagle style exclude burst write mechanic junior crater crystal seek"

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
        ensureSeedIsImported(referenceSeedPhrase)


        JWTTools().create(context = activity, payload = payload, rootHandle = address, derivationPath = UportHDSigner.UPORT_ROOT_DERIVATION_PATH, prompt = "", callback = { err, newJwt ->
            assertNull(err)

            runBlocking {
                // but we should be able to verify the newly created token

                val timeProvider = TestTimeProvider(1532095437)
                val newJwtPayload = JWTTools(timeProvider).verify(newJwt!!)
                assertNotNull(newJwtPayload)
                latch.countDown()
            }

        })
        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun create_token_from_payload() = runBlocking {
        val timeProvider = TestTimeProvider(12345678000L)
        val tested = JWTTools(timeProvider)

        val payload = mapOf<String, Any>(
                "claims" to mapOf("name" to "R Daneel Olivaw")
        )
        val baseSigner = UportHDSigner()
        val (rootHandle, _) = baseSigner.importHDSeed(mActivityRule.activity, KeyProtection.Level.SIMPLE, "notice suffer eagle style exclude burst write mechanic junior crater crystal seek")
        val signer = UportHDSignerImpl(
                mActivityRule.activity,
                baseSigner,
                rootHandle,
                rootHandle
        )
        val issuerDID = "did:ethr:${signer.getAddress()}"

        val jwt = tested.createJWT(payload, issuerDID, signer)
        val expected = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjbGFpbXMiOnsibmFtZSI6IlIgRGFuZWVsIE9saXZhdyJ9LCJpYXQiOjEyMzQ1Njc4LCJleHAiOjEyMzQ1OTc4LCJpc3MiOiJkaWQ6ZXRocjoweDQxMjNjYmQxNDNiNTVjMDZlNDUxZmYyNTNhZjA5Mjg2YjY4N2E5NTAifQ.o6eDKYjHJnak1ylkpe9g8krxvK9UEhKf-1T0EYhH8pGyb8MjOEepRJi8DYlVEnZno0DkVYXQCf3u1i_HThBKtAA"
        assertEquals(expected, jwt)
        val tt = tested.decode(expected)
        assertEquals(12345678L, tt.second.iat)
    }

    private fun ensureSeedIsImported(phrase: String) = runBlocking {
        //ensure seed is imported
        UportHDSigner().importHDSeed(mActivityRule.activity, KeyProtection.Level.SIMPLE, phrase)
    }

    @Test(expected = InvalidJWTException::class)
    fun throws_when_iat_is_in_future() {
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJkaWQ6ZXRocjoweGE5ZTMyMzJiNjFiZGI2NzI3MTJiOWFlMzMxOTUwNjlkOGQ2NTFjMWEiLCJpYXQiOjE1NDU1Njk1NDEsImV4cCI6MTU0NjA4Nzk0MSwiYXVkIjoiZGlkOmV0aHI6MHgxMDgyMDlmNDI0N2I3ZmU2NjA1YjBmNThmOTE0NWVjMzI2OWQwMTU0Iiwic3ViIjoiIn0.Bt9Frc1QabJfpXYBoU4sns8WPeRLdKU87FncgMFq1lY"

        val timeProvider = TestTimeProvider(977317692000L)

        runBlocking {
            JWTTools(timeProvider).verify(token)
        }
    }

    @Test(expected = InvalidJWTException::class)
    fun throws_when_exp_is_in_the_past() {
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJkaWQ6ZXRocjoweGE5ZTMyMzJiNjFiZGI2NzI3MTJiOWFlMzMxOTUwNjlkOGQ2NTFjMWEiLCJpYXQiOjE1NDU1Njk1NDEsImV4cCI6MTU0NjA4Nzk0MSwiYXVkIjoiZGlkOmV0aHI6MHgxMDgyMDlmNDI0N2I3ZmU2NjA1YjBmNThmOTE0NWVjMzI2OWQwMTU0Iiwic3ViIjoiIn0.Bt9Frc1QabJfpXYBoU4sns8WPeRLdKU87FncgMFq1lY"

        val timeProvider = TestTimeProvider(1576847292000L)

        runBlocking {
            JWTTools(timeProvider).verify(token)
        }
    }


}

class TestTimeProvider(private val currentTime: Long) : ITimeProvider {
    override fun now(): Long = currentTime

}


