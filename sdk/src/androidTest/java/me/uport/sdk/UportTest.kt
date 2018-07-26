package me.uport.sdk

import android.os.Looper
import android.support.test.InstrumentationRegistry
import kotlinx.coroutines.experimental.runBlocking
import me.uport.sdk.core.Networks
import me.uport.sdk.identity.Account
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class UportTest {

    @Before
    fun run_before_every_test() {
        val config = Uport.Configuration()
                .setApplicationContext(InstrumentationRegistry.getTargetContext())
        Uport.initialize(config)
    }

    @Test
    fun default_account_gets_updated() {

        val tested = Uport

        tested.defaultAccount = null

        runBlocking {
            val acc = tested.createAccount(Networks.rinkeby)
            assertNotNull(acc)
            assertNotEquals(Account.blank, acc)

            assertNotNull(tested.defaultAccount)
        }
    }

    @Test
    fun account_completion_called_on_main_thread() {
        val latch = CountDownLatch(1)
        Uport.createAccount(Networks.rinkeby) { _, _ ->
            assertTrue(Looper.getMainLooper().isCurrentThread)
            latch.countDown()
        }

        latch.await(15, TimeUnit.SECONDS)
    }

}