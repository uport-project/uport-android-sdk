package me.uport.sdk.demoapp

import android.support.test.rule.ActivityTestRule
import assertk.assertThat
import assertk.assertions.isNotEqualTo
import me.uport.sdk.demoapp.managing_jwt.SignJWTUportHDSignerActivity
import org.junit.Rule
import org.junit.Test

class SignJWTUportHDTest {

    @get:Rule
    val activityRule = ActivityTestRule(SignJWTUportHDSignerActivity::class.java)

    @Test
    fun signerIsCreated() {

        Thread.sleep(1000)

        assertThat(activityRule.activity.issuerDID).isNotEqualTo("")
    }
}