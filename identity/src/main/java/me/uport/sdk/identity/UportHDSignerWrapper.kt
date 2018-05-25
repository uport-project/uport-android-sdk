package me.uport.sdk.identity

import android.content.Context
import android.util.Base64
import com.uport.sdk.signer.UportHDSigner
import me.uport.sdk.core.Signer
import org.kethereum.model.SignatureData

/**
 * Wrapps the [uportHDSigner] into a [Signer] interface
 */
class UportHDSignerWrapper(
        private val context: Context,
        private val uportHDSigner: UportHDSigner,
        private val rootAddress: String,
        private val deviceAddress: String
) : Signer {

    override fun signMessage(
            rawMessage: ByteArray,
            callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        return uportHDSigner.signTransaction(
                context, //FIXME: Not cool hiding the context like this... may lead to leaks
                rootAddress,
                Account.GENERIC_DEVICE_KEY_DERIVATION_PATH,
                Base64.encodeToString(rawMessage, Base64.DEFAULT),
                "",
                callback)
    }

    /**
     * returns the address that corresponds to the device keypair
     */
    override fun getAddress(): String = deviceAddress

}