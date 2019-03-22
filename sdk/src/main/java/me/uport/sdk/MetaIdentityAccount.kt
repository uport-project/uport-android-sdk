package me.uport.sdk

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.uport.sdk.signer.Signer
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import kotlinx.serialization.SerialName
import me.uport.sdk.core.Networks
import me.uport.sdk.identity.AccountInterface
import me.uport.sdk.signer.MetaIdentitySigner
import me.uport.sdk.signer.TxRelaySigner

/**
 * AccountInterface Implementation for [MetaIdentityAccount] which are backed up by [MetaIdentitySigner]
 */
class MetaIdentityAccount(

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        @SerialName("uportRoot")
        val handle: String,

        @SerialName("devKey")
        val deviceAddress: String,

        override val publicAddress: String,

        override val network: String,

        @SerialName("manager")
        val identityManagerAddress: String,

        @SerialName("txRelay")
        val txRelayAddress: String,

        @SerialName("fuelToken")
        val fuelToken: String

) : AccountInterface {

    override fun getDID(): String {
        return "did:uport:${getMnid()}"
    }

    override fun getSigner(context: Context): Signer {
        val signer = UportHDSignerImpl(context, UportHDSigner(), rootAddress = handle, deviceAddress = deviceAddress)
        val relaySigner = TxRelaySigner(signer, Networks.get(network))
        return MetaIdentitySigner(relaySigner, publicAddress, identityManagerAddress)
    }
}