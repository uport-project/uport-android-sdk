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
import me.uport.sdk.demoapp.request_flows.EthereumTransactionActivity
import me.uport.sdk.transport.RequestDispatchActivity
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ro.mirceanistor.testutil.ViewMatcherIdlingRule

class TransactionRequestTest {

    @get:Rule
    val intentsTestRule = IntentsTestRule(EthereumTransactionActivity::class.java)

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
        // Mock the ActivityResult to be returned to EthereumTransactionActivity in onActivityResult.
        val resultData = Intent()
        val redirectUri = "https://uport-project.github.io/uport-android-sdk/callbacks#tx=6146ccf6a66d994f7c363db875e31ca35581450a4bf6d3be6cc9ac79233a69d0"
        resultData.putExtra("redirect_uri", redirectUri)
        val response = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // This sets up what the ActivityResult to be returned whenever an intent with the below action is launched
        intending(hasAction(RequestDispatchActivity.ACTION_DISPATCH_REQUEST)).respondWith(response)
    }

    @Test
    fun transaction_request_successfully_approved_by_uport_account() {
        // User clicks on send request.
        onView(withId(R.id.send_request)).perform(click())

        // Response detail TextView will no longer be empty
        onView(withId(R.id.response_details)).check(matches(not(withText(""))))
    }
}