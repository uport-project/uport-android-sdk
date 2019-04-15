package me.uport.sdk.identity

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.uport.sdk.signer.KPSigner
import com.uport.sdk.signer.Signer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

/**
 * This is an [Account] implementation that is backed by a KeyPair Signer.
 */
@Serializable
data class KeyPairAccount(

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        @SerialName("uportRoot")
        override val handle: String,

        @SerialName("devKey")
        override val deviceAddress: String,

        @SerialName("network")
        override val network: String,

        @SerialName("proxy")
        override val publicAddress: String,

        @Transient
        val signer: KPSigner,

        @SerialName("signerType")
        override val type: AccountType = AccountType.KeyPair

) : Account {

    @Transient
    val address: String
        get() = publicAddress

    /**
     * serializes account
     */
    fun toJson(pretty: Boolean = false): String = if (pretty) Json.indented.stringify(KeyPairAccount.serializer(), this) else Json.stringify(KeyPairAccount.serializer(), this)

    override fun getSigner(context: Context): Signer = signer

    /**
     * This function generates and returns the DID associated with an account
     */
    override fun getDID(): String = "did:ethr:$publicAddress"

    companion object {

        /**
         * de-serializes account
         */
        fun fromJson(serializedAccount: String?): KeyPairAccount? {
            if (serializedAccount == null || serializedAccount.isEmpty()) {
                return null
            }

            return Json.nonstrict.parse(KeyPairAccount.serializer(), serializedAccount)
        }
    }
}
