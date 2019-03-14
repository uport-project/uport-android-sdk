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
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import me.uport.sdk.demoapp.request_flows.PersonalSignRequestActivity
import me.uport.sdk.transport.RequestDispatchActivity
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PersonalSignatureTest {

    @get:Rule
    val intentsTestRule = IntentsTestRule(PersonalSignRequestActivity::class.java)

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
        // Mock the ActivityResult to be returned to PersonalSignRequestActivity in onActivityResult.
        val resultData = Intent()
        val redirectUri = "https://uport-project.github.io/uport-android-sdk/callbacks#personalSig=eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1NTI0ODczMjIsImV4cCI6MTU1MjU3MzcyMiwidHlwZSI6InBlcnNvbmFsU2lnblJlc3AiLCJkYXRhIjoiVGhpcyBpcyBhIG1lc3NhZ2UgSSBuZWVkIHlvdSB0byBzaWduIiwic2lnbmF0dXJlIjp7InYiOjI3LCJyIjoiMHgzYjExMmUxMzZiMDI3MDZiNzNjZjhjYjFmNDI0ZDBjZjg1NWExZTM5OTU0ZGM3OTJjZTllYzFmNDkwZmJlZjBiIiwicyI6IjB4N2U0YTViMWU4MWM4YjNjZjJkMTY1ODdjNTUwMjUyNmNjOTM3OWU4OWUwOWI2NTNlMWI2NWUzOThkYzg5MjBjNiJ9LCJpc3MiOiJkaWQ6ZXRocjoweDNmZjI1MTE3YzBlMTcwY2E1MzBiZDU4OTE4OTljMTgzOTQ0ZGI0MzEifQ.5FtBavglhq1aeMtTM644cY5d1ij-0QvlMMLMQhmwVpUtCKOJRBjj3psXrPPuEm28_ZqAV0ISTyH7a2w1osjyzgE"
        resultData.putExtra("redirect_uri", redirectUri)
        val response = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // This sets up what the ActivityResult to be returned whenever an intent with the below action is launched
        intending(hasAction(RequestDispatchActivity.ACTION_DISPATCH_REQUEST)).respondWith(response)
    }

    @Test
    fun data_successfully_signed_by_uport_account() {

        // User clicks on send request.
        onView(withId(R.id.send_request)).perform(click())

        Thread.sleep(5000)

        // Response detail TextView will no longer be empty
        onView(withId(R.id.response_details)).check(matches(not(withText(""))))
    }
}