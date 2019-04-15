package me.uport.sdk.identity

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.uport.sdk.signer.KPSigner
import com.uport.sdk.signer.Signer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

/**
 * This is an [Account] implementation that is backed by a KeyPair Signer.
 *
 * This account type does not support serialization
 *
 * This account type is still experimental and should only be used in test a environment
 *
 * API volatility: __high__
 */

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

        val signer: KPSigner,

        @SerialName("signerType")
        override val type: AccountType = AccountType.KeyPair

) : Account {

    @Transient
    val address: String
        get() = publicAddress

    override fun getSigner(context: Context): Signer = signer

    /**
     * This function generates and returns the DID associated with an account
     */
    override fun getDID(): String = "did:ethr:$publicAddress"
}
