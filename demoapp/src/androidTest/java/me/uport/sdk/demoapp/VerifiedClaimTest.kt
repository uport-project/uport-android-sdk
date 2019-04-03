package me.uport.sdk.demoapp

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.IntentFilter
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents.intending
import android.support.test.espresso.intent.matcher.IntentMatchers.hasAction
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import me.uport.sdk.demoapp.request_flows.VerifiedClaimRequestActivity
import me.uport.sdk.transport.RequestDispatchActivity
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ro.mirceanistor.testutil.ViewMatcherIdlingRule

class VerifiedClaimTest {

    @get:Rule
    val intentsTestRule = IntentsTestRule(VerifiedClaimRequestActivity::class.java)

    @get:Rule
    val viewMatcherIdlingRule = ViewMatcherIdlingRule(allOf(withId(R.id.progress), isDisplayed()))

    private var instrumentation: Instrumentation? = null
    private var monitor: Instrumentation.ActivityMonitor? = null
    private val filter: IntentFilter? = null

    @Before
    fun run_before_every_test() {
        instrumentation = InstrumentationRegistry.getInstrumentation()
        monitor = instrumentation?.addMonitor(filter, null, true)
    }

    @After
    fun run_after_every_test() {
        instrumentation?.removeMonitor(monitor)
    }

    @Before
    fun stub_intent() {
        // Mock the ActivityResult to be returned to VerifiedClaimRequestActivity in onActivityResult.
        val resultData = Intent()
        val redirectUri = "https://uport-project.github.io/uport-android-sdk/callbacks#access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1NTI0MzU4MzAsImV4cCI6MTU1MjUyMjIzMCwic3ViIjoiZGlkOmV0aHI6MHgzZmYyNTExN2MwZTE3MGNhNTMwYmQ1ODkxODk5YzE4Mzk0NGRiNDMxIiwiY2xhaW0iOnsiY2l0aXplbiBvZiBDbGV2ZXJsYW5kIjp0cnVlfSwiaXNzIjoiZGlkOmV0aHI6MHgzZmYyNTExN2MwZTE3MGNhNTMwYmQ1ODkxODk5YzE4Mzk0NGRiNDMxIn0.vLENXTTjnbPGTDgSMg4KvH7srlYaXupJrxOZ1Ic_uBzp-paAHoCy2f0Qcfn60RQ0vXuD9Cb3p9tJyvBfkNYUdAA"
        resultData.putExtra("redirect_uri", redirectUri)
        val response = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // This sets up what the ActivityResult to be returned whenever an intent with the below action is launched
        intending(hasAction(RequestDispatchActivity.ACTION_DISPATCH_REQUEST)).respondWith(response)
    }

    @Test
    fun claim_successfully_signed_by_uport() {

        // User clicks on send request.
        onView(withId(R.id.send_request)).perform(click())

        // Response detail textview will no longer be empty
        onView(withId(R.id.response_details)).check(matches(not(withText(""))))
    }
}