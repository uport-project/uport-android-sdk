@file:Suppress("TooManyFunctions")

package com.uport.sdk.signer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.uport.sdk.signer.encryption.KeyProtection
import com.uport.sdk.signer.encryption.KeyProtectionFactory
import com.uport.sdk.signer.encryption.SimpleAsymmetricProtection
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.getUncompressedPublicKeyWithPrefix
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.kethereum.crypto.createEthereumKeyPair
import org.kethereum.crypto.signMessage
import org.kethereum.crypto.signMessageHash
import org.kethereum.crypto.toAddress
import org.kethereum.crypto.toECKeyPair
import org.kethereum.extensions.toBytesPadded
import org.kethereum.hashes.sha256
import org.kethereum.model.ECKeyPair
import org.kethereum.model.PRIVATE_KEY_SIZE
import org.kethereum.model.PrivateKey
import org.kethereum.model.SignatureData
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.math.BigInteger
import java.security.InvalidKeyException
import java.security.KeyException
import java.security.Security

@Suppress("unused")
open class UportSigner {

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    /**
     * checks if the device is secured with a lockscreen
     */
    fun hasSecuredKeyguard(context: Context, callback: (Boolean) -> Unit) {
        val keyguardSecured = KeyProtection.canUseKeychainAuthentication(context)
        callback(keyguardSecured)
    }

    /**
     * checks if the user has enrolled fingerprints
     */
    fun hasSetupFingerprints(context: Context, callback: (Boolean) -> Unit) {
        callback(KeyProtection.hasSetupFingerprint(context))
    }

    /**
     * checks if the device has a fingerprint sensor
     */
    fun hasFingerprintHardware(context: Context, callback: (Boolean) -> Unit) {
        callback(KeyProtection.hasFingerprintHardware(context))
    }


    /**
     * Creates an ETH keypair and stores it at the [level] encryption level.
     * then [callback] with the corresponding 0x `address` and base64 `public Key`
     * or a non-null error in case something went wrong
     */
    fun createKey(context: Context, level: KeyProtection.Level, callback: (err: Exception?, address: String, pubKey: String) -> Unit) {

        val privateKeyBytes = try {
            val (privKey, _) = createEthereumKeyPair()
            privKey.key.toBytesPadded(PRIVATE_KEY_SIZE)
        } catch (exception: Exception) {
            return callback(KeyException("ERR_CREATING_KEYPAIR", exception), "", "")
        }

        return saveKey(context, level, privateKeyBytes, callback)
    }

    /**
     * Stores a given [privateKeyBytes] ByteArray at the provided [level] of encryption.
     *
     * Then calls back with the derived 0x `address` and base64 `public Key`
     * or a non-null err in case something goes wrong
     */
    fun saveKey(context: Context, level: KeyProtection.Level, privateKeyBytes: ByteArray, callback: (err: Exception?, address: String, pubKey: String) -> Unit) {

        val keyPair = PrivateKey(privateKeyBytes).toECKeyPair()

        val publicKeyBytes = keyPair.getUncompressedPublicKeyWithPrefix()
        val publicKeyString = publicKeyBytes.toBase64().padBase64()
        val address: String = keyPair.toAddress().hex

        val label = asAddressLabel(address)

        storeEncryptedPayload(context,
                level,
                label,
                privateKeyBytes
        ) { err, _ ->

            //empty memory
            privateKeyBytes.fill(0)

            if (err != null) {
                return@storeEncryptedPayload callback(err, "", "")
            }

            return@storeEncryptedPayload callback(null, address, publicKeyString)
        }
    }

    /**
     * deletes a key from storage
     */
    fun deleteKey(context: Context, address: String) {
        val prefs = context.getSharedPreferences(ETH_ENCRYPTED_STORAGE, MODE_PRIVATE)
        val label = asAddressLabel(address)
        prefs.edit()
                .remove(label)
                .remove(asLevelLabel(label))
                .apply()
    }

