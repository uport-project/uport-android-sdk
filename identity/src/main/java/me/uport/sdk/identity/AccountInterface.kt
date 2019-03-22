package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.Signer
import kotlinx.serialization.SerialName
import me.uport.mnid.MNID


/**
 * Abstraction for Accounts
 */
interface AccountInterface {

    //TODO: deprecate previous [Account] class and replace with this interface

    @SerialName("proxy")
    val publicAddress: String

    @SerialName("network")
    val network: String

    // This function is overridden by the implementing class and returns the DID associated with the Account
    fun getDID() : String

    fun getSigner(context: Context) : Signer

    fun getMnid() = MNID.encode(network, publicAddress)
}