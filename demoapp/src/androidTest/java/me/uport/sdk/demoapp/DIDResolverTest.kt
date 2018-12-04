package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DIDResolverTest {

    @get:Rule
    val activityRule = ActivityTestRule(DIDResolverActivity::class.java)

    @Test
    fun DID_document_is_returned() {

        onView(withId(R.id.resolve_btn)).perform(click())

        Thread.sleep(10000)

        onView(withId(R.id.ethr_did_doc)).check(matches(withText(containsString("Ethr DID Document"))))

        onView(withId(R.id.uport_did_doc)).check(matches(withText(containsString("Uport DID Document"))))
    }
}