package me.uport.sdk.identity

import android.content.Context
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

        override val handle: String,

        override val deviceAddress: String,

        override val network: String,

        override val publicAddress: String,

        @SerialName("manager")
        val identityManagerAddress: String,

        @SerialName("txRelay")
        val txRelayAddress: String,

        @SerialName("fuelToken")
        val fuelToken: String,

        @Optional
        @SerialName("isDefault")
        val isDefault: Boolean? = false

) : Account {

    @Transient
    val address: String
        get() = getMnid()

    @Transient
    val type: AccountType
        get() = AccountType.MetaIdentityManager

    fun toJson(pretty: Boolean = false): String = if (pretty) Json.indented.stringify(MetaIdentityAccount.serializer(), this) else Json.stringify(MetaIdentityAccount.serializer(), this)

    override fun getSigner(context: Context): Signer = UportHDSignerImpl(context, UportHDSigner(), rootAddress = handle, deviceAddress = deviceAddress)

    /**
     * This function generates the DID associated with an account based on the account type.
     *
     * Limitation: The current implementation only covers KeyPair and MetaIdentity HDAccount types
     * @returns the DID as a string
     * @throws IllegalStateException if there is no implementation for the HDAccount Type
     */
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