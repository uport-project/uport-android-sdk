package me.uport.sdk.jwt

data class SplitEncodedToken(
        val header: String,
        val payload: String,
        val signature: String
)

val hasThreeParts: (List<String>) -> Boolean = { it.size == 3 }

fun splitToken(token: String): SplitEncodedToken {
    val parts: List<String>? = token.split('.', limit = 3)
    if (parts !== null && hasThreeParts(parts)) {
        val splitET = SplitEncodedToken(parts[0], parts[1], parts[2])
        return splitET
    } else {
        throw IllegalArgumentException("Token must have 3 parts: Header, Payload, and Signature")
    }
}