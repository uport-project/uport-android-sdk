package me.uport.sdk.identity

import android.content.Context
import android.support.test.InstrumentationRegistry
import kotlinx.coroutines.experimental.runBlocking
import me.uport.sdk.core.Networks
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class KPAccountCreatorTest {

    private lateinit var appContext: Context

    @Before
    fun run_before_every_test() {
        appContext = InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun createAccount() {
        runBlocking {
            val account = KPAccountCreator(appContext).createAccount(Networks.rinkeby.network_id)
            assertNotNull(account)
            assertNotEquals(Account.blank, account)
            assertTrue(account.type == AccountType.KeyPair)
            assertTrue(account.address.isNotEmpty())
            assertTrue(account.publicAddress.isNotEmpty())
            assertTrue(account.deviceAddress.isNotEmpty())
        }
    }

    @Test
    fun importAccount() {

        val referenceSeedPhrase = "vessel ladder alter error federal sibling chat ability sun glass valve picture"

        runBlocking {
            val account = KPAccountCreator(appContext).importAccount(Networks.rinkeby.network_id, referenceSeedPhrase)
            assertNotNull(account)
            assertNotEquals(Account.blank, account)
            assertTrue(account.type == AccountType.KeyPair)
            assertEquals("2opxPamUQoLarQHAoVDKo2nDNmfQLNCZif4", account.address)
            assertEquals("0x847e5e3e8b2961c2225cb4a2f719d5409c7488c6", account.publicAddress)
            assertEquals("0x847e5e3e8b2961c2225cb4a2f719d5409c7488c6", account.deviceAddress)
            assertEquals("0x794adde0672914159c1b77dd06d047904fe96ac8", account.handle)
        }
    }
}