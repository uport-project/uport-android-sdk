package com.uport.sdk.signer

import android.os.Build
import com.uport.sdk.signer.UportSigner.Companion.COMPRESSED_PUBLIC_KEY_SIZE
import com.uport.sdk.signer.UportSigner.Companion.UNCOMPRESSED_PUBLIC_KEY_SIZE
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import me.uport.sdk.core.toBase64UrlSafe
import org.kethereum.crypto.decompressKey
import org.kethereum.crypto.model.ECKeyPair
import org.kethereum.crypto.model.PRIVATE_KEY_SIZE
import org.kethereum.crypto.model.PublicKey
import org.kethereum.extensions.toBigInteger
import org.kethereum.extensions.toBytesPadded
import org.kethereum.model.SignatureData
import org.spongycastle.asn1.ASN1EncodableVector
import org.spongycastle.asn1.ASN1Encoding
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequence
import org.walleth.khex.toNoPrefixHexString
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.*

const val SIG_COMPONENT_SIZE = PRIVATE_KEY_SIZE
const val SIG_SIZE = SIG_COMPONENT_SIZE * 2
const val SIG_RECOVERABLE_SIZE = SIG_SIZE + 1

/**
 * Returns the JOSE encoding of the standard signature components (joined by empty string)
 *
 * @param recoverable If this is true then the buffer returned gets an extra byte with the
 *          recovery param shifted back to [0, 1] ( as opposed to [27,28] )
 */
fun SignatureData.getJoseEncoded(recoverable: Boolean = false): String {
    val size = if (recoverable)
        SIG_RECOVERABLE_SIZE
    else
        SIG_SIZE

    val bos = ByteArrayOutputStream(size)
    bos.write(this.r.toBytesPadded(SIG_COMPONENT_SIZE))
    bos.write(this.s.toBytesPadded(SIG_COMPONENT_SIZE))
    if (recoverable) {
        bos.write(byteArrayOf((this.v - 27).toByte()))
    }
    return bos.toByteArray().toBase64UrlSafe()
}

fun String.decodeJose(recoveryParam: Byte = 27): SignatureData = this.decodeBase64().decodeJose(recoveryParam)

fun ByteArray.decodeJose(recoveryParam: Byte = 27): SignatureData {
    val rBytes = Arrays.copyOfRange(this, 0, SIG_COMPONENT_SIZE)
    val sBytes = Arrays.copyOfRange(this, SIG_COMPONENT_SIZE, SIG_SIZE)
    val v = if (this.size > SIG_SIZE)
        this[SIG_SIZE].let {
            if (it < 27) (it + 27).toByte() else it
        }
    else
        recoveryParam
    return SignatureData(BigInteger(1, rBytes), BigInteger(1, sBytes), v)
}

/**
 * Returns the DER encoding of the standard signature components
 */
fun SignatureData.getDerEncoded(): String {

    val v = ASN1EncodableVector()
    v.add(ASN1Integer(this.r))
    v.add(ASN1Integer(this.s))
    return DERSequence(v)
            .getEncoded(ASN1Encoding.DER)
            .toNoPrefixHexString()
}

private const val DELIMITER = "]"

fun packCiphertext(vararg data: ByteArray): String =
        data.joinToString(DELIMITER) { it.toBase64().padBase64() }

fun unpackCiphertext(ciphertext: String): List<ByteArray> =
        ciphertext
                .split(DELIMITER)
                .map { it.decodeBase64() }

fun ECKeyPair.getUncompressedPublicKeyWithPrefix(): ByteArray {
    val pubBytes = this.publicKey.key.toBytesPadded(UportSigner.UNCOMPRESSED_PUBLIC_KEY_SIZE)
    pubBytes[0] = 0x04
    return pubBytes
}

fun PublicKey.normalize(): PublicKey {
    val pubBytes = this.key.toByteArray()
    val normalizedBytes = when (pubBytes.size) {
        UNCOMPRESSED_PUBLIC_KEY_SIZE -> pubBytes.copyOfRange(1, pubBytes.size)
        COMPRESSED_PUBLIC_KEY_SIZE -> decompressKey(pubBytes)
        else -> pubBytes
    }
    return PublicKey(normalizedBytes.toBigInteger())
}

fun BigInteger.keyToBase64(keySize: Int = PRIVATE_KEY_SIZE): String =
        this.toBytesPadded(keySize).toBase64().padBase64()

fun hasMarshmallow(): Boolean = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)

typealias EncryptionCallback = (err: Exception?, ciphertext: String) -> Unit
typealias DecryptionCallback = (err: Exception?, cleartext: ByteArray) -> Unit