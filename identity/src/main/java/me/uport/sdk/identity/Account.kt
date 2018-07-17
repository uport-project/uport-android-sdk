package me.uport.sdk.identity

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.squareup.moshi.Json
import com.uport.sdk.signer.UportHDSigner
import me.uport.mnid.MNID
import me.uport.sdk.core.Signer
import me.uport.sdk.identity.endpoints.moshi

data class Account(

        @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
        @Json(name = "uportRoot")
        val handle: String,

        @Json(name = "devKey")
        val deviceAddress: String,

        @Json(name = "network")
        val network: String,

        @Json(name = "proxy")
        val publicAddress: String,

        @Json(name = "manager")
        val identityManagerAddress: String,

        @Json(name = "txRelay")
        val txRelayAddress: String,

        @Json(name = "fuelToken")
        val fuelToken: String,

        @Json(name = "signerType")
        val signerType: SignerType = SignerType.KeyPair
) {

    val address : String
        get() = getMnid()

    fun getMnid() = MNID.encode(network, publicAddress)

    fun toJson(pretty: Boolean = false): String = adapter.indent(if (pretty) "  " else "").toJson(this)

    fun getSigner(context: Context): Signer = UportHDSignerWrapper(context, UportHDSigner(), rootAddress = handle, deviceAddress = deviceAddress)

    companion object {

        /**
         * TODO: should be used to derive SDK KeyPairs instead of the UPORT_ROOT
         */
        const val GENERIC_DEVICE_KEY_DERIVATION_PATH = "m/44'/60'/0'/0"
        const val GENERIC_RECOVERY_DERIVATION_PATH = "m/44'/60'/0'/1"

        val blank = Account("", "", "", "", "", "", "", SignerType.KeyPair)

        private val adapter = moshi.adapter<Account>(Account::class.java)

        fun fromJson(serializedAccount: String?): Account? {
            if (serializedAccount == null || serializedAccount.isEmpty()) {
                return null
            }

            return adapter.fromJson(serializedAccount)
        }
    }

}