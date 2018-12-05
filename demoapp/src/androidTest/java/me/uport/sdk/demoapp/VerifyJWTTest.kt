package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerifyJWTTest {

    @get:Rule
    val activityRule = ActivityTestRule(VerifyJWTActivity::class.java)

    @Test
    fun successfulVerification() {

        onView(withId(R.id.verify_btn)).perform(click())

        onView(withId(R.id.jwtPayload)).check(matches(CoreMatchers.not(withText(""))))
    }
}