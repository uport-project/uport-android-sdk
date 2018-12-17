package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImportKeyTest {

    @get:Rule
    val activityRule = ActivityTestRule(ImportKeyActivity::class.java)

    @Test
    fun keyIsCreated() {

        onView(withId(R.id.inner_simple_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.submit_btn_two)).perform(click())

        onView(withId(R.id.text_input_area)).check(matches(not(withText(""))))

        onView(withId(R.id.submit_btn_one)).perform(click())

        onView(withId(R.id.item_details_one)).check(matches(not(withText(""))))

        onView(withId(R.id.item_details_two)).check(matches(not(withText(""))))

        onView(withId(R.id.error_details)).check(matches(withText("")))

    }

}