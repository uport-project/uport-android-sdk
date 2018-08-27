package me.uport.sdk

import android.content.Context
import android.os.Looper
import android.support.test.InstrumentationRegistry
import com.uport.sdk.signer.UportHDSigner
import kotlinx.coroutines.experimental.runBlocking
import me.uport.sdk.core.Networks
import me.uport.sdk.identity.Account
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class UportTest {

    lateinit var context : Context

    @Before
    fun run_before_every_test() {
        context = InstrumentationRegistry.getTargetContext()
        val config = Uport.Configuration()
                .setApplicationContext(context)

        Uport.initialize(config)
    }

    @Test
    fun default_account_gets_updated() {

        val tested = Uport

        tested.defaultAccount?.let{ tested.deleteAccount(it)}

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

    @Test
    fun account_can_be_imported() {
        val tested = Uport
        val referenceSeedPhrase = "vessel ladder alter error federal sibling chat ability sun glass valve picture"

        tested.defaultAccount?.let{ tested.deleteAccount(it)}

        runBlocking {
            val account = tested.createAccount(Networks.rinkeby, referenceSeedPhrase)
            assertNotNull(account)
            assertNotEquals(Account.blank, account)
            assertEquals("2opxPamUQoLarQHAoVDKo2nDNmfQLNCZif4", account.address)
            assertEquals("0x847e5e3e8b2961c2225cb4a2f719d5409c7488c6", account.publicAddress)
            assertEquals("0x847e5e3e8b2961c2225cb4a2f719d5409c7488c6", account.deviceAddress)
            assertEquals("0x794adde0672914159c1b77dd06d047904fe96ac8", account.handle)
        }
    }

    @Test
    fun can_delete_account() {
        val tested = Uport

        tested.defaultAccount?.let{ tested.deleteAccount(it)}

        runBlocking {
            val account = tested.createAccount(Networks.rinkeby)
            assertNotNull(account)
            assertNotEquals(Account.blank, account)

            val root = account.handle
            tested.deleteAccount(root)

            assertFalse(UportHDSigner().allHDRoots(context).contains(root))
            assertNull(tested.defaultAccount)
        }
    }

}