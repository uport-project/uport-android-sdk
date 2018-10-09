package com.uport.sdk.signer

import android.content.Context
import android.util.Base64
import com.uport.sdk.signer.UportHDSigner.Companion.GENERIC_DEVICE_KEY_DERIVATION_PATH
import org.kethereum.model.SignatureData

/**
 * Wraps a [UportHDSigner] into a [Signer] interface.
 *
 * The HD key provider it wraps needs an activity context for keys that are linked to user-auth.
 * This object should not be long-lived
 */
class UportHDSignerImpl(
        private val context: Context,
        private val uportHDSigner: UportHDSigner,
        private val rootAddress: String,
        private val deviceAddress: String
) : Signer {

    override fun signETH(
            rawMessage: ByteArray,
            callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        return uportHDSigner.signTransaction(
                context, //FIXME: Not cool hiding the context like this... may lead to leaks
                rootAddress,
                GENERIC_DEVICE_KEY_DERIVATION_PATH,
                Base64.encodeToString(rawMessage, Base64.DEFAULT),
                "",
                callback)
    }

    override fun signJWT(rawPayload: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        return uportHDSigner.signJwtBundle(
                context, //FIXME: Not cool hiding the context like this... may lead to leaks
                rootAddress,
                GENERIC_DEVICE_KEY_DERIVATION_PATH,
                Base64.encodeToString(rawPayload, Base64.DEFAULT),
                "",
                callback)
    }

    /**
     * returns the address that corresponds to the device keypair
     */
    override fun getAddress(): String = deviceAddress

}