package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import io.mockk.coEvery
import io.mockk.spyk
import me.uport.sdk.core.Networks
import me.uport.sdk.demoapp.managing_jwt.VerifyJWTActivity
import me.uport.sdk.ethrdid.EthrDIDDocument
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.universaldid.UniversalDID
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import ro.mirceanistor.testutil.ViewMatcherIdlingRule

class VerifyJWTTest {

    @get:Rule
    val activityRule = ActivityTestRule(VerifyJWTActivity::class.java)

    @get:Rule
    val viewMatcherIdlingRule = ViewMatcherIdlingRule(allOf(withId(R.id.progress), isDisplayed()))

    @Test
    fun can_verify_jwt_in_activity() {

        val ddo = """
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

        val rpc = JsonRPC(Networks.rinkeby.rpcUrl)
        val ethrResolver = spyk(EthrDIDResolver(rpc)) {
            coEvery { resolve(eq("did:ethr:0x4123cbd143b55c06e451ff253af09286b687a950")) } returns EthrDIDDocument.fromJson(ddo)
        }
        UniversalDID.registerResolver(ethrResolver)

        onView(withId(R.id.verify_btn)).perform(click())

        onView(withId(R.id.jwtPayload)).check(matches(not(withText(""))))
    }
}