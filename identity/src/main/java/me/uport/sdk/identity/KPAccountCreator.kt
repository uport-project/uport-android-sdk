package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class KPAccountCreator(private val appContext: Context) : AccountCreator {

    override fun createAccount(networkId: String, forceRestart: Boolean, callback: AccountCreatorCallback) {
        launch {
            val signer = UportHDSigner()
            try {
                val (handle, _) = signer.createHDSeed(appContext, KeyProtection.Level.SIMPLE)
                val (deviceAddress, _) = signer.computeAddressForPath(appContext,
                        handle,
                        Account.GENERIC_DEVICE_KEY_DERIVATION_PATH,
                        "")
                val account = Account(
                        handle,
                        deviceAddress,
                        networkId,
                        deviceAddress,
                        "",
                        "",
                        "",
                        SignerType.KeyPair
                )

                launch(UI) { callback(null, account) }
            } catch (err: Exception) {
                launch(UI) { callback(err, Account.blank) }
            }

        }
    }

}