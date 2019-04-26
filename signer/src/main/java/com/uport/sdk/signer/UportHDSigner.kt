package com.uport.sdk.signer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.uport.sdk.signer.encryption.KeyProtection
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.getUncompressedPublicKeyWithPrefix
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.kethereum.bip39.entropyToMnemonic
import org.kethereum.bip39.mnemonicToEntropy
import org.kethereum.bip39.model.MnemonicWords
import org.kethereum.bip39.toKey
import org.kethereum.bip39.validate
import org.kethereum.bip39.wordlists.WORDLIST_ENGLISH
import org.kethereum.crypto.signMessage
import org.kethereum.crypto.toAddress
import org.kethereum.model.SignatureData
import java.security.SecureRandom

@Suppress("unused", "KDocUnresolvedReference")
class UportHDSigner : UportSigner() {

    /**
     * Checks if there is ANY seed created or imported
     */
    fun hasSeed(context: Context): Boolean {

        val prefs = context.getSharedPreferences(ETH_ENCRYPTED_STORAGE, MODE_PRIVATE)

        val allSeeds = prefs.all.keys
                .filter { label -> label.startsWith(SEED_PREFIX) }
                .filter { hasCorrespondingLevelKey(prefs, it) }

        return allSeeds.isNotEmpty()
    }

    /**
     * Creates a 128 bit seed and stores it at the [level] encryption level.
     * Calls back with the seed handle ([rootAddress]) and the Base64 encoded
     * [pubKey] corresponding to it, or a non-null [err] if something broke
     */
    fun createHDSeed(context: Context, level: KeyProtection.Level, callback: (err: Exception?, rootAddress: String, pubKey: String) -> Unit) {

        val entropyBuffer = ByteArray(128 / 8)
        SecureRandom().nextBytes(entropyBuffer)

        val seedPhrase = entropyToMnemonic(entropyBuffer, WORDLIST_ENGLISH)

        return importHDSeed(context, level, seedPhrase, callback)

    }

    /**
     * Imports a given mnemonic [phrase]
     * The phrase is converted to its binary representation using bip39 rules
     * and stored at the provided [level] of encryption.
     *
     * A rootAddress is derived from the phrase and will be used to refer to this imported phrase for future signing.
     *
     * Then calls back with the derived 0x `rootAddress` and base64 encoded `public Key`
     * or a non-null err in case something goes wrong
     */
    fun importHDSeed(context: Context, level: KeyProtection.Level, phrase: String, callback: (err: Exception?, address: String, pubKey: String) -> Unit) {

        try {
//            val seedBuffer = mnemonicToSeed(phrase)

            val entropyBuffer = mnemonicToEntropy(phrase, WORDLIST_ENGLISH)

            val extendedRootKey = MnemonicWords(phrase).toKey(UPORT_ROOT_DERIVATION_PATH)

            val keyPair = extendedRootKey.keyPair

            val publicKeyBytes = keyPair.getUncompressedPublicKeyWithPrefix()
            val publicKeyString = publicKeyBytes.toBase64().padBase64()
            val address: String = keyPair.toAddress().hex

            val label = asSeedLabel(address)

            storeEncryptedPayload(context,
                    level,
                    label,
                    entropyBuffer
            ) { err, _ ->

                //empty memory
                entropyBuffer.fill(0)

                if (err != null) {
                    return@storeEncryptedPayload callback(err, "", "")
                }

                return@storeEncryptedPayload callback(null, address, publicKeyString)
            }
        } catch (ex: Exception) {
            return callback(ex, "", "")
        }
    }

    /**
     * Deletes a seed from storage.
     */
    fun deleteSeed(context: Context, label: String) {
        val prefs = context.getSharedPreferences(ETH_ENCRYPTED_STORAGE, MODE_PRIVATE)
        prefs.edit()
                //store encrypted privatekey
                .remove(asSeedLabel(label))
                //mark the key as encrypted with provided security level
                .remove(asLevelLabel(label))
                .apply()
    }


    /**
     * Signs a transaction bundle using a key derived from a previously imported/created seed.
     *
     * In case the seed corresponding to the [rootAddress] requires user authentication to decrypt,
     * this method will launch the decryption UI with a [prompt] and schedule a callback with the signature data
     * after the decryption takes place; or a non-null error in case something goes wrong (or user cancels)
     *
     * The decryption UI can be a device lockscreen or fingerprint-dialog depending on the level of encryption
     * requested at seed creation/import.
     *
     * @param context The android activity from which the signature is requested or app context if it's encrypted using [KeyProtection.Level.SIMPLE]] protection
     * @param rootAddress the 0x ETH address used to refer to the previously imported/created seed
     * @param txPayload the base64 encoded byte array that represents the message to be signed
     * @param prompt A string that needs to be displayed to the user in case user-auth is requested
     * @param callback (error, signature) called after the transaction has been signed successfully or
     * with an error and empty data when it fails
     */
    fun signTransaction(context: Context, rootAddress: String, derivationPath: String, txPayload: String, prompt: String, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        val (encryptionLayer, encryptedEntropy, storageError) = getEncryptionForLabel(context, asSeedLabel(rootAddress))

        if (storageError != null) {
            //storage error is also thrown if the root seed does not exist
            return callback(storageError, EMPTY_SIGNATURE_DATA)
        }

        encryptionLayer.decrypt(context, prompt, encryptedEntropy) { err, entropyBuff ->

            if (err != null) {
                return@decrypt callback(err, EMPTY_SIGNATURE_DATA)
            }

            try {

                val phrase = entropyToMnemonic(entropyBuff, WORDLIST_ENGLISH)
                val extendedKey = MnemonicWords(phrase).toKey(derivationPath)

                val keyPair = extendedKey.keyPair

                val txBytes = txPayload.decodeBase64()

                val sigData = keyPair.signMessage(txBytes)
                return@decrypt callback(null, sigData)

            } catch (exception: Exception) {
                return@decrypt callback(exception, EMPTY_SIGNATURE_DATA)
            }

        }

    }

