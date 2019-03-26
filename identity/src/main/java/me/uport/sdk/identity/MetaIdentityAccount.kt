package me.uport.sdk.identity

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.uport.sdk.signer.Signer
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

@Serializable
data class MetaIdentityAccount(

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        @SerialName("uportRoot")
        override val handle: String,

        @SerialName("devKey")
        override val deviceAddress: String,

        @SerialName("network")
        override val network: String,

        @SerialName("proxy")
        override val publicAddress: String,

        @SerialName("manager")
        val identityManagerAddress: String,

        @SerialName("txRelay")
        val txRelayAddress: String,

        @SerialName("fuelToken")
        val fuelToken: String,

        @SerialName("signerType")
        val type: AccountType = AccountType.MetaIdentityManager,

        @Optional
        @SerialName("isDefault")
        val isDefault: Boolean? = false

) : Account {

    @Transient
    val address: String
        get() = getMnid()

    fun toJson(pretty: Boolean = false): String = if (pretty) Json.indented.stringify(MetaIdentityAccount.serializer(), this) else Json.stringify(MetaIdentityAccount.serializer(), this)

    override fun getSigner(context: Context): Signer = UportHDSignerImpl(context, UportHDSigner(), rootAddress = handle, deviceAddress = deviceAddress)

    // This function generates and returns the DID associated with an account
    override fun getDID(): String = "did:uport:${getMnid()}"

    companion object {

        val blank = MetaIdentityAccount("", "", "", "", "", "", "")

        fun fromJson(serializedAccount: String?): MetaIdentityAccount? {
            if (serializedAccount == null || serializedAccount.isEmpty()) {
                return null
            }

            return Json.parse(MetaIdentityAccount.serializer(), serializedAccount)
        }
    }
}