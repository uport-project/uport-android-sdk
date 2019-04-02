package me.uport.sdk.identity

import android.content.Context
import android.support.test.InstrumentationRegistry
import assertk.all
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.Networks
import org.junit.Before
import org.junit.Test

//TODO: move this to JVM test with robolectric once a solution is found for https://github.com/robolectric/robolectric/issues/1518
class HDAccountCreatorTest {

    private lateinit var appContext: Context

    @Before
    fun run_before_every_test() {
        appContext = InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun createAccount() {
        runBlocking {
            val account = HDAccountCreator(appContext).createAccount(Networks.rinkeby.networkId)

            assert(account).all {
                isNotNull()
                isNotEqualTo(HDAccount.blank)
            }

            assert(account.address).isNotEmpty()
            assert(account.publicAddress).isNotEmpty()
            assert(account.deviceAddress).isNotEmpty()
        }
    }

    @Test
    fun importAccount() {

        val referenceSeedPhrase = "vessel ladder alter error federal sibling chat ability sun glass valve picture"

        runBlocking {
            val account = HDAccountCreator(appContext).importAccount(Networks.rinkeby.networkId, referenceSeedPhrase)
            assert(account).all {
                isNotNull()
                isNotEqualTo(HDAccount.blank)
            }
            assert(account.address).isEqualTo("0x847e5e3e8b2961c2225cb4a2f719d5409c7488c6")
            assert(account.publicAddress).isEqualTo("0x847e5e3e8b2961c2225cb4a2f719d5409c7488c6")
            assert(account.deviceAddress).isEqualTo("0x847e5e3e8b2961c2225cb4a2f719d5409c7488c6")
            assert(account.handle).isEqualTo("0x794adde0672914159c1b77dd06d047904fe96ac8")
        }
    }
}