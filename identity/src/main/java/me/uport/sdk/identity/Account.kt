package me.uport.sdk.identity

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.uport.sdk.signer.Signer
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import me.uport.mnid.MNID

interface Account {

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @SerialName("uportRoot")
    val handle: String

    @SerialName("devKey")
    val deviceAddress: String

    @SerialName("network")
    val network: String

    @SerialName("proxy")
    val publicAddress: String

    fun getMnid() = MNID.encode(network, publicAddress)

    fun getSigner(context: Context): Signer

    fun getDID(): String
}