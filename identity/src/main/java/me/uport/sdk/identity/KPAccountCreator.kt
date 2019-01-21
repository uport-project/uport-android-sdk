package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSigner.Companion.GENERIC_DEVICE_KEY_DERIVATION_PATH
import com.uport.sdk.signer.computeAddressForPath
import com.uport.sdk.signer.createHDSeed
import com.uport.sdk.signer.encryption.KeyProtection
import com.uport.sdk.signer.importHDSeed

/**
 * [Account] manager backed by a [UportHDSigner] that creates and uses key-pairs
 * derived from a seed phrase.
 *
 * The seed is encrypted using AndroidKeyStore at rest
 */
class KPAccountCreator(private val appContext: Context) : AccountCreator {

    val signer = UportHDSigner()

    private suspend fun createOrImportAccount(networkId: String, phrase: String?): Account {
        val (handle, _) = if (phrase.isNullOrBlank()) {
            signer.createHDSeed(appContext, KeyProtection.Level.SIMPLE)
        } else {
            signer.importHDSeed(appContext, KeyProtection.Level.SIMPLE, phrase)
        }
        val (deviceAddress, _) = signer.computeAddressForPath(appContext,
                handle,
                GENERIC_DEVICE_KEY_DERIVATION_PATH,
                "")

        return Account(
                handle,
                deviceAddress,
                networkId,
                deviceAddress,
                "",
                "",
                "",
                AccountType.KeyPair
        )
    }

    override suspend fun createAccount(networkId: String, forceRecreate: Boolean): Account {
        return createOrImportAccount(networkId, null)
    }

    override suspend fun importAccount(networkId: String, seedPhrase: String, forceRecreate: Boolean): Account {
        return createOrImportAccount(networkId, seedPhrase)
    }

    override suspend fun deleteAccount(handle: String) {
        signer.deleteSeed(appContext, handle)
    }

}