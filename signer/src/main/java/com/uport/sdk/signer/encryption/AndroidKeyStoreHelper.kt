@file:Suppress("DEPRECATION")

package com.uport.sdk.signer.encryption

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import com.uport.sdk.signer.hasMarshmallow
import java.io.IOException
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.UnrecoverableKeyException
import java.security.spec.MGF1ParameterSpec
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.security.auth.x500.X500Principal
import javax.security.cert.CertificateException

object AndroidKeyStoreHelper {

    const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"

    /**
     * Size of the RSA key used to wrap the protected key
     */
    private const val WRAPPING_KEY_SIZE = 2048

    /**
     * [java.security.spec.AlgorithmParameterSpec] applied to the wrapping cipher on API 23+
     */
    private val OAEP_SPEC = OAEPParameterSpec(
            "SHA-256", "MGF1",
            MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)

    /**
     * The cipher transformation used to wrap the protected key
     */
    private val WRAPPING_TRANSFORMATION =
            if (hasMarshmallow())
                "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            else
                "RSA/ECB/PKCS1Padding"


    @Throws(KeyStoreException::class,
            NoSuchProviderException::class,
            IOException::class,
            NoSuchAlgorithmException::class,
            CertificateException::class)
    internal fun getKeyStore(): KeyStore {
        // Get a KeyStore instance with the Android Keystore provider.
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER)

        // Relict of the old JCA API - you have to call load() even
        // if you do not have an input stream you want to load - otherwise it'll crash.
        keyStore.load(null)
        return keyStore
    }

    @Throws(KeyPermanentlyInvalidatedException::class,
            KeyStoreException::class,
            CertificateException::class,
            UnrecoverableKeyException::class,
            IOException::class,
            NoSuchAlgorithmException::class,
            InvalidKeyException::class,
            InvalidAlgorithmParameterException::class,
            NoSuchProviderException::class,
            NoSuchPaddingException::class)
    fun getWrappingCipher(mode: Int, keyAlias: String): Cipher {

        val keyStore = getKeyStore()

        val cipher = Cipher.getInstance(WRAPPING_TRANSFORMATION)

        val key = when (mode) {
            Cipher.DECRYPT_MODE, Cipher.UNWRAP_MODE -> {
                keyStore.getKey(keyAlias, null) as PrivateKey
            }
            //ENCRYPT_MODE, WRAP_MODE
            else -> {
                val pubKey = keyStore.getCertificate(keyAlias).publicKey
                //due to a bug in API23, the public key needs to be separated from the keystore
                KeyFactory.getInstance(pubKey.algorithm)
                        .generatePublic(X509EncodedKeySpec(pubKey.encoded)) as PublicKey
            }
        }

        if (hasMarshmallow()) {
            cipher.init(mode, key, OAEP_SPEC)
        } else {
            cipher.init(mode, key)
        }

        return cipher
    }

    @SuppressLint("NewApi")
    @Throws(KeyStoreException::class,
            NoSuchProviderException::class,
            NoSuchAlgorithmException::class,
            InvalidAlgorithmParameterException::class)
    fun generateWrappingKey(context: Context, keyAlias: String, requiresAuth: Boolean = false, sessionTimeout: Int = -1) {

        val keyStore = getKeyStore()
        val publicKey = keyStore.getCertificate(keyAlias)?.publicKey

        if (publicKey == null) {

            val spec = if (hasMarshmallow()) {
                KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                        .setUserAuthenticationRequired(requiresAuth)
                        .setUserAuthenticationValidityDurationSeconds(sessionTimeout)
                        .build()
            } else {
                val cal = Calendar.getInstance()
                val startDate: Date = cal.time
                cal.add(Calendar.YEAR, 100)
                val endDate: Date = cal.time

                @Suppress("DEPRECATION")
                val specBuilder = KeyPairGeneratorSpec.Builder(context)
                        .setAlias(keyAlias)
                        .setSubject(X500Principal("CN=$keyAlias"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                // Only API levels 19 and above allow specifying RSA key parameters.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    val rsaSpec = RSAKeyGenParameterSpec(WRAPPING_KEY_SIZE, RSAKeyGenParameterSpec.F4)
                    specBuilder.setAlgorithmParameterSpec(rsaSpec)
                    specBuilder.setKeySize(WRAPPING_KEY_SIZE)
                }
                if (requiresAuth) {
                    specBuilder.setEncryptionRequired()
                }
                specBuilder.build()
            }

            val keyPairGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEYSTORE_PROVIDER)
            keyPairGenerator.initialize(spec)
            keyPairGenerator.generateKeyPair()
        }
    }
}