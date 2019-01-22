@file:Suppress("unused")

package me.uport.sdk.core

import org.kethereum.extensions.toHexStringNoPrefix
import org.spongycastle.util.encoders.Base64
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.prepend0xPrefix
import java.math.BigInteger
import java.nio.charset.Charset
import kotlinx.coroutines.Dispatchers.Main as mainLooperContext

//using spongy castle implementation because the android one can't be used properly in tests
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

/**
 * Converts the bytes of this string into a base64 string usable in a URL
 */
fun String.toBase64UrlSafe() = this.toBase64().replace('+', '-').replace('/', '_')

/**
 * Converts this byte array into a base64 string usable in a URL
 */
fun ByteArray.toBase64UrlSafe() = this.toBase64().replace('+', '-').replace('/', '_')

/**
 * Decodes a base64 string into a bytearray.
 * Supports unpadded and url-safe strings as well.
 */
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


/**
 * Converts a hex string to another hex string pre-padded with zeroes until it represents at least 32 bytes
 */
fun String.hexToBytes32() = clean0xPrefix().padStart(64, '0').prepend0xPrefix()

/**
 * Converts this BigInteger into a hex string that is represented by at least 32 bytes.
 * The hex representation will be left-padded with zeroes if needed
 */
fun BigInteger.toBytes32String() = toHexStringNoPrefix().padStart(64, '0').prepend0xPrefix()

/**
 * shorthand to use the utf8 Charset
 */
val utf8: Charset = Charset.forName("UTF-8")

/**
 * interprets the underlying ByteArray as String
 */
fun ByteArray.bytes32ToString() = this.toString(utf8)