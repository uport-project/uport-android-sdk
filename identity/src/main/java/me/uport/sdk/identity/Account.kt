package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.Signer
import me.uport.mnid.MNID

interface Account {

    val handle: String

    val deviceAddress: String

    val network: String

    val publicAddress: String

    fun getMnid() = MNID.encode(network, publicAddress)

    fun getSigner(context: Context): Signer

    fun getDID(): String
}