    /**
     * Signs a transaction bundle using the private key that generated the given address.
     * In case the private key corresponding to the [address] requires user authentication to decrypt,
     * this method will launch the decryption UI with a [prompt] and schedule a callback with the signature data
     * after the decryption takes place or a non-null error in case something goes wrong (or user cancels)
     *
     * The decryption UI can be a device lockscreen or fingerprint-dialog depending on the level of encryption
     * requested at key creation.
     *
     * @param context The android activity from which the signature is requested or app context if it's encrypted using [KeyProtection.Level.SIMPLE]] protection
     * @param address the 0x ETH address corresponding to the desired key
     * @param txPayload the base64 encoded byte array that represents the message to be signed
     * @param prompt A string that needs to be displayed to the user in case fingerprint auth is requested
     * @param callback (error, signature) called after the transaction has been signed successfully or
     * with an error and empty data when it fails
     */
    fun signTransaction(context: Context, address: String, txPayload: String, prompt: String, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        val (encryptionLayer, encryptedPrivateKey, storageError) = getEncryptionForLabel(context, asAddressLabel(address))

        if (storageError != null) {
            return callback(storageError, EMPTY_SIGNATURE_DATA)
        }

        encryptionLayer.decrypt(context, prompt, encryptedPrivateKey) { err, privateKeyBytes ->

            if (err != null) {
                return@decrypt callback(err, EMPTY_SIGNATURE_DATA)
            }

            try {
                val keyPair = PrivateKey(privateKeyBytes).toECKeyPair()
                privateKeyBytes.fill(0)

                val txBytes = txPayload.decodeBase64()

                val sigData = keyPair.signMessage(txBytes)
                return@decrypt callback(null, sigData)

            } catch (exception: Exception) {
                return@decrypt callback(exception, EMPTY_SIGNATURE_DATA)
            }

        }

    }

    /**
     * Fetches the encryption combo for a particular label(address)
     * The encryption combo is the ciphertext along with the class needed to decrypt it
     */
    fun getEncryptionForLabel(context: Context, label: String): EncryptionCombo {
        val prefs = context.getSharedPreferences(ETH_ENCRYPTED_STORAGE, MODE_PRIVATE)

        return try {
            //check if label is tracked
            val keyExists = (prefs.contains(asLevelLabel(label))
                    && prefs.contains(label))

            if (!keyExists) {
                throw InvalidKeyException(ERR_KEY_NOT_REGISTERED)
            }

            val levelName = prefs.getString(asLevelLabel(label), null) ?: ""
            val level = KeyProtection.Level.valueOf(levelName)
            val encryptionLayer = KeyProtectionFactory.obtain(context, level)

            //read encrypted payload from storage
            val encryptedPayload = prefs.getString(label, null)
                    ?: throw InvalidKeyException(ERR_KEY_CORRUPTED)

            EncryptionCombo(encryptionLayer, encryptedPayload, null)

        } catch (ex: Exception) {
            EncryptionCombo(SimpleAsymmetricProtection(), "", ex)
        }
    }

    /**
     * Signs a jwt payload using the private key that generated the given [address].
     *
     * In case the private key corresponding to the [address] requires user authentication to decrypt,
     * this method will launch the decryption UI and schedule a callback with the signature
     * after the decryption takes place or a non-null error in case something goes wrong (or user cancels)
     *
     * The decryption UI can be a device lockscreen or fingerprint-dialog depending on the
     * [KeyProtection.Level] of encryption requested at key creation.
     *
     * @param context The android activity from which the signature is requested or application context;
     * @param address the 0x ETH address corresponding to the desired key
     * @param data the base64 encoded byte array that represents the message to be signed
     * @param prompt A string that needs to be displayed to the user in case fingerprint auth is requested
     * @param callback (error, signature) called after the transaction has been signed successfully or
     * with an error and empty data when it fails
     */
    fun signJwtBundle(context: Context, address: String, data: String, prompt: String, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        val (encryptionLayer, encryptedPrivateKey, storageError) = getEncryptionForLabel(context, asAddressLabel(address))

        if (storageError != null) {
            return callback(storageError, SignatureData())
        }

        encryptionLayer.decrypt(context, prompt, encryptedPrivateKey) { err, privateKeyBytes ->
            if (err != null) {
                return@decrypt callback(err, SignatureData())
            }

            try {
                val keyPair = PrivateKey(privateKeyBytes).toECKeyPair()
                val payloadBytes = data.decodeBase64()
                val sig = signJwt(payloadBytes, keyPair)
                privateKeyBytes.fill(0)

                return@decrypt callback(null, sig)
            } catch (exception: Exception) {
                return@decrypt callback(err, SignatureData())
            }
        }
    }

    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    fun signJwt(payloadBytes: ByteArray, keyPair: ECKeyPair) = signMessageHash(payloadBytes.sha256(), keyPair, false)

    /**
     * Builds a list of all the saved eth addresses (that also have encrypted private keys tracked)
     */
    fun allAddresses(context: Context, callback: (addresses: List<String>) -> Unit) {

        val prefs = context.getSharedPreferences(ETH_ENCRYPTED_STORAGE, MODE_PRIVATE)
        //list all stored keys, keep a list of what looks like addresses
        val addresses = prefs.all.keys
                .filter { label -> label.startsWith(ADDRESS_PREFIX) }
                .filter { hasCorrespondingLevelKey(prefs, it) }
                .map { label: String -> label.substring(ADDRESS_PREFIX.length) }
        callback(addresses)
    }

