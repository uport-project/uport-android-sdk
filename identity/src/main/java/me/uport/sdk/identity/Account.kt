package me.uport.sdk.identity

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.squareup.moshi.Json
import com.uport.sdk.signer.Signer
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import me.uport.mnid.MNID
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
        val signerType: SignerType = SignerType.KeyPair,

        @Json(name = "isDefault")
        val isDefault: Boolean? = false
) {

    val address: String
        get() = getMnid()

    fun getMnid() = MNID.encode(network, publicAddress)

    fun toJson(pretty: Boolean = false): String = adapter.indent(if (pretty) "  " else "").toJson(this)

    fun getSigner(context: Context): Signer = UportHDSignerImpl(context, UportHDSigner(), rootAddress = handle, deviceAddress = deviceAddress)

    companion object {

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