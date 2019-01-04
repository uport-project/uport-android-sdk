package me.uport.sdk.identity

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.uport.sdk.signer.Signer
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JSON
import me.uport.mnid.MNID

@Serializable
data class Account(

        @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
        @SerialName("uportRoot")
        val handle: String,

        @SerialName("devKey")
        val deviceAddress: String,

        @SerialName("network")
        val network: String,

        @SerialName("proxy")
        val publicAddress: String,

        @SerialName("manager")
        val identityManagerAddress: String,

        @SerialName("txRelay")
        val txRelayAddress: String,

        @SerialName("fuelToken")
        val fuelToken: String,

        @SerialName("signerType")
        val type: AccountType = AccountType.KeyPair,

        @Optional
        @SerialName("isDefault")
        val isDefault: Boolean? = false
) {

    @Transient
    val address: String
        get() = getMnid()

    fun getMnid() = MNID.encode(network, publicAddress)

    fun toJson(pretty: Boolean = false): String = if (pretty) JSON.indented.stringify(Account.serializer(), this) else JSON.stringify(Account.serializer(), this)

    fun getSigner(context: Context): Signer = UportHDSignerImpl(context, UportHDSigner(), rootAddress = handle, deviceAddress = deviceAddress)

    /**
     * This function generates the DID associated with an account based on the account type.
     *
     * Limitation: The current implementation only covers KeyPair and MetaIdentity Account types
     * @returns the DID as a string
     * @throws IllegalStateException if there is no implementation for the Account Type
     */
    fun getDID(): String {
        return when (type) {
            AccountType.MetaIdentityManager -> "did:uport:${getMnid()}"
            AccountType.KeyPair -> "did:ethr:$publicAddress"
            else -> throw IllegalStateException("A DID could not be created for the Account Type $type")
        }
    }

    companion object {

        val blank = Account("", "", "", "", "", "", "", AccountType.KeyPair)

        fun fromJson(serializedAccount: String?): Account? {
            if (serializedAccount == null || serializedAccount.isEmpty()) {
                return null
            }

            return JSON.parse(Account.serializer(), serializedAccount)
        }
    }

}