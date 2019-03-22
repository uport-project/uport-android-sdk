package me.uport.sdk

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.uport.sdk.signer.Signer
import kotlinx.serialization.SerialName
import me.uport.sdk.identity.AccountInterface

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
        throw Error("Not yet implemented")
    }
}