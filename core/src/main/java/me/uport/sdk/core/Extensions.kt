package me.uport.sdk.core

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE
import org.kethereum.extensions.toHexStringNoPrefix
import org.spongycastle.util.encoders.Base64
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.prepend0xPrefix
import java.math.BigInteger
import java.nio.charset.Charset
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlinx.coroutines.experimental.android.UI as mainLooperContext

/**
 * Shorthand for a mockable UI context in unit tests
 */
val UI by lazy { coroutineUiContextInitBlock() }

private var coroutineUiContextInitBlock: () -> CoroutineContext = { mainLooperContext }

/**
 * Call this in @Before methods where you need to interact with UI context
 */
@VisibleForTesting(otherwise = PRIVATE)
fun stubUiContext() {
    coroutineUiContextInitBlock = { EmptyCoroutineContext }
}

//using spongy castle implementation because the android one can't be mocked in tests
fun ByteArray.toBase64(): String = Base64.toBase64String(this).replace("=", "")

fun String.toBase64() = this.toByteArray().toBase64()

fun String.toBase64UrlSafe() = this.toBase64().replace('+', '-').replace('/', '_')
fun ByteArray.toBase64UrlSafe() = this.toBase64().replace('+', '-').replace('/', '_')

fun String.decodeBase64(): ByteArray = this
        //force non-url safe and add padding so that it can be applied to all b64 formats
        .replace('-', '+')
        .replace('_', '/')
        .let {
            val numPadding = it.length + (4 - it.length % 4) % 4
            it.padEnd(numPadding, '=')
        }
        .let {
            if (it.isEmpty())
                byteArrayOf()
            else
                Base64.decode(it)
        }


fun String.hexToBytes32() = clean0xPrefix().padStart(64, '0').prepend0xPrefix()
fun BigInteger.toBytes32String() = toHexStringNoPrefix().padStart(64, '0').prepend0xPrefix()

val utf8: Charset = Charset.forName("UTF-8")
fun ByteArray.bytes32ToString() = this.toString(utf8)