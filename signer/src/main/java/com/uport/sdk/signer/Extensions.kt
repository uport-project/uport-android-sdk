package com.uport.sdk.signer

import android.os.Build
import org.kethereum.crypto.ECKeyPair
import org.kethereum.crypto.PRIVATE_KEY_SIZE
import org.kethereum.extensions.toBytesPadded
import org.kethereum.model.SignatureData
import org.spongycastle.asn1.ASN1EncodableVector
import org.spongycastle.asn1.ASN1Encoding
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequence
import org.spongycastle.util.encoders.Base64
import org.walleth.khex.toNoPrefixHexString
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.*


//using spongy castle implementation because the android one can't be mocked in tests
/**
 * Creates a base64 representation of the given byteArray, without padding
 */
fun ByteArray.toBase64(): String = Base64.toBase64String(this).replace("=", "")

/**
 * Creates a base64 representation of the byteArray that backs this string, without padding
 */
fun String.toBase64() = this.toByteArray().toBase64()

/**
 * pads a base64 string with a proper number of '='
 */
fun String.padBase64() = this.padEnd(this.length + (4 - this.length % 4) % 4, '=')

fun String.toBase64UrlSafe() = this.toBase64().replace('+', '-').replace('/', '_')
fun ByteArray.toBase64UrlSafe() = this.toBase64().replace('+', '-').replace('/', '_')

fun String.decodeBase64(): ByteArray = this
        //force non-url safe and add padding so that it can be applied to all b64 formats
        .replace('-', '+')
        .replace('_', '/')
        .padBase64()
        .let {
            if (it.isEmpty())
                byteArrayOf()
            else
                Base64.decode(it)
        }

const val SIG_COMPONENT_SIZE = PRIVATE_KEY_SIZE
const val SIG_SIZE = SIG_COMPONENT_SIZE * 2
const val SIG_RECOVERABLE_SIZE = SIG_SIZE + 1

/**
 * Returns the JOSE encoding of the standard signature components (joined by empty string)
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
        bos.write(byteArrayOf(this.v))
    }
    return bos.toByteArray().toBase64UrlSafe()
}

fun String.decodeJose(recoveryParam: Byte = 27): SignatureData {
    val bytes = this.decodeBase64()
    val rBytes = Arrays.copyOfRange(bytes, 0, SIG_COMPONENT_SIZE)
    val sBytes = Arrays.copyOfRange(bytes, SIG_COMPONENT_SIZE, SIG_SIZE)
    val v = if (bytes.size > SIG_SIZE) bytes[SIG_SIZE] else recoveryParam
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

internal fun ECKeyPair.getUncompressedPublicKeyWithPrefix(): ByteArray {
    val pubBytes = this.publicKey.toBytesPadded(UportSigner.UNCOMPRESSED_PUBLIC_KEY_SIZE)
    pubBytes[0] = 0x04
    return pubBytes
}

fun BigInteger.keyToBase64(keySize: Int = PRIVATE_KEY_SIZE): String =
        this.toBytesPadded(keySize).toBase64().padBase64()

fun hasMarshmallow(): Boolean = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)

typealias EncryptionCallback = (err: Exception?, ciphertext: String) -> Unit
typealias DecryptionCallback = (err: Exception?, cleartext: ByteArray) -> Unit