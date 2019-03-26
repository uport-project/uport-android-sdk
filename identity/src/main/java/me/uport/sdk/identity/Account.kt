package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.Signer
import me.uport.mnid.MNID

/**
 * Abstraction of the common properties and methods for various account types
 *
 * Each account implement this interface, then override its properties and methods
 */
interface Account {

    val handle: String

    val deviceAddress: String

    val network: String

    val publicAddress: String

    fun getMnid() = MNID.encode(network, publicAddress)

    // this will return the signer of the implementing account
    fun getSigner(context: Context): Signer

    // this will return the DID associated with the implementing account
    fun getDID(): String
}