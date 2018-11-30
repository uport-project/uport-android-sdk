package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.*
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.anything
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NavigationTest {


    @JvmField
    @Rule
    val activityRule = IntentsTestRule(MainListActivity::class.java)

    @Test
    fun listIsDisplayed() {
        onView(withId(me.uport.sdk.demoapp.R.id.item_list)).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun navigateAllActivities() {

        // check if Create Account Activity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0).perform(click())
        intended(hasComponent(CreateAccountActivity::class.java.name))
        pressBack()

        // check if Create Key Activity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(1).perform(click())
        intended(hasComponent(CreateKeyActivity::class.java.name))
        pressBack()

        // check if Import Key Activity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(2).perform(click())
        intended(hasComponent(ImportKeyActivity::class.java.name))
        pressBack()

        // check if Key Protection Activity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(3).perform(click())
        intended(hasComponent(KeyProtectionListActivity::class.java.name))

        // check if KeyGuard Protection Activity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0).perform(click())
        intended(hasComponent(KeyGuardProtectionActivity::class.java.name))
        pressBack()

        // check if FingerPrint Protection Activity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(1).perform(click())
        intended(hasComponent(FingerPrintProtectionActivity::class.java.name))
        pressBack()

        // back to Main List Activity
        pressBack()

        // check if Sign JWT List Activity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(4).perform(click())
        intended(hasComponent(SignJWTListActivity::class.java.name))

        // check if Sign JWT with KeyPairSigner Activity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0).perform(click())
        intended(hasComponent(SignJWTKeyPairSignerActivity::class.java.name))
        pressBack()

        // check if Sign JWT with UportHDSigner Activity is launched
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(1).perform(click())
        intended(hasComponent(SignJWTUportHDSignerActivity::class.java.name))
        pressBack()

        // back to Main List Activity
        pressBack()
    }
}