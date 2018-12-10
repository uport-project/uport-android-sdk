package me.uport.sdk.transport

import kotlin.math.ceil
import kotlin.math.roundToInt

private const val BLOCK_SIZE = 64
private val matchNullCharAtEnd = "\u0000+$".toRegex()

internal fun String.pad(): String {
    val paddedSize = (ceil(length.toDouble() / BLOCK_SIZE) * BLOCK_SIZE).roundToInt()
    return this.padEnd(paddedSize, '\u0000')
}

internal fun String.unpad(): String {
    return this.replace(matchNullCharAtEnd, "")
}