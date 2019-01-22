package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import org.hamcrest.Matchers.not
import org.hamcrest.core.StringContains.containsString
import org.junit.Rule
import org.junit.Test

class CreateAccountTest {

    @get:Rule
    val activityRule = ActivityTestRule(CreateAccountActivity::class.java)

    @Test
    fun accountIsCreated() {
        onView(withId(R.id.defaultAccountView)).check(matches(withText(not(containsString("ERROR")))))
    }

}