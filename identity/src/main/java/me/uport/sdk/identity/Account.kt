@file:Suppress("unused")

package me.uport.sdk.identity

import android.content.Context
import me.uport.sdk.core.Signer

/**
 * Abstraction of the common properties and methods for various account types
 *
 * Each account implement this interface, then override its properties and methods
 */
interface Account {

    /**
     * Represents an alias to the account.
     * This [handle] will be used to refer to and interact with the [Account]
     */
    val handle: String

    /**
     * Represents the ethereum address that signs transactions.
     * This address is derived from a private key that this [Account] can control
     * (as opposed to it representing an external entity with an unknown key).
     */
    val deviceAddress: String

    /**
     * The network ID this account is associated with.
     */
    val network: String

    /**
     * Represents the public facing address of this [Account].
     * In most cases this is identical to the [deviceAddress] or an encoding of it,
     * but it can also represent a contract address
     * or an address of a key that is not directly controlled by this [Account].
     */
    val publicAddress: String

    /**
     * The type of signer that is backing this Account
     */
    val type: AccountType

    /**
     * this will return the [Signer] of the implementing account
     */
    fun getSigner(context: Context): Signer

    /**
     * this will return the DID associated with the implementing account
     */
    fun getDID(): String
}
