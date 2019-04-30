@file:Suppress("DEPRECATION")

package me.uport.sdk

import android.content.Context
import android.support.test.InstrumentationRegistry
import assertk.all
import assertk.assert
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.uport.sdk.signer.UportHDSigner
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.Networks
import me.uport.sdk.identity.HDAccount
import org.junit.Before
import org.junit.Test

class UportTest {

    private lateinit var context: Context

    @Before
    fun run_before_every_test() {
        context = InstrumentationRegistry.getTargetContext()
        val config = Configuration()
                .setApplicationContext(context)

        Uport.initialize(config)
    }

    @Test
    fun default_account_gets_updated() {

        val tested = Uport

        tested.defaultAccount?.let { tested.deleteAccount(it) }

        runBlocking {
            val acc = tested.createAccount(Networks.rinkeby.networkId)

            assert(acc).all {
                isNotNull()
                isNotEqualTo(HDAccount.blank)
            }

            assert(tested.defaultAccount).isNotNull()
        }
    }


    @Test
    fun account_can_be_imported() {
        val tested = Uport
        val referenceSeedPhrase = "vessel ladder alter error federal sibling chat ability sun glass valve picture"

        tested.defaultAccount?.let { tested.deleteAccount(it) }

        runBlocking {
            val account = tested.createAccount(Networks.rinkeby.networkId, referenceSeedPhrase)
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

    @Test
    fun imported_account_can_be_retrieved() {
        val tested = Uport
        val referenceSeedPhrase = "vessel ladder alter error federal sibling chat ability sun glass valve picture"

        runBlocking {
            val refAccount = tested.createAccount(Networks.rinkeby.networkId, referenceSeedPhrase)
            assert(tested.getAccount("0x794adde0672914159c1b77dd06d047904fe96ac8")).isEqualTo(refAccount)
        }
    }

    @Test
    fun can_delete_account() {
        val tested = Uport

        tested.defaultAccount?.let { tested.deleteAccount(it) }

        runBlocking {
            val account = tested.createAccount(Networks.rinkeby.networkId)
            assert(account).all {
                isNotNull()
                isNotEqualTo(HDAccount.blank)
            }

            val root = account.handle
            tested.deleteAccount(root)

            assert(UportHDSigner().allHDRoots(context)).doesNotContain(root)
            assert(tested.defaultAccount).isNull()
        }
    }
}