package me.uport.sdk.transport

import android.app.Activity
import android.support.test.espresso.intent.rule.IntentsTestRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


class IntentSenderAndroidTest {

    //only used for testing
    class DummyActivity : Activity()


    @JvmField
    @Rule
    var intentsTestRule = IntentsTestRule<DummyActivity>(DummyActivity::class.java)

    @Test
    fun setup_is_ok() {
        assertTrue(true)
    }

}