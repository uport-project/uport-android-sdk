package com.uport.sdk.signer

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.uport.sdk.signer.encryption.KeyProtection
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.kethereum.bip32.generateKey
import org.kethereum.bip39.Mnemonic
import org.kethereum.crypto.getAddress
import org.kethereum.crypto.signMessage
import org.kethereum.model.SignatureData
import org.walleth.khex.prepend0xPrefix
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
     *
     * @return Returns a Pair with the first value being the address, and the second being the
     * public key.
     */
    suspend fun createHDSeed(context: Context, level: KeyProtection.Level): Pair<String, String> {

        val entropyBuffer = ByteArray(128 / 8)
        SecureRandom().nextBytes(entropyBuffer)

        val seedPhrase = Mnemonic.entropyToMnemonic(entropyBuffer)

        return importHDSeed(context, level, seedPhrase)

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
     *
     * @return Returns a pair being the address and the public key
     */
    suspend fun importHDSeed(context: Context, level: KeyProtection.Level, phrase: String): Pair<String, String> {
        val seedBuffer = Mnemonic.mnemonicToSeed(phrase)

        val entropyBuffer = Mnemonic.mnemonicToEntropy(phrase)

        val extendedRootKey = generateKey(seedBuffer, UPORT_ROOT_DERIVATION_PATH)

        val keyPair = extendedRootKey.keyPair

        val publicKeyBytes = keyPair.getUncompressedPublicKeyWithPrefix()
        val publicKeyString = publicKeyBytes.toBase64().padBase64()
        val address: String = keyPair.getAddress().prepend0xPrefix()

        val label = asSeedLabel(address)

        // Store the payload
        storeEncryptedPayload(context, level, label, entropyBuffer)

        // Empty memory
        entropyBuffer.fill(0)

        return Pair(address, publicKeyString)
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
    suspend fun signTransaction(context: Context, rootAddress: String, derivationPath: String, txPayload: String, prompt: String): SignatureData {
        val (encryptionLayer, encryptedEntropy) = getEncryptionForLabel(context, asSeedLabel(rootAddress))

        val entropyBuff = encryptionLayer.decrypt(context, prompt, encryptedEntropy)

        val phrase = Mnemonic.entropyToMnemonic(entropyBuff)
        val seed = Mnemonic.mnemonicToSeed(phrase)
        val extendedKey = generateKey(seed, derivationPath)

        val keyPair = extendedKey.keyPair

        val txBytes = txPayload.decodeBase64()

        val sigData = keyPair.signMessage(txBytes)
        return sigData
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
    suspend fun signJwtBundle(context: Context, rootAddress: String, derivationPath: String, data: String, prompt: String): SignatureData {
        val (encryptionLayer, encryptedEntropy) = getEncryptionForLabel(context, asSeedLabel(rootAddress))
        val entropyBuff = encryptionLayer.decrypt(context, prompt, encryptedEntropy)
        val phrase = Mnemonic.entropyToMnemonic(entropyBuff)
        val seed = Mnemonic.mnemonicToSeed(phrase)
        val extendedKey = generateKey(seed, derivationPath)

        val keyPair = extendedKey.keyPair

        val payloadBytes = data.decodeBase64()
        return signJwt(payloadBytes, keyPair)
    }

    /**
     * Derives the ethereum address and public key using the given [derivationPath] starting from
     * the seed that generated the given [rootAddress]
     *
     * The respective seed must have been previously generated or imported.
     *
     * The results are passed back to the calling code using the provided [callback]
     *
     * @return Returns a Pair containing the address and the public key
     */
    suspend fun computeAddressForPath(context: Context, rootAddress: String, derivationPath: String, prompt: String): Pair<String, String> {

        val (encryptionLayer, encryptedEntropy) = getEncryptionForLabel(context, asSeedLabel(rootAddress))

        val entropyBuff = encryptionLayer.decrypt(context, prompt, encryptedEntropy)

        val phrase = Mnemonic.entropyToMnemonic(entropyBuff)
        val seed = Mnemonic.mnemonicToSeed(phrase)
        val extendedKey = generateKey(seed, derivationPath)

        val keyPair = extendedKey.keyPair

        val publicKeyBytes = keyPair.getUncompressedPublicKeyWithPrefix()
        val publicKeyString = publicKeyBytes.toBase64().padBase64()
        val address: String = keyPair.getAddress().prepend0xPrefix()

        return Pair(address, publicKeyString)
    }

    /**
     * Decrypts the seed that generated the given [rootAddress] and returns it as a mnemonic phrase
     *
     * The respective seed must have been previously generated or imported.
     *
     * The result is passed back to the calling code using the provided [callback]
     * @return Returns the seed
     */
    suspend fun showHDSeed(context: Context, rootAddress: String, prompt: String): String {

        val (encryptionLayer, encryptedEntropy) = getEncryptionForLabel(context, asSeedLabel(rootAddress))
        val entropyBuff = encryptionLayer.decrypt(context, prompt, encryptedEntropy)
        val phrase = Mnemonic.entropyToMnemonic(entropyBuff)
        return phrase
    }

    /**
     * Verifies if a given phrase is a valid mnemonic phrase usable in seed generation
     */
    fun validateMnemonic(phrase: String): Boolean = Mnemonic.validateMnemonic(phrase)

    /**
     * Returns a list of addresses representing the uport roots used as handles for seeds
     */
    fun allHDRoots(context: Context): List<String> {

        val prefs = context.getSharedPreferences(ETH_ENCRYPTED_STORAGE, MODE_PRIVATE)
        //list all stored keys, keep a list off what looks like uport root addresses
        return prefs.all.keys
                .filter { label -> label.startsWith(SEED_PREFIX) }
                .filter { hasCorrespondingLevelKey(prefs, it) }
                .map { label: String -> label.substring(SEED_PREFIX.length) }
    }

    companion object {
        const val UPORT_ROOT_DERIVATION_PATH: String = "m/7696500'/0'/0'/0'"
    }
}