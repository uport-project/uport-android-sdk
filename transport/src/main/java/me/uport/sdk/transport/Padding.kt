package me.uport.sdk.transport

import kotlin.math.ceil
import kotlin.math.roundToInt

internal const val BLOCK_SIZE = 64

internal fun String.padToBlock(): ByteArray {
    val bytes = this.toByteArray(Charsets.UTF_8)
    val paddedSize = (ceil(bytes.size.toDouble() / BLOCK_SIZE) * BLOCK_SIZE).roundToInt()
    val padding = ByteArray(paddedSize - bytes.size) { '\u0000'.toByte() }
    return bytes + padding
}

internal fun ByteArray.unpadFromBlock(): String {
    var lastNonNullIndex = this.size - 1
    for (i in lastNonNullIndex downTo 0) {
        if (this[i] != 0.toByte()) {
            lastNonNullIndex = i
            break
        }
    }
    val unpaddedBytes = this.copyOfRange(0, lastNonNullIndex + 1)
    return String(unpaddedBytes, Charsets.UTF_8)
}