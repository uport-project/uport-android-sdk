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
data class HDAccount(

        override val handle: String,

        override val deviceAddress: String,

        override val network: String,

        override val publicAddress: String,

        @Optional
        @SerialName("isDefault")
        val isDefault: Boolean? = false

) : Account {

    @Transient
    val address: String
        get() = getMnid()

    override val type: AccountType
        get() = AccountType.KeyPair

    fun toJson(pretty: Boolean = false): String = if (pretty) Json.indented.stringify(HDAccount.serializer(), this) else Json.stringify(HDAccount.serializer(), this)

    override fun getSigner(context: Context): Signer = UportHDSignerImpl(context, UportHDSigner(), rootAddress = handle, deviceAddress = deviceAddress)

    // This function generates and returns the DID associated with an account
    override fun getDID(): String = "did:ethr:$publicAddress"

    companion object {

        val blank = HDAccount("", "", "", "")

        fun fromJson(serializedAccount: String?): HDAccount? {
            if (serializedAccount == null || serializedAccount.isEmpty()) {
                return null
            }

            return Json.parse(HDAccount.serializer(), serializedAccount)
        }
    }
}