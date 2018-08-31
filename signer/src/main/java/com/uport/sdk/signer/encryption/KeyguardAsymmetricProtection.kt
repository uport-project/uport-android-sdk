package com.uport.sdk.signer.encryption

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.security.keystore.UserNotAuthenticatedException
import android.support.v7.app.AppCompatActivity
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

        if (!KeyProtection.canUseKeychainAuthentication(context)) {
            throw IllegalStateException(UportSigner.ERR_KEYGUARD_NOT_CONFIGURED)
        }

        generateWrappingKey(context, extendedAlias, true, sessionTimeout)
    }

    override suspend fun encrypt(context: Context, purpose: String, blob: ByteArray): String {
        return KeyProtection.encryptRaw(blob, extendedAlias)
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


    private suspend fun decryptAfterKeyguard(context: Context, purpose: String, ciphertext: String): ByteArray {
        return when {
            context !is AppCompatActivity -> throw IllegalStateException(ERR_ACTIVITY_DOES_NOT_EXIST)
            !showKeyguard(context, purpose) -> throw RuntimeException(UportSigner.ERR_AUTH_CANCELED)
            else -> {
                val cleartextBytes = KeyProtection.decryptRaw(ciphertext, extendedAlias)
                //only update if there was no exception
                updateUnlock(extendedAlias)
                //finally decrypted.. phew
                cleartextBytes
            }
        }
    }

    override suspend fun decrypt(context: Context, purpose: String, ciphertext: String): ByteArray {
        return if (hasMarshmallow()) {
            try {
                KeyProtection.decryptRaw(ciphertext, extendedAlias)
            } catch (exception: InvalidKeyException) {
                @SuppressLint("NewApi")
                if (exception is UserNotAuthenticatedException) {
                    decryptAfterKeyguard(context, purpose, ciphertext)
                } else {
                    throw exception
                }
            }
        } else {
            if (shouldShowKeyguard()) {
                decryptAfterKeyguard(context, purpose, ciphertext)
            } else {
                KeyProtection.decryptRaw(ciphertext, extendedAlias)
            }
        }

        //TODO: possible scenario to address: if the device has just configured PIN and has never been unlocked, this may throw IllegalBlockSizeException
    }

    private suspend fun showKeyguard(activity: Activity, purpose: String): Boolean {
        val supportFragmentManager = (activity as AppCompatActivity).supportFragmentManager
        return KeyguardLaunchFragment.show(supportFragmentManager, purpose)
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