    /**
     * Signs a uPort specific JWT bundle using a key derived from a previously imported/created seed.
     *
     * In case the seed corresponding to the [rootAddress] requires user authentication to decrypt,
     * this method will launch the decryption UI with a [prompt] and schedule a callback with the signature data
     * after the decryption takes place; or a non-null error in case something goes wrong (or user cancels)
     *
     * The decryption UI can be a device lockscreen or fingerprint-dialog depending on the level of encryption
     * requested at seed creation/import.
     *
     * @param context The android activity from which the signature is requested or app context if it's encrypted using [KeyProtection.Level.SIMPLE]] protection
     * @param rootAddress the 0x ETH address used to refer to the previously imported/created seed
     * @param data the base64 encoded byte array that represents the payload to be signed
     * @param prompt A string that needs to be displayed to the user in case user-auth is requested
     * @param callback (error, signature) called after the transaction has been signed successfully or
     * with an error and empty data when it fails
     */
    fun signJwtBundle(context: Context, rootAddress: String, derivationPath: String, data: String, prompt: String, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        val (encryptionLayer, encryptedEntropy, storageError) = getEncryptionForLabel(context, asSeedLabel(rootAddress))

        if (storageError != null) {
            return callback(storageError, SignatureData())
        }

        encryptionLayer.decrypt(context, prompt, encryptedEntropy) { err, entropyBuff ->
            if (err != null) {
                return@decrypt callback(err, SignatureData())
            }

            try {
                val phrase = entropyToMnemonic(entropyBuff, WORDLIST_ENGLISH)
                val extendedKey = MnemonicWords(phrase).toKey(derivationPath)

                val keyPair = extendedKey.keyPair

                val payloadBytes = data.decodeBase64()
                val sig = signJwt(payloadBytes, keyPair)

                return@decrypt callback(null, sig)
            } catch (exception: Exception) {
                return@decrypt callback(err, SignatureData())
            }
        }
    }

    /**
     * Derives the ethereum address and public key using the given [derivationPath] starting from
     * the seed that generated the given [rootAddress]
     *
     * The respective seed must have been previously generated or imported.
     *
     * The results are passed back to the calling code using the provided [callback]
     */
    fun computeAddressForPath(context: Context, rootAddress: String, derivationPath: String, prompt: String, callback: (err: Exception?, address: String, pubKey: String) -> Unit) {

        val (encryptionLayer, encryptedEntropy, storageError) = getEncryptionForLabel(context, asSeedLabel(rootAddress))

        if (storageError != null) {
            return callback(storageError, "", "")
        }

        encryptionLayer.decrypt(context, prompt, encryptedEntropy) { err, entropyBuff ->
            if (err != null) {
                return@decrypt callback(storageError, "", "")
            }

            try {
                val phrase = entropyToMnemonic(entropyBuff, WORDLIST_ENGLISH)
                val extendedKey = MnemonicWords(phrase).toKey(derivationPath)

                val keyPair = extendedKey.keyPair

                val publicKeyBytes = keyPair.getUncompressedPublicKeyWithPrefix()
                val publicKeyString = publicKeyBytes.toBase64().padBase64()
                val address: String = keyPair.toAddress().hex

                return@decrypt callback(null, address, publicKeyString)

            } catch (exception: Exception) {
                return@decrypt callback(err, "", "")
            }
        }


    }

    /**
     * Decrypts the seed that generated the given [rootAddress] and returns it as a mnemonic phrase
     *
     * The respective seed must have been previously generated or imported.
     *
     * The result is passed back to the calling code using the provided [callback]
     */
    fun showHDSeed(context: Context, rootAddress: String, prompt: String, callback: (err: Exception?, phrase: String) -> Unit) {

        val (encryptionLayer, encryptedEntropy, storageError) = getEncryptionForLabel(context, asSeedLabel(rootAddress))

        if (storageError != null) {
            return callback(storageError, "")
        }

        encryptionLayer.decrypt(context, prompt, encryptedEntropy) { err, entropyBuff ->
            if (err != null) {
                return@decrypt callback(err, "")
            }

            try {
                val phrase = entropyToMnemonic(entropyBuff, WORDLIST_ENGLISH)
                return@decrypt callback(null, phrase)
            } catch (exception: Exception) {
                return@decrypt callback(err, "")
            }
        }
    }

    /**
     * Verifies if a given phrase is a valid mnemonic phrase usable in seed generation
     */
    fun validateMnemonic(phrase: String): Boolean = MnemonicWords(phrase).validate(WORDLIST_ENGLISH)

    /**
     * Returns a list of addresses representing the uport roots used as handles for seeds
     */
    fun allHDRoots(context: Context): List<String> {

        val prefs = context.getSharedPreferences(ETH_ENCRYPTED_STORAGE, MODE_PRIVATE)
        //list all stored keys, keep a list of what looks like uport root addresses
        return prefs.all.keys
                .asSequence()
                .filter { label -> label.startsWith(SEED_PREFIX) }
                .filter { hasCorrespondingLevelKey(prefs, it) }
                .map { label: String -> label.substring(SEED_PREFIX.length) }
                .toList()
    }

    companion object {
        const val UPORT_ROOT_DERIVATION_PATH = "m/7696500'/0'/0'/0'"
        const val GENERIC_DEVICE_KEY_DERIVATION_PATH = "m/44'/60'/0'/0"
        const val GENERIC_RECOVERY_DERIVATION_PATH = "m/44'/60'/0'/1"
    }
}