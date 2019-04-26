package com.uport.sdk.signer.encryption

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.security.keystore.UserNotAuthenticatedException
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.DecryptionCallback
import com.uport.sdk.signer.EncryptionCallback
import com.uport.sdk.signer.UportSigner
import com.uport.sdk.signer.UportSigner.Companion.ERR_ACTIVITY_DOES_NOT_EXIST
import com.uport.sdk.signer.encryption.AndroidKeyStoreHelper.generateWrappingKey
import com.uport.sdk.signer.hasMarshmallow
import java.security.InvalidKeyException

class KeyguardAsymmetricProtection(sessionTimeoutSeconds: Int = DEFAULT_SESSION_TIMEOUT_SECONDS) : KeyProtection() {

    override
    val alias = "__keyguard_asymmetric_key_alias__"

    private var sessionTimeout: Int = sessionTimeoutSeconds
    private val extendedAlias = if (sessionTimeout == DEFAULT_SESSION_TIMEOUT_SECONDS) {
        alias
    } else {
        "$alias$sessionTimeout"
    }

    override
    fun genKey(context: Context) {

        if (!canUseKeychainAuthentication(context)) {
            throw IllegalStateException(UportSigner.ERR_KEYGUARD_NOT_CONFIGURED)
        }

        generateWrappingKey(context, extendedAlias, true, sessionTimeout)
    }

    override
    fun encrypt(context: Context, purpose: String, blob: ByteArray, callback: EncryptionCallback) {
        try {
            val ciphertext = encryptRaw(blob, extendedAlias)
            callback(null, ciphertext)
        } catch (ex: Exception) {
            callback(ex, "")
        }
    }

    /**
     * Emulates a keyguard protected key on API 19-22
     *
     * On API 19-22, keys don't throw UserNotAuthenticatedException while the screen is unlocked
     * so that behavior needs to be emulated
     */
    private fun shouldShowKeyguard(): Boolean {
        val now = System.currentTimeMillis()
        val elapsedTimeMillis = (now - getLastUnlock(extendedAlias))
        return if (sessionTimeout >= 0) {
            elapsedTimeMillis > sessionTimeout * 1000
        } else {
            true
        }
    }


    private fun decryptAfterKeyguard(context: Context, purpose: String, ciphertext: String, callback: DecryptionCallback) {
        if (context is AppCompatActivity) {
            showKeyguard(
                    context,
                    purpose,
                    object : KeyguardLaunchFragment.KeyguardCallback {
                        override fun onKeyguardResult(unlocked: Boolean) {
                            if (unlocked) {
                                try {
                                    val cleartextBytes = decryptRaw(ciphertext, extendedAlias)
                                    //only update if there was no exception
                                    updateUnlock(extendedAlias)
                                    //finally decrypted.. phew
                                    callback(null, cleartextBytes)
                                } catch (exception: Exception) {
                                    callback(exception, ByteArray(0))
                                }
                            } else {
                                callback(RuntimeException(UportSigner.ERR_AUTH_CANCELED), ByteArray(0))
                            }
                        }
                    })
        } else {
            callback(IllegalStateException(ERR_ACTIVITY_DOES_NOT_EXIST), ByteArray(0))
        }
    }

    @Suppress("NestedBlockDepth", "ComplexMethod")
    override
    fun decrypt(context: Context, purpose: String, ciphertext: String, callback: DecryptionCallback) {
        try {
            if (hasMarshmallow()) {
                try {
                    val cleartextBytes = decryptRaw(ciphertext, extendedAlias)
                    callback(null, cleartextBytes)
                } catch (exception: InvalidKeyException) {
                    @SuppressLint("NewApi")
                    if (exception is UserNotAuthenticatedException) {
                        decryptAfterKeyguard(context, purpose, ciphertext, callback)
                    } else {
                        throw exception
                    }
                }
            } else {
                if (shouldShowKeyguard()) {
                    decryptAfterKeyguard(context, purpose, ciphertext, callback)
                } else {
                    val cleartextBytes = decryptRaw(ciphertext, extendedAlias)
                    callback(null, cleartextBytes)
                }
            }
        } catch (exception: Exception) {
            //TODO: possible scenario to address: if the device has just configured PIN and has never been unlocked, this may throw IllegalBlockSizeException
            return callback(exception, ByteArray(0))
        }
    }

    private fun showKeyguard(activity: Activity, purpose: String, callback: KeyguardLaunchFragment.KeyguardCallback) {
        val supportFragmentManager = (activity as AppCompatActivity).supportFragmentManager
        KeyguardLaunchFragment.show(supportFragmentManager, purpose, callback)
    }


    companion object {
        private const val DEFAULT_SESSION_TIMEOUT_SECONDS: Int = 30 //seconds

        private val lastUnlock = mapOf<String, Long>().toMutableMap()

        @Synchronized
        private fun getLastUnlock(alias: String): Long {
            return lastUnlock[alias] ?: 0L
        }

        @Synchronized
        private fun updateUnlock(alias: String) {
            lastUnlock[alias] = System.currentTimeMillis()
        }

    }

}