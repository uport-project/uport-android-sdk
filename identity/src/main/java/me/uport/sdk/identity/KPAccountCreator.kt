package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection

class KPAccountCreator(private val appContext: Context) : AccountCreator {

    val signer = UportHDSigner()

    private suspend fun createOrImportAccount(networkId: String, phrase: String?): Account {
        val (handle, _) = if (phrase.isNullOrBlank()) {
            signer.createHDSeed(appContext, KeyProtection.Level.SIMPLE)
        } else {
            signer.importHDSeed(appContext, KeyProtection.Level.SIMPLE, phrase!!)
        }
        val (deviceAddress, _) = signer.computeAddressForPath(appContext,
                handle,
                Account.GENERIC_DEVICE_KEY_DERIVATION_PATH,
                "")

        return Account(
                handle,
                deviceAddress,
                networkId,
                deviceAddress,
                "",
                "",
                "",
                SignerType.KeyPair
        )
    }

    override suspend fun createAccount(networkId: String, forceRestart: Boolean): Account {
        return createOrImportAccount(networkId, null)
    }

    override suspend fun importAccount(networkId: String, seedPhrase: String, forceRestart: Boolean): Account {
        return createOrImportAccount(networkId, seedPhrase)
    }

    override suspend fun deleteAccount(handle: String) {
        signer.deleteSeed(appContext, handle)
    }

}