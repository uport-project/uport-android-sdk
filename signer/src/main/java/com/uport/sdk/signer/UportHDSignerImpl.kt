package com.uport.sdk.signer

import android.content.Context
import com.uport.sdk.signer.UportHDSigner.Companion.UPORT_ROOT_DERIVATION_PATH
import me.uport.sdk.core.Signer
import me.uport.sdk.core.toBase64
import org.kethereum.model.SignatureData

/**
 * Wraps a [UportHDSigner] into a [Signer] interface.
 *
 * The HD key provider it wraps needs an activity context for keys that are linked to user-auth.
 *
 *
 * **This object should not be long-lived**
 *
 * FIXME: This implementation only uses the UPORT_ROOT_DERIVATION_PATH derivation path. The path should be a parameter and the resulting device address should be calculated after the key is unlocked.
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
                UPORT_ROOT_DERIVATION_PATH, //FIXME: path should be configurable
                rawMessage.toBase64(),
                "",
                callback)
    }

    override fun signJWT(rawPayload: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        return uportHDSigner.signJwtBundle(
                context, //FIXME: Not cool hiding the context like this... may lead to leaks
                rootAddress,
                UPORT_ROOT_DERIVATION_PATH, //FIXME: path should be configurable
                rawPayload.toBase64(),
                "",
                callback)
    }

    /**
     * returns the address that corresponds to the device keypair
     */
    override fun getAddress(): String = deviceAddress

}