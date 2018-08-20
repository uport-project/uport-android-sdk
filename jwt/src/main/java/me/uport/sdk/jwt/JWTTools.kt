package me.uport.sdk.jwt

//import org.kethereum.crypto.signedMessageToKey
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.getJoseEncoded
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.toBase64
import me.uport.sdk.core.toBase64UrlSafe
import me.uport.sdk.did.DIDResolver
import me.uport.sdk.jwt.model.JwtHeader
import me.uport.sdk.jwt.model.JwtPayload
import org.kethereum.crypto.CURVE
import org.kethereum.extensions.toHexStringZeroPadded
import org.kethereum.hashes.sha256
import org.kethereum.model.SignatureData
import org.spongycastle.asn1.x9.X9IntegerConverter
import org.spongycastle.math.ec.ECAlgorithms
import org.spongycastle.math.ec.ECPoint
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve
import org.walleth.khex.prepend0xPrefix
import java.math.BigInteger
import java.security.SignatureException
import kotlin.experimental.and

/**
 * Tools for Verifying, Creating, and Decoding uport JWT's
 */


class JWTTools {
    private val notEmpty: (String) -> Boolean = { !it.isEmpty() }

    fun create(context: Context, payload: JwtPayload, rootHandle: String, derivationPath: String, prompt: String = "", recoverable: Boolean = false, callback: (err: Exception?, encodedJWT: String?) -> Unit) {
        //JSON Parser
        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        //Create adapters with each object
        val jwtHeaderAdapter = moshi.adapter(JwtHeader::class.java)
        val jwtPayloadAdapter = moshi.adapter(JwtPayload::class.java)

        //create header and convert the parts to json strings
        val header = if (recoverable) {
            JwtHeader("JWT", "ES256K")
        } else {
            JwtHeader("JWT", "ES256K-R")
        }
        val headerJsonString = jwtHeaderAdapter.toJson(header)
        val payloadJsonString = jwtPayloadAdapter.toJson(payload)
        //base 64 encode the jwt parts
        val headerEncodedString = headerJsonString.toBase64UrlSafe()
        val payloadEncodedString = payloadJsonString.toBase64UrlSafe()

        //XXX: This is the crux of the bad behavior. signJwtBundle expects a Base64 string as payload and it was receiving plain text
        val messageToSign = "$headerEncodedString.$payloadEncodedString".toBase64()

        UportHDSigner().signJwtBundle(context, rootHandle, derivationPath, messageToSign, prompt) { err, signature ->
            val encodedJwt = "$headerEncodedString.$payloadEncodedString.${signature.getJoseEncoded(recoverable)}"
            callback(err, encodedJwt)
        }
    }

    /**
     * Decodes a jwt [token]
     * @param token is a string of 3 parts separated by .
     * @throws InvalidJWTException when the header or payload are empty or when they don't start with { (invalid json)
     * @return the JWT Header and Payload as a pair of JSONObjects
     */
    fun decode(token: String): Triple<JwtHeader, JwtPayload, ByteArray> {
        //Split token by . from jwtUtils
        val (encodedHeader, encodedPayload, encodedSignature) = splitToken(token)
        if (!notEmpty(encodedHeader))
            throw InvalidJWTException("Header cannot be empty")
        else if (!notEmpty(encodedPayload))
            throw InvalidJWTException("Payload cannot be empty")
        //Decode the pieces
        val headerString = String(encodedHeader.decodeBase64())
        val payloadString = String(encodedPayload.decodeBase64())
        val signatureBytes = encodedSignature.decodeBase64()
        //JSON Parser
        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        //Create adapters with each object
        val jwtHeaderAdapter = moshi.adapter(JwtHeader::class.java)
        val jwtPayloadAdapter = moshi.adapter(JwtPayload::class.java)

        //Parse Json
        if (headerString[0] != '{' || payloadString[0] != '{')
            throw InvalidJWTException("Invalid JSON format, should start with {")
        else {
            val header = jwtHeaderAdapter.fromJson(headerString) // JSONObject(headerString)
            val payload = jwtPayloadAdapter.fromJson(payloadString) //JSONObject(payloadString
            return Triple(header!!, payload!!, signatureBytes)
        }
    }

