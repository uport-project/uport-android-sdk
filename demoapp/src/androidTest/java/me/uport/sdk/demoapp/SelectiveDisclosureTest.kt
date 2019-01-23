package me.uport.sdk.demoapp

import android.app.Instrumentation
import android.content.Intent
import android.content.IntentFilter
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.intent.matcher.UriMatchers
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.withId
import assertk.assert
import assertk.assertions.isNotNull
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SelectiveDisclosureTest {

    @get:Rule
    var intentsTestRule = IntentsTestRule<SelectiveDisclosureActivity>(SelectiveDisclosureActivity::class.java, true, true)

    // used to block intent from actual broadcast
    private val filter: IntentFilter? = null
    private var instrumentation: Instrumentation? = null
    private var monitor: Instrumentation.ActivityMonitor? = null

    @Before
    fun run_before_every_test() {
        instrumentation = InstrumentationRegistry.getInstrumentation()
        monitor = instrumentation?.addMonitor(filter, null, true)
    }

    @After
    fun run_after_every_test() {
        instrumentation?.removeMonitor(monitor)
    }

    @Test
    fun signedJwtCreated() {

        onView(withId(R.id.send_request)).perform(click())

        assert((intentsTestRule.activity.requestJWT)).isNotNull()

        Intents.intended(Matchers.allOf(
                IntentMatchers.hasAction(Matchers.equalTo(Intent.ACTION_VIEW)),
                IntentMatchers.hasCategories(Matchers.hasItem(Matchers.equalTo(Intent.CATEGORY_BROWSABLE))),
                IntentMatchers.hasData(Matchers.allOf(
                        UriMatchers.hasHost(Matchers.equalTo("id.uport.me")),
                        UriMatchers.hasPath(Matchers.containsString("/req/${intentsTestRule.activity.requestJWT}"))
                )
                )))
    }

}