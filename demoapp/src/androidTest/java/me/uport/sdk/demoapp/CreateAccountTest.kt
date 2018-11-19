package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.hamcrest.core.StringContains.containsString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateAccountTest {

    @JvmField
    @Rule
    val activityRule = ActivityTestRule(CreateAccountActivity::class.java, true, false)


    @Before
    fun run_before_every_test() {
        activityRule.launchActivity(null)
    }

    @Test
    public fun accountIsCreated() {

        onView(withId(R.id.defaultAccountView))
                .check(matches(withText(not(containsString("ERROR")))));

    }

}