    fun verify(token: String, callback: (err: Exception?, payload: JwtPayload?) -> Unit) {
        val (_, payload, signatureBytes) = decode(token)
        //TODO: use a broader DID resolver, or perhaps determine the resolver type based on `header.algo` or `payload.iss`
        DIDResolver().getProfileDocument(payload.iss) { err, ddo ->
            if (err !== null)
                return@getProfileDocument callback(err, null)

            val tokenParts = token.split('.')
            val signingInput = tokenParts[0] + "." + tokenParts[1]
            val signingInputBytes = signingInput.toByteArray()

            val r = BigInteger(1, signatureBytes.sliceArray(0 until 32))
            val s = BigInteger(1, signatureBytes.sliceArray(32 until 64))
            val vArr = if (signatureBytes.size > 64)
                signatureBytes.sliceArray(64..64) // just the recovery byte
            else
                byteArrayOf(27, 28) //try all recovery options

            for (v in vArr) {
                val sig = SignatureData(r, s, v)
                val recoveredPubKey: BigInteger = signedJwtToKey(signingInputBytes, sig)
                val recoveredPubKeyString = recoveredPubKey.toHexStringZeroPadded(130).prepend0xPrefix()
                if (recoveredPubKeyString == ddo.publicKey)
                    return@getProfileDocument callback(err, payload)
            }
            return@getProfileDocument callback(InvalidSignatureException("Signature invalid: Public Key Mismatch"), null)
        }
    }

    /***
     * Adapted from Kethereum
     */
    private fun recoverFromSignature(recId: Int, sig: ECDSASignature, message: ByteArray?): BigInteger? {
        require(recId >= 0) { "recId must be positive" }
        require(sig.r.signum() >= 0) { "r must be positive" }
        require(sig.s.signum() >= 0) { "s must be positive" }
        require(message != null) { "message cannot be null" }

        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        val n = CURVE.n  // Curve order.
        val i = BigInteger.valueOf(recId.toLong() / 2)
        val x = sig.r.add(i.multiply(n))
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion
        //        routine specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R
        //        using the conversion routine specified in Section 2.3.4. If this conversion
        //        routine outputs “invalid”, then do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        val prime = SecP256K1Curve.q
        if (x >= prime) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are
        // two possibilities. So it's encoded in the recId.
        val r = decompressKey(x, recId and 1 == 1)
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers
        //        responsibility).
        if (!r.multiply(n).isInfinity) {
            return null
        }
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        val e = BigInteger(1, message!!)
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via
        //        iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n).
        // In the above equation ** is point multiplication and + is point addition (the EC group
        // operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For
        // example the additive inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and
        // -3 mod 11 = 8.
        val eInv = BigInteger.ZERO.subtract(e).mod(n)
        val rInv = sig.r.modInverse(n)
        val srInv = rInv.multiply(sig.s).mod(n)
        val eInvrInv = rInv.multiply(eInv).mod(n)
        val q = ECAlgorithms.sumOfTwoMultiplies(CURVE.g, eInvrInv, r, srInv)

        val qBytes = q.getEncoded(false)
        // We remove the prefix
        return BigInteger(1, qBytes)//Arrays.copyOfRange(qBytes, 0, qBytes.size))
    }

    /**
     * This function is taken from Kethereum
     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
     * */
    private fun decompressKey(xBN: BigInteger, yBit: Boolean): ECPoint {
        val x9 = X9IntegerConverter()
        val compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.curve))
        compEnc[0] = (if (yBit) 0x03 else 0x02).toByte()
        return CURVE.curve.decodePoint(compEnc)
    }

    /**
     * This Function is adapted from the Kethereum implementation
     * Given an arbitrary piece of text and an Ethereum message signature encoded in bytes,
     * returns the public key that was used to sign it. This can then be compared to the expected
     * public key to determine if the signature was correct.
     *
     * @param message RLP encoded message.
     * @param signatureData The message signature components
     * @return the public key used to sign the message
     * @throws SignatureException If the public key could not be recovered or if there was a
     * signature format error.
     */
    @Throws(SignatureException::class)
    //XXX: renamed the method to prevent accidental conflicts with kethereum imports
    fun signedJwtToKey(message: ByteArray, signatureData: SignatureData): BigInteger {

        val header = signatureData.v and 0xFF.toByte()
        // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
        //                  0x1D = second key with even y, 0x1E = second key with odd y
        if (header < 27 || header > 34) {
            throw SignatureException("Header byte out of range: $header")
        }

        val sig = ECDSASignature(signatureData.r, signatureData.s)

        val messageHash = message.sha256()
        val recId = header - 27
        return recoverFromSignature(recId, sig, messageHash)
                ?: throw SignatureException("Could not recover public key from signature")
    }

}

private data class ECDSASignature internal constructor(val r: BigInteger, val s: BigInteger)