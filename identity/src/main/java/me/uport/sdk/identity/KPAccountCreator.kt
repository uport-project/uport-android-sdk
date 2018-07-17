package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection

class KPAccountCreator(private val context: Context) : AccountCreator {

    override fun createAccount(networkId: String, forceRestart: Boolean, callback: AccountCreatorCallback) {

        val signer = UportHDSigner()

        signer.createHDSeed(context, KeyProtection.Level.SIMPLE) { err, rootAddress, _ ->
            if (err != null) {
                return@createHDSeed callback(err, Account.blank)
            }
            signer.computeAddressForPath(context,
                    rootAddress,
                    Account.GENERIC_DEVICE_KEY_DERIVATION_PATH,
                    "") { ex, deviceAddress, _ ->
                if (ex != null) {
                    return@computeAddressForPath callback(err, Account.blank)
                }

                val acc = Account(
                        rootAddress,
                        deviceAddress,
                        networkId,
                        deviceAddress,
                        "",
                        "",
                        "",
                        SignerType.KeyPair
                )

                return@computeAddressForPath callback(null, acc)
            }
        }
    }

}