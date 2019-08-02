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
import me.uport.sdk.demoapp.request_flows.TypedDataRequestActivity
import me.uport.sdk.transport.RequestDispatchActivity
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ro.mirceanistor.testutil.ViewMatcherIdlingRule

class TypedDataTests {

    @get:Rule
    val intentsTestRule = IntentsTestRule(TypedDataRequestActivity::class.java)

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
        // Mock the ActivityResult to be returned to TypedDataRequestActivity in onActivityResult.
        val resultData = Intent()
        val redirectUri = "https://uport-project.github.io/uport-android-sdk/callbacks#typedDataSig=eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1NTI0ODk1NDEsImV4cCI6MTU1MjU3NTk0MSwidHlwZSI6ImVpcDcxMlJlc3AiLCJyZXF1ZXN0Ijp7InR5cGVzIjp7IkVJUDcxMkRvbWFpbiI6W3sibmFtZSI6Im5hbWUiLCJ0eXBlIjoic3RyaW5nIn0seyJuYW1lIjoidmVyc2lvbiIsInR5cGUiOiJzdHJpbmcifSx7Im5hbWUiOiJjaGFpbklkIiwidHlwZSI6InVpbnQyNTYifSx7Im5hbWUiOiJ2ZXJpZnlpbmdDb250cmFjdCIsInR5cGUiOiJhZGRyZXNzIn1dLCJQZXJzb24iOlt7Im5hbWUiOiJuYW1lIiwidHlwZSI6InN0cmluZyJ9LHsibmFtZSI6IndhbGxldCIsInR5cGUiOiJhZGRyZXNzIn1dLCJNYWlsIjpbeyJuYW1lIjoiZnJvbSIsInR5cGUiOiJQZXJzb24ifSx7Im5hbWUiOiJ0byIsInR5cGUiOiJQZXJzb24ifSx7Im5hbWUiOiJjb250ZW50cyIsInR5cGUiOiJzdHJpbmcifV19LCJkb21haW4iOnsibmFtZSI6IkV0aGVyIE1haWwiLCJ2ZXJzaW9uIjoiMSIsImNoYWluSWQiOiIxIiwidmVyaWZ5aW5nQ29udHJhY3QiOiIweENjQ0NjY2NjQ0NDQ2NDQ0NDQ0NjQ2NDY2NDY0NDQ2NDY2NjY2NjY0MifSwibWVzc2FnZSI6eyJjb250ZW50cyI6IkhlbGxvIEJvYiIsImZyb20iOnsibmFtZSI6IkNvdyIsIndhbGxldCI6IjB4Q0QyYTNkOUY5MzhFMTNDRDk0N0VjMDVBYkM3RkU3MzREZjhERDgyNiJ9LCJ0byI6eyJuYW1lIjoidG8iLCJ3YWxsZXQiOiIweGJCYkJCQkJiYkJCQmJiYkJiYkJiYmJiQkJiQmJiYmJCYkJiYkJCYkIifX0sInByaW1hcnlUeXBlIjoiTWFpbCJ9LCJzaWduYXR1cmUiOnsidiI6MjgsInIiOiIweDlkZjliMDBiOTJiMzYwZDM0NDU2MTg5MDc1MzQ3NzhhOWU1MGRiMDY5ZmQ4NGNmODJiNThmYTFhN2YzNjBkZTUiLCJzIjoiMHg1YjQyOGE2MjVkNDAxZjdmMzQ1N2YxYzk1YjM5MzQ5NTBlZjI3M2JhNjY5MTY3MzNhZGNjYWIyMGEwYTA3YzUxIn0sImlzcyI6ImRpZDpldGhyOjB4M2ZmMjUxMTdjMGUxNzBjYTUzMGJkNTg5MTg5OWMxODM5NDRkYjQzMSJ9.kEwFlT-hQF0eAhv9uS0A_qmc13J0-w3nTjk6ZMbVWOhxGqp_4HKzfYKJuHyKsWJKq2NMzX2jVcvx7Thfqnrc7wE"
        resultData.putExtra("redirect_uri", redirectUri)
        val response = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // This sets up what the ActivityResult to be returned whenever an intent with the below action is launched
        intending(hasAction(RequestDispatchActivity.ACTION_DISPATCH_REQUEST)).respondWith(response)
    }

    @Test
    fun typed_data_successfully_signed_by_uport_account() {

        // User clicks on send request.
        onView(withId(R.id.send_request)).perform(click())

        // Response detail TextView will no longer be empty
        onView(withId(R.id.response_details)).check(matches(not(withText(""))))
    }
}