package com.uport.sdk.signer.encryption

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.R
import com.uport.sdk.signer.UportSigner
import com.uport.sdk.signer.UportSigner.Companion.ERR_ACTIVITY_DOES_NOT_EXIST
import com.uport.sdk.signer.encryption.AndroidKeyStoreHelper.generateWrappingKey
import com.uport.sdk.signer.encryption.AndroidKeyStoreHelper.getWrappingCipher
import com.uport.sdk.signer.unpackCiphertext
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import javax.crypto.Cipher
import kotlin.coroutines.experimental.suspendCoroutine

class FingerprintAsymmetricProtection : KeyProtection() {

    override
    val alias = "__fingerprint_asymmetric_key_alias__"

    override
    fun genKey(context: Context) {

        generateWrappingKey(context, alias, true)
    }

    override suspend fun encrypt(context: Context, purpose: String, blob: ByteArray): String {
        return encryptRaw(blob, alias)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override suspend fun decrypt(context: Context, purpose: String, ciphertext: String): ByteArray {
        val (_, encryptedBytes) = unpackCiphertext(ciphertext)
        val cipher = getWrappingCipher(Cipher.DECRYPT_MODE, alias)

        if (context is AppCompatActivity) {
            val cryptoObject = showFingerprintDialog(context, purpose, cipher)
            return cryptoObject.cipher.doFinal(encryptedBytes)
        } else {
            throw IllegalStateException(ERR_ACTIVITY_DOES_NOT_EXIST)
        }
    }


    private lateinit var fingerprintDialog: FingerprintDialog

    @TargetApi(Build.VERSION_CODES.M)
    private suspend fun showFingerprintDialog(activity: Activity, purpose: String, cipher: Cipher): FingerprintManager.CryptoObject = suspendCoroutine { continuation ->
        launch(UI) {
            fingerprintDialog = FingerprintDialog.create(purpose)
            fingerprintDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.uport_AppDialogTheme)

            if (activity.fragmentManager.findFragmentByTag(FingerprintDialog.TAG_FINGERPRINT_DIALOG) == null) {
                val cryptoObject = FingerprintManager.CryptoObject(cipher)
                fingerprintDialog.init(
                        cryptoObject,
                        object : FingerprintDialog.FingerprintDialogCallbacks {
                            override fun onFingerprintSuccess(cryptoObject: FingerprintManager.CryptoObject) {
                                continuation.resume(cryptoObject)
                                fingerprintDialog.dismiss()
                            }

                            override fun onFingerprintCancel() {
                                continuation.resumeWithException(RuntimeException(UportSigner.ERR_AUTH_CANCELED))
                                fingerprintDialog.dismiss()
                            }
                        }
                )
                fingerprintDialog.show(activity.fragmentManager, FingerprintDialog.TAG_FINGERPRINT_DIALOG)
            }
        }
    }

}