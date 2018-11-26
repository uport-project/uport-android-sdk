package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SignJWTUportHDTest {

    @JvmField
    @Rule
    val activityRule = ActivityTestRule(SignJWTUportHDSignerActivity::class.java)

    @Test
    fun keyIsCreated() {

        Espresso.onView(ViewMatchers.withId(R.id.create_key_btn)).perform(ViewActions.click())

        assertFalse(activityRule.activity.issuerDID!!.contains("null"))
    }
}