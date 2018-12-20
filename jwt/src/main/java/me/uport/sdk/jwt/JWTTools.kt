package me.uport.sdk.jwt

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.uport.sdk.signer.*
import me.uport.sdk.core.*
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.httpsdid.HttpsDIDResolver
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jwt.model.JwtHeader
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K_R
import me.uport.sdk.jwt.model.JwtPayload
import me.uport.sdk.serialization.mapAdapter
import me.uport.sdk.serialization.moshi
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDResolver
import org.kethereum.crypto.CURVE
import org.kethereum.crypto.model.PUBLIC_KEY_SIZE
import org.kethereum.crypto.model.PublicKey
import org.kethereum.crypto.toAddress
import org.kethereum.encodings.decodeBase58
import org.kethereum.extensions.toBigInteger
import org.kethereum.hashes.sha256
import org.kethereum.model.SignatureData
import org.spongycastle.asn1.x9.X9IntegerConverter
import org.spongycastle.math.ec.ECAlgorithms
import org.spongycastle.math.ec.ECPoint
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.hexToByteArray
import java.math.BigInteger
import java.security.SignatureException
import kotlin.experimental.and

/**
 * Tools for Verifying, Creating, and Decoding uport JWTs
 *
 * the [timeProvider] defaults to [SystemTimeProvider] but you can configure it for testing or for "was valid at" scenarios
 */
class JWTTools(
        private val timeProvider: ITimeProvider = SystemTimeProvider
) {
    private val notEmpty: (String) -> Boolean = { !it.isEmpty() }
    private val IAT_SKEW = 300

    init {

        // blank did declarations
        val blankUportDID = "did:uport:2nQs23uc3UN6BBPqGHpbudDxBkeDRn553BB"
        val blankEthrDID = "did:ethr:0x0000000000000000000000000000000000000000"
        val blankHttpsDID = "did:https:example.com"

        // register default Ethr DID resolver if Universal DID is unable to resolve blank Ethr DID
        if (!UniversalDID.canResolve(blankEthrDID)) {
            val defaultRPC = JsonRPC(Networks.mainnet.rpcUrl)
            UniversalDID.registerResolver(EthrDIDResolver(defaultRPC))
        }

        // register default Uport DID resolver if Universal DID is unable to resolve blank Uport DID
        if (!UniversalDID.canResolve(blankUportDID)) {
            UniversalDID.registerResolver(UportDIDResolver())
        }

        // register default https DID resolver if Universal DID is unable to resolve blank https DID
        if (!UniversalDID.canResolve(blankHttpsDID)) {
            UniversalDID.registerResolver(HttpsDIDResolver())
        }
    }

    /**
     * This coroutine method creates a signed JWT from a [payload] Map and an abstracted [Signer]
     * You're also supposed to pass the [issuerDID] and can configure the algorithm used and expiry time
     *
     * @param payload a map containing the fields forming the payload of this JWT
     * @param issuerDID a DID string that will be set as the `iss` field in the JWT payload.
     *                  The signature produced by the signer should correspond to this DID.
     *                  If the `iss` field is already part of the [payload], that will get overwritten.
     *                  **The [issuerDID] is NOT checked for format, nor for a match with the signer.**
     * @param signer a [Signer] that will produce the signature section of this JWT.
     *                  The signature should correspond to the [issuerDID].
     * @param expiresInSeconds number of seconds of validity of this JWT. You may omit this param if
     *                  an `exp` timestamp is already part of the [payload].
     *                  If there is no `exp` field in the payload and the param is not specified,
     *                  it defaults to [DEFAULT_JWT_VALIDITY_SECONDS]
     * @param algorithm defaults to `ES256K-R`. The signing algorithm for this JWT.
     *                  Supported types are `ES256K` for uport DID and `ES256K-R` for ethr-did and the rest
     *
     */
    suspend fun createJWT(payload: Map<String, Any>, issuerDID: String, signer: Signer, expiresInSeconds: Long = DEFAULT_JWT_VALIDITY_SECONDS, algorithm: String = ES256K_R): String {
        val mapAdapter = moshi.mapAdapter<String, Any>(String::class.java, Any::class.java)

        val mutablePayload = payload.toMutableMap()

        val header = JwtHeader(alg = algorithm)

        val iatSeconds = Math.floor(timeProvider.now() / 1000.0).toLong()
        val expSeconds = iatSeconds + expiresInSeconds

        mutablePayload["iat"] = iatSeconds
        mutablePayload["exp"] = payload["exp"] ?: expSeconds
        mutablePayload["iss"] = issuerDID

        @Suppress("SimplifiableCallChain", "ConvertCallChainIntoSequence")
        val signingInput = listOf(header.toJson(), mapAdapter.toJson(mutablePayload))
                .map { it.toBase64UrlSafe() }
                .joinToString(".")

        val jwtSigner = JWTSignerAlgorithm(header)
        val signature: String = jwtSigner.sign(signingInput, signer)
        return listOf(signingInput, signature).joinToString(".")
    }

    @Deprecated("This method has been deprecated in favor of `createJWT` because it is too coupled to the UportHDSigner mechanics", ReplaceWith("createJWT()"))
    fun create(context: Context, payload: JwtPayload, rootHandle: String, derivationPath: String, prompt: String = "", recoverable: Boolean = false, callback: (err: Exception?, encodedJWT: String?) -> Unit) {
        //create header and convert the parts to json strings
        val header = if (!recoverable) {
            JwtHeader(alg = ES256K)
        } else {
            JwtHeader(alg = ES256K_R)
        }
        val headerJsonString = header.toJson()
        val payloadJsonString = jwtPayloadAdapter.toJson(payload)
        //base 64 encode the jwt parts
        val headerEncodedString = headerJsonString.toBase64UrlSafe()
        val payloadEncodedString = payloadJsonString.toBase64UrlSafe()

        //FIXME: UportHDSigner should not be expecting base64 payloads
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

        //Parse Json
        if (headerString[0] != '{' || payloadString[0] != '{')
            throw InvalidJWTException("Invalid JSON format, should start with {")
        else {
            val header = JwtHeader.fromJson(headerString) // JSONObject(headerString)
            val payload = jwtPayloadAdapter.fromJson(payloadString) //JSONObject(payloadString
            return Triple(header!!, payload!!, signatureBytes)
        }
    }

    /**
     * Verifies a jwt [token]
     * @params jwt token
     * @throws InvalidJWTException when the current time is not within the time range of payload iat and exp
     * @return a [JwtPayload] if the verification is successful and `null` if it fails
     */
    suspend fun verify(token: String): JwtPayload? {
        val (_, payload, signatureBytes) = decode(token)

        val now = timeProvider.now() + IAT_SKEW

        if (payload.iat != null && payload.iat > now) {
            throw InvalidJWTException ("Jwt not valid yet (issued in the future) iat: ${payload.iat}")
        }

        if (payload.exp != null && payload.exp < now) {
            throw InvalidJWTException("JWT has expired: exp: ${payload.exp}")
        }

        val ddo: DIDDocument = UniversalDID.resolve(payload.iss)

        // extract header and payload from token and convert to bytes
        val signingInputBytes = token.substringBeforeLast('.').toByteArray()

        // extract recoveryByte if available or generate a new one
        val recoveryBytes = if (signatureBytes.size > 64)
            signatureBytes.sliceArray(64..64) // an array of just the recovery byte
        else
            byteArrayOf(27, 28) //try all recovery options

        // Generate signature from recovery byte
        val signatures = recoveryBytes.map {
            signatureBytes.decodeJose(it)
        }

        for (sigData in signatures) {

            val recoveredPubKey: BigInteger = try {
                signedJwtToKey(signingInputBytes, sigData)
            } catch (e: Exception) {
                BigInteger.ZERO
            }

            val pubKeyNoPrefix = PublicKey(recoveredPubKey).normalize()
            val recoveredAddress = pubKeyNoPrefix.toAddress().cleanHex.toLowerCase()

            val matches = ddo.publicKey.map { pubKeyEntry ->

                val pkBytes = pubKeyEntry.publicKeyHex?.hexToByteArray()
                        ?: pubKeyEntry.publicKeyBase64?.decodeBase64()
                        ?: pubKeyEntry.publicKeyBase58?.decodeBase58()
                        ?: ByteArray(PUBLIC_KEY_SIZE)
                val pubKey = PublicKey(pkBytes.toBigInteger()).normalize()

                (pubKeyEntry.ethereumAddress?.clean0xPrefix() ?: pubKey.toAddress().cleanHex)

            }.filter { ethereumAddress ->

                //this method of validation only works for uPort style JWTs, where the publicKeys
                // can be converted to ethereum addresses
                ethereumAddress.toLowerCase() == recoveredAddress

            }

            if (matches.isNotEmpty()) {
                return payload
            }
        }
        return null
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
        // two possibilities. So it'DEFAULT_REGISTRY_ADDRESS encoded in the recId.
        val r = decompressKey(x, recId and 1 == 1)
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers
        //        responsibility).
        if (!r.multiply(n).isInfinity) {
            return null
        }
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        val e = BigInteger(1, message)
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via
        //        iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * DEFAULT_REGISTRY_ADDRESS ** R) + (mi(r) * -e ** G)
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

    companion object {
        //Create adapters with each object
        val jwtPayloadAdapter: JsonAdapter<JwtPayload> by lazy { moshi.adapter(JwtPayload::class.java) }

        /**
         * 5 minutes. The default number of seconds of validity of a JWT, in case no other interval is specified.
         */
        const val DEFAULT_JWT_VALIDITY_SECONDS = 300L
    }

}

private data class ECDSASignature internal constructor(val r: BigInteger, val s: BigInteger)