package me.uport.sdk.identity

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.uport.sdk.signer.Signer
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

class HDAccount(

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        @SerialName("uportRoot")
        val handle: String,

        @SerialName("devKey")
        val deviceAddress: String,

        override val publicAddress: String,

        override val network: String

) : AccountInterface {

    override fun getDID(): String {
        return "did:ethr:$publicAddress"
    }

    override fun getSigner(context: Context): Signer {
        return UportHDSignerImpl(context, UportHDSigner(), rootAddress = handle, deviceAddress = deviceAddress)
    }

    @Transient
    val address: String
        get() = getMnid()
}