package me.uport.sdk

import android.support.test.InstrumentationRegistry
import kotlinx.coroutines.experimental.runBlocking
import me.uport.sdk.core.Networks
import me.uport.sdk.identity.Account
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

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

}