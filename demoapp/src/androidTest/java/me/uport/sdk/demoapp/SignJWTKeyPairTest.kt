package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import assertk.assertThat
import assertk.assertions.isNotNull
import me.uport.sdk.demoapp.managing_jwt.SignJWTKeyPairSignerActivity
import org.junit.Rule
import org.junit.Test

class SignJWTKeyPairTest {

    @get:Rule
    val activityRule = ActivityTestRule(SignJWTKeyPairSignerActivity::class.java)

    @Test
    fun keyIsCreated() {

        onView(withId(R.id.submit_btn_one)).perform(click())

        assertThat(activityRule.activity.signedJWT).isNotNull()
    }
}