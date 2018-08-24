package me.uport.sdk.core

import android.util.Base64
import org.kethereum.extensions.toHexStringNoPrefix
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.prepend0xPrefix
import java.math.BigInteger
import java.nio.charset.Charset

fun String.toBase64() = Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)
//using spongy castle implementation because the android one can't be mocked in tests
fun ByteArray.toBase64() = org.spongycastle.util.encoders.Base64.toBase64String(this)
fun String.toBase64UrlSafe() = Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)
fun ByteArray.toBase64UrlSafe() = Base64.encodeToString(this, Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)

fun String.decodeBase64(): ByteArray = this
        //force url safe no padding so that it can be applied to all b64 formats
        .replace('+', '-')
        .replace('/', '_')
        .replace("=", "")
        .let { Base64.decode(it.toByteArray(), Base64.URL_SAFE or Base64.NO_PADDING) }


fun String.hexToBytes32() = clean0xPrefix().padStart(64, '0').prepend0xPrefix()
fun BigInteger.toBytes32String() = toHexStringNoPrefix().padStart(64, '0').prepend0xPrefix()

val utf8: Charset = Charset.forName("UTF-8")
fun ByteArray.bytes32ToString() = this.toString(utf8)