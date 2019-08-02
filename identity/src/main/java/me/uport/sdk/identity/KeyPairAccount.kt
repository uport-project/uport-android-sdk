package me.uport.sdk.identity

import android.content.Context
import kotlinx.serialization.Transient
import me.uport.sdk.signer.KPSigner
import me.uport.sdk.signer.Signer

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

        override val network: String,

        val signer: KPSigner

) : Account {

    override val type: AccountType
        get() = AccountType.KeyPair

    override val deviceAddress: String
        get() = signer.getAddress()

    override val publicAddress: String
        get() = signer.getAddress()

    override val handle: String
        get() = signer.getAddress()

    @Transient
    val address: String
        get() = signer.getAddress()

    override fun getSigner(context: Context): Signer = signer

    /**
     * This function generates and returns the DID associated with an account
     */
    override fun getDID(): String = "did:ethr:$publicAddress"
}
