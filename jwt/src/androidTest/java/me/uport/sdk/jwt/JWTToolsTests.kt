package me.uport.sdk.jwt

/**
 * Created by aldi on 3/10/18.
 */
import android.support.test.rule.ActivityTestRule
import android.util.Log
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import me.uport.sdk.jwt.model.JwtPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.assertNotNull

class JWTToolsTests {

    @Rule
    @JvmField
    val mActivityRule: ActivityTestRule<TestDummyActivity> = ActivityTestRule(TestDummyActivity::class.java)

    private val validShareReqToken1 = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMDM2NjQzMiwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vY2hhc3F1aS51cG9ydC5tZS9hcGkvdjEvdG9waWMvWG5IZnlldjUxeHNka0R0dSIsIm5ldCI6IjB4NCIsImV4cCI6MTUyMDM2NzAzMiwidHlwZSI6InNoYXJlUmVxIn0.C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"
    private val expectedShareReqPayload1 = JwtPayload(iss = "2oeXufHGDpU51bfKBsZDdu7Je9weJ3r7sVG", iat = 1520366432, sub = null, aud = null, exp = 1520367032, callback = "https://chasqui.uport.me/api/v1/topic/XnHfyev51xsdkDtu", type = "shareReq", net = "0x4", act = null, requested = listOf("name", "phone", "country", "avatar"), verified = null, permissions = listOf("notifications"), req = null, nad = null, dad = null, own = null, capabilities = null, claims = null, ctl = null, reg = null, rel = null, fct = null, acc = null)

    private val incomingJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb21SSlpMMjNaQ1lnYzFyWnJGVnBGWEpwV29hRUV1SlVjZiIsImlhdCI6MTUxOTM1MDI1NiwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vYXBpLnVwb3J0LnNwYWNlL29sb3J1bi9jcmVhdGVJZGVudGl0eSIsIm5ldCI6IjB4MzAzOSIsImFjdCI6ImRldmljZWtleSIsImV4cCI6MTUyMjU0MDgwMCwidHlwZSI6InNoYXJlUmVxIn0.EkqNUyrZhcDbTQl73XpL2tp470lCo2saMXzuOZ91UI2y-XzpcBMzhhSeUORnoJXJhHnkGGpshZlESWUgrbuiVQ"
    private val incomingJwtPayload = JwtPayload(iss = "2omRJZL23ZCYgc1rZrFVpFXJpWoaEEuJUcf", iat = 1519350256, sub = null, aud = null, exp = 1522540800, callback = "https://api.uport.space/olorun/createIdentity", type = "shareReq", net = "0x3039", act = "devicekey", requested = null, verified = null, permissions = listOf("notifications"), req = null, nad = null, dad = null, own = null, capabilities = null, claims = null, ctl = null, reg = null, rel = null, fct = null, acc = null)

    @Test
    fun testVerifyToken() {
        val latch = CountDownLatch(2)
        JWTTools().verify(validShareReqToken1, { err, actualPayload ->
            assertNull(err)
            assertEquals(expectedShareReqPayload1, actualPayload)
            Log.d("herehere", "verify1")
            latch.countDown()
        })


        JWTTools().verify(incomingJwt, { err, actualPayload ->
            assertNull(err)
            assertEquals(incomingJwtPayload, actualPayload)
            Log.d("herehere", "verify2")
            latch.countDown()
        })

        latch.await()
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
                exp = 1522186796,
                type = "shareResp",
                nad = "2oufEA35y7GiApcdyL87Lp5zTV7NRNCF6HH",
                own = ownObj,
                req = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMjEwMDM4OCwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vZGVtby51cG9ydC5tZS8jIiwibmV0IjoiMHg0IiwiZXhwIjoxNTIyMTAwOTg4LCJ0eXBlIjoic2hhcmVSZXEifQ.3Lkbz4zG0kcDaOkhThFY9iGhvqagKmXHos15JHGl3IvSn6-fXJJTf6xsYCL_wYDHI3GctbNUHhTuCYkJRF5NPw",
                capabilities = capabilities)
        ensureSeedIsImported(referenceSeedPhrase)


        JWTTools().create(context = activity, payload = payload, address = address, derivationPath = UportHDSigner.UPORT_ROOT_DERIVATION_PATH, prompt = "", callback = { err, newJwt ->
            assertNull(err)

            // but we should be able to verify the newly created token
            JWTTools().verify(newJwt!!) { ex, verifiedPayload ->
                assertNull(ex)
                //XXX: Comparing payloads directly fails because of serialization differences
                //The expectedToken contains an `"avatar" : null` while the verifiedPayload has `avatar : ""`
                //moshi doesn't encode nulls by default so it is normal to fail on equals
                //BUT, the rest of the fields seem to match
                //
                // test data or code should be adjusted so that this can pass too:
                //val (_, expectedPayload, _) = JWTTools().decode(expectedToken)
                assertNotNull(verifiedPayload)
                assertEquals(payload, verifiedPayload!!)
                latch.countDown()
            }

        })
        latch.await()
    }

    private fun ensureSeedIsImported(phrase: String) {
        //ensure seed is imported
        val latch = CountDownLatch(1)
        UportHDSigner().importHDSeed(mActivityRule.activity, KeyProtection.Level.SIMPLE, phrase, { err, _, _ ->
            assertNull(err)
            latch.countDown()
        })
        latch.await()
    }

}

