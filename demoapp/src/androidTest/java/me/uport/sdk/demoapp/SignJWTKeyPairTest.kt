package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignJWTKeyPairTest {

    @JvmField
    @Rule
    val activityRule = ActivityTestRule(SignJWTKeyPairSignerActivity::class.java)

    @Test
    fun keyIsCreated() {

        Espresso.onView(ViewMatchers.withId(R.id.create_key_btn)).perform(ViewActions.click())

        assertNotNull(activityRule.activity.signedJWT)
    }
}