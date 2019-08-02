package me.uport.sdk.identity

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import me.uport.sdk.signer.Signer

/**
 * This is an [Account] implementation that is backed by a key pair derived from a seed.
 */
@Serializable
data class HDAccount(

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        @SerialName("uportRoot")
        override val handle: String,

        @SerialName("devKey")
        override val deviceAddress: String,

        @SerialName("network")
        override val network: String,

        @SerialName("proxy")
        override val publicAddress: String,

        @SerialName("signerType")
        override val type: AccountType = AccountType.HDKeyPair

) : Account {

    @Transient
    val address: String
        get() = publicAddress

    /**
     * serializes account
     */
    fun toJson(pretty: Boolean = false): String = if (pretty) Json.indented.stringify(serializer(), this) else Json.stringify(serializer(), this)

    override fun getSigner(context: Context): Signer = UportHDSignerImpl(context, UportHDSigner(), rootAddress = handle, deviceAddress = deviceAddress)

    /**
     * This function generates and returns the DID associated with an account
     */
    override fun getDID(): String = "did:ethr:$publicAddress"

    companion object {

        val blank = HDAccount("", "", "", "")

        /**
         * de-serializes account
         */
        fun fromJson(serializedAccount: String?): HDAccount? {
            if (serializedAccount == null || serializedAccount.isEmpty()) {
                return null
            }

            return Json.nonstrict.parse(serializer(), serializedAccount)
        }
    }
}
