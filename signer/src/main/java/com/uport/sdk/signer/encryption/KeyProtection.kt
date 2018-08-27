@file:Suppress("DEPRECATION")

package com.uport.sdk.signer.encryption

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import com.uport.sdk.signer.DecryptionCallback
import com.uport.sdk.signer.EncryptionCallback
import com.uport.sdk.signer.encryption.AndroidKeyStoreHelper.getWrappingCipher
import com.uport.sdk.signer.hasMarshmallow
import com.uport.sdk.signer.packCiphertext
import com.uport.sdk.signer.unpackCiphertext
import javax.crypto.BadPaddingException
import javax.crypto.Cipher.*
import javax.crypto.IllegalBlockSizeException


/**
 * Describes the functionality of encryption layer
 */
abstract class KeyProtection {

    enum class Level {
        /**
         * Requires user authentication within a 30 second time window
         */
        SINGLE_PROMPT,

        /**
         * Requires user authentication using fingerprint or Lockscreen for every use of the key
         */
        PROMPT,

        /**
         * Uses AndroidKeyStore encryption, without user presence requirement
         */
        SIMPLE,

        /**
         * unused yet - defaults to [SIMPLE]
         */
        CLOUD
    }

    abstract fun genKey(context: Context)
    abstract fun encrypt(context: Context, purpose: String = "", blob: ByteArray, callback: EncryptionCallback)
    abstract fun decrypt(context: Context, purpose: String = "", ciphertext: String, callback: DecryptionCallback)

    abstract val alias: String

    companion object {

        fun canUseKeychainAuthentication(context: Context): Boolean {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            // TODO: prompt user to setup keyguard
            return keyguardManager.isKeyguardSecure
        }

        @SuppressLint("NewApi")
        fun hasSetupFingerprint(context: Context): Boolean {
            if (hasMarshmallow()) {
                val mFingerprintManager = context.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
                try {
                    if (!mFingerprintManager.isHardwareDetected) {
                        return false
                    } else if (!mFingerprintManager.hasEnrolledFingerprints()) {
                        //TODO: prompt user to enroll fingerprints
                        return false
                    }
                } catch (e: SecurityException) {
                    // Should never be thrown since we have declared the USE_FINGERPRINT permission
                    // in the manifest file
                    return false
                }

                return true
            } else {
                return false
            }
        }

        @SuppressLint("NewApi")
        fun hasFingerprintHardware(context: Context): Boolean {
            return if (hasMarshmallow()) {
                val mFingerprintManager = context.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
                try {
                    mFingerprintManager.isHardwareDetected
                } catch (e: SecurityException) {
                    // Should never be thrown since we have declared the USE_FINGERPRINT permission
                    // in the manifest file
                    false
                }
            } else {
                false
            }
        }

        @Throws(IllegalBlockSizeException::class, BadPaddingException::class)
        internal fun encryptRaw(blob: ByteArray, keyAlias: String): String {

            val cipher = getWrappingCipher(ENCRYPT_MODE, keyAlias)

            val encryptedBytes = cipher.doFinal(blob)

            val ivCompat = ByteArray(0)
            return packCiphertext(ivCompat, encryptedBytes)
        }

        @Throws(IllegalBlockSizeException::class, BadPaddingException::class)
        internal fun decryptRaw(ciphertext: String, keyAlias: String): ByteArray {

            val cipher = getWrappingCipher(DECRYPT_MODE, keyAlias)

            val (_, encryptedBytes) = unpackCiphertext(ciphertext)

            return cipher.doFinal(encryptedBytes)
        }
    }
}