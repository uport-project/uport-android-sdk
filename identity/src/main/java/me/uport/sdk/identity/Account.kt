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

    /**
    * Represents an alias to the account. This [handle] will be used to refer to and interact with the [Account]
    */
    val handle: String

    val deviceAddress: String

    val network: String

    val publicAddress: String

    fun getMnid() = MNID.encode(network, publicAddress)

    val isDefault: Boolean?

    val type: AccountType

    /**
     * this will return the signer of the implementing account
     */
    fun getSigner(context: Context): Signer

    /**
     * this will return the DID associated with the implementing account
     */
    fun getDID(): String
}