    /**
     * Stores the given [payload] ByteArray encrypted at the provided [keyLevel].
     * The encrypted [payload] and corresponding [keyLevel] can later be retrieved using the [label]
     *
     * This method calls back with the success of the encryption operation
     * or a non-null exception if something goes wrong
     */
    fun storeEncryptedPayload(
            context: Context,
            keyLevel: KeyProtection.Level,
            label: String,
            payload: ByteArray,
            callback: (err: Exception?, result: Boolean) -> Unit
    ) {

        try {
            val encLayer: KeyProtection = KeyProtectionFactory.obtain(context, keyLevel)
            val prefs = context.getSharedPreferences(ETH_ENCRYPTED_STORAGE, MODE_PRIVATE)

            encLayer.encrypt(
                    context,
                    "store encrypted payload",
                    payload
            ) { err, ciphertext ->

                if (err != null) {
                    return@encrypt callback(err, false)
                }
                prefs.edit()
                        //store encrypted privatekey
                        .putString(label, ciphertext)
                        //mark the key as encrypted with provided security level
                        .putString(asLevelLabel(label), keyLevel.name)
                        .apply()
                return@encrypt callback(null, true)
            }
        } catch (ex: Exception) {
            return callback(ex, false)
        }
    }

    /**
     * Decrypts a previously encrypted payload that was stored under the given [label]
     *
     * Payloads can be stored at different levels of encryption (see [storeEncryptedPayload])
     * In case the payload in question needs user presence to decrypt,
     * the [context] should be an instance of AppcompatActivity and it will
     * show the necessary UI (fingerprint dialog or lock-screen) with a [prompt]
     *
     * This method calls back with the decrypted [ByteArray] on success
     * or a non-null exception if something goes wrong
     */
    fun loadEncryptedPayload(
            context: Context,
            label: String,
            prompt: String,
            callback: (err: Exception?, result: ByteArray) -> Unit
    ) {

        val (encryptionLayer, encryptedPayload, storageError) = getEncryptionForLabel(context, label)

        if (storageError != null) {
            return callback(storageError, ByteArray(0))
        }

        //decrypt using the appropriate level
        encryptionLayer.decrypt(context, prompt, encryptedPayload) { err, decrypted ->
            if (err != null) {
                return@decrypt callback(err, ByteArray(0))
            }

            return@decrypt callback(null, decrypted)
        }
    }

    companion object {

        internal const val ETH_ENCRYPTED_STORAGE = "eth_signer_store"

        private const val ADDRESS_PREFIX = "address-"
        internal const val SEED_PREFIX = "seed-"

        val asLevelLabel = { what: String -> "level-$what" }
        val asAddressLabel = { address: String -> "$ADDRESS_PREFIX$address" }
        val asGenericLabel = { label: String -> "enc-$label" }
        val asSeedLabel = { label: String -> "$SEED_PREFIX$label" }

        val hasCorrespondingLevelKey = { prefs: SharedPreferences, label: String -> prefs.contains(asLevelLabel(label)) }

        /**
         * This is thrown by KeyguardProtection when the user has not configured any device security.
         * Prompt the user to setup PIN / Pattern / Password protection for their device.
         * You can use Intent(Settings.ACTION_SECURITY_SETTINGS) to lead them to Android Settings -> Security tab
         */
        const val ERR_KEYGUARD_NOT_CONFIGURED = "E_KEYGUARD_NOT_CONFIGURED"

        /**
         * Thrown when trying to encrypt/decrypt something with a key that is not registered
         */
        const val ERR_KEY_NOT_REGISTERED = "E_KEY_NOT_REGISTERED"
        const val ERR_KEY_CORRUPTED = "E_KEY_CORRUPTED"
        const val ERR_BLANK_KEY = "E_BLANK_KEY"
        const val ERR_ENCODING_ERROR = "E_ENCODING_PROBLEM"
        const val ERR_AUTH_CANCELED = "E_AUTH_CANCELED"

        /**
         * Thrown when the [KeyProtection.Level] requested is [KeyProtection.Level.SINGLE_PROMPT] or [KeyProtection.Level.PROMPT]
         * but the requested operation is being performed outside an activity context.
         *
         * For signing stuff with these protection levels, an activity is needed to launch the proper UI.
         */
        const val ERR_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST"

//        const val UNCOMPRESSED_PUBLIC_KEY_SIZE = PUBLIC_KEY_SIZE + 1
//        const val COMPRESSED_PUBLIC_KEY_SIZE = PRIVATE_KEY_SIZE + 1

        data class EncryptionCombo(val keyProtection: KeyProtection, val encPayload: String, val err: Exception?)

        internal val EMPTY_SIGNATURE_DATA = SignatureData(BigInteger.ZERO, BigInteger.ZERO, 0)

    }

}


