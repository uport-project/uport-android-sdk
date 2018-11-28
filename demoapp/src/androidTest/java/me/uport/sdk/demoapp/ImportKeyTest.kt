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

        onView(withId(R.id.seed_phrase_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.generate_seed_phrase)).perform(click())

        onView(withId(R.id.input_seed_phrase)).check(matches(not(withText(""))))

        onView(withId(R.id.create_key_btn)).perform(click())

        onView(withId(R.id.public_key_details)).check(matches(not(withText(""))))

        onView(withId(R.id.address_details)).check(matches(not(withText(""))))

        onView(withId(R.id.error_text)).check(matches(withText("")))

    }

}