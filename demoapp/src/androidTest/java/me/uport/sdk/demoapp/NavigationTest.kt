package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import me.uport.sdk.demoapp.key_protection.FingerPrintProtectionActivity
import me.uport.sdk.demoapp.key_protection.KeyGuardProtectionActivity
import me.uport.sdk.demoapp.key_protection.KeyProtectionListActivity
import me.uport.sdk.demoapp.managing_jwt.SignJWTKeyPairSignerActivity
import me.uport.sdk.demoapp.managing_jwt.SignJWTListActivity
import me.uport.sdk.demoapp.managing_jwt.SignJWTUportHDSignerActivity
import org.hamcrest.CoreMatchers.anything
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val activityRule = IntentsTestRule(MainListActivity::class.java)

    @Test
    fun listIsDisplayed() {
        onView(withId(me.uport.sdk.demoapp.R.id.item_list)).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun navigateAllActivities() {

        // check if CreateAccountActivity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0).perform(click())
        intended(hasComponent(CreateAccountActivity::class.java.name))
        pressBack()

        // check if CreateKeyActivity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(1).perform(click())
        intended(hasComponent(CreateKeyActivity::class.java.name))
        pressBack()

        // check if ImportKeyActivity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(2).perform(click())
        intended(hasComponent(ImportKeyActivity::class.java.name))
        pressBack()

        // check if KeyProtectionListActivity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(3).perform(click())
        intended(hasComponent(KeyProtectionListActivity::class.java.name))

        // check if KeyGuardProtectionActivity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0).perform(click())
        intended(hasComponent(KeyGuardProtectionActivity::class.java.name))
        pressBack()

        // check if FingerPrintProtectionActivity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(1).perform(click())
        intended(hasComponent(FingerPrintProtectionActivity::class.java.name))
        pressBack()

        // back to MainListActivity
        pressBack()

        // check if SignJWTListActivity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(4).perform(click())
        intended(hasComponent(SignJWTListActivity::class.java.name))

        // check if SignJWTKeyPairSignerActivity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0).perform(click())
        intended(hasComponent(SignJWTKeyPairSignerActivity::class.java.name))
        pressBack()

        // check if SignJWTUportHDSignerActivity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(1).perform(click())
        intended(hasComponent(SignJWTUportHDSignerActivity::class.java.name))
        pressBack()

        // back to MainListActivity
        pressBack()
    }
}