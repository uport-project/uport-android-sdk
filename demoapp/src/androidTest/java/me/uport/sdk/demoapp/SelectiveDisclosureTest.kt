package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelectiveDisclosureTest {

    @get:Rule
    val activityRule = ActivityTestRule(SelectiveDisclosureActivity::class.java)

    @Test
    fun signedJwtCreated() {

        onView(withId(R.id.send_request)).perform(click())

        assertNotNull(activityRule.activity.signedJWT)
    }

}