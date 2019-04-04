package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.core.StringContains.containsString
import org.junit.Rule
import org.junit.Test
import ro.mirceanistor.testutil.ViewMatcherIdlingRule

class CreateAccountTest {

    @get:Rule
    val activityRule = ActivityTestRule(CreateAccountActivity::class.java)

    @get:Rule
    val viewMatcherIdlingRule = ViewMatcherIdlingRule(allOf(withId(R.id.progress), isDisplayed()))

    @Test
    fun accountIsCreated() {
        onView(withId(R.id.defaultAccountView))
                .check(matches(withText(not(containsString("ERROR")))))
    }

}