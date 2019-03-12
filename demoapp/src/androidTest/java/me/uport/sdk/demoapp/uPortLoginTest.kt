package me.uport.sdk.demoapp

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents.intending
import android.support.test.espresso.intent.matcher.IntentMatchers.toPackage
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import me.uport.sdk.demoapp.request_flows.uPortLoginActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class uPortLoginTest {

    @get:Rule
    val intentsTestRule = IntentsTestRule(uPortLoginActivity::class.java)

    @Before
    fun stub_uport_intent() {
        // Build the result to return when the activity is launched.
        val resultData = Intent()
        val redirectUri = "https://uport-project.github.io/uport-android-sdk/callbacks#access_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1NTIzODM5MzYsImV4cCI6MTU1MjQ3MDMzNiwiYXVkIjoiZGlkOmV0aHI6MHhjZjAzZGQwYTg5NGVmNzljYjViNjAxYTQzYzRiMjVlM2FlNGM2N2VkIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvaGpmdGhLb2sxV2d2OFhoZTNKcjFEUnh3ckRjM1JhMVpaIiwib3duIjp7Im5hbWUiOiJ1UG9ydCBVc2VyIn0sInJlcSI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSkZVekkxTmtzdFVpSjkuZXlKallXeHNZbUZqYXlJNkltaDBkSEJ6T2k4dmRYQnZjblF0Y0hKdmFtVmpkQzVuYVhSb2RXSXVhVzh2ZFhCdmNuUXRZVzVrY205cFpDMXpaR3N2WTJGc2JHSmhZMnR6SWl3aWNtVnhkV1Z6ZEdWa0lqcGJJbTVoYldVaVhTd2lZV04wSWpvaVoyVnVaWEpoYkNJc0luUjVjR1VpT2lKemFHRnlaVkpsY1NJc0ltbGhkQ0k2TVRVMU1qTTRNemt5Tnl3aVpYaHdJam94TlRVeU16ZzBOVEkzTENKcGMzTWlPaUprYVdRNlpYUm9jam93ZUdObU1ETmtaREJoT0RrMFpXWTNPV05pTldJMk1ERmhORE5qTkdJeU5XVXpZV1UwWXpZM1pXUWlmUS4wVTJHakcwaDAwTDRIU0EwQWhwRGZ6U09LSGFqRUFkNEFqWEwteFZMUXFMM1VNYnZlSVRsS0ttVG5WdEE4Y1M3cndBaU92VnF2WnBIaFdRUk8zb2NfZ0EiLCJpc3MiOiJkaWQ6ZXRocjoweDNmZjI1MTE3YzBlMTcwY2E1MzBiZDU4OT"
        resultData.putExtra("redirect_uri", redirectUri)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // Set up result stubbing when an intent is sent to uport mobile app.
        intending(toPackage("com.uportmobile")).respondWith(result)
    }

    @Test
    fun uport_login_intent_is_sent() {

        // User starts the uport login activity.
        onView(withId(R.id.btn_send_request)).perform(click())

        // Check if other request flow buttons visible after successful login
        onView(withId(R.id.btn_verified_claim)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_personal_signature)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_typed_data)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_ethereum_transaction)).check(matches(isDisplayed()))
    }
}

