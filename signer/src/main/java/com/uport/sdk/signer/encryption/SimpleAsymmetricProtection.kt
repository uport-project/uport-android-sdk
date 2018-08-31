package com.uport.sdk.signer.encryption

import android.content.Context
import com.uport.sdk.signer.encryption.AndroidKeyStoreHelper.generateWrappingKey

class SimpleAsymmetricProtection : KeyProtection() {

    override val alias = "__simple_asymmetric_key_alias__"

    override fun genKey(context: Context) {
        generateWrappingKey(context, alias)
    }

    override suspend fun encrypt(context: Context, purpose: String, blob: ByteArray): String {
        return encryptRaw(blob, alias)
    }

    override suspend fun decrypt(context: Context, purpose: String, ciphertext: String): ByteArray {
        return decryptRaw(ciphertext, alias)
    }

}