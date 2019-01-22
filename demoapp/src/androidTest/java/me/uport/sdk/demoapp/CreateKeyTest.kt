package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test

class CreateKeyTest {

    @get:Rule
    val activityRule = ActivityTestRule(CreateKeyActivity::class.java)

    @Test
    fun keyIsCreated() {

        onView(withId(R.id.submit_btn_one)).perform(click())

        onView(withId(R.id.item_details_one)).check(matches(not(withText(""))))

        onView(withId(R.id.item_details_two)).check(matches(not(withText(""))))

        onView(withId(R.id.error_details)).check(matches(withText("")))

    }

}