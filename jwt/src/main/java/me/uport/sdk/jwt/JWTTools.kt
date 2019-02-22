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
import me.uport.sdk.universaldid.PublicKeyEntry
import me.uport.sdk.universaldid.PublicKeyType.Companion.EcdsaPublicKeySecp256k1
import me.uport.sdk.universaldid.PublicKeyType.Companion.Secp256k1SignatureVerificationKey2018
import me.uport.sdk.universaldid.PublicKeyType.Companion.Secp256k1VerificationKey2018
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDResolver
import org.kethereum.crypto.model.PUBLIC_KEY_SIZE
import org.kethereum.crypto.model.PublicKey
import org.kethereum.crypto.toAddress
import org.kethereum.encodings.decodeBase58
import org.kethereum.extensions.toBigInteger
import org.kethereum.hashes.sha256
import org.kethereum.model.SignatureData
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.hexToByteArray
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.SignatureException

/**
 * Tools for Verifying, Creating, and Decoding uport JWTs
 *
 * the [timeProvider] defaults to [SystemTimeProvider] but you can configure it for testing or for "was valid at" scenarios
 */
class JWTTools(
        private val timeProvider: ITimeProvider = SystemTimeProvider
) {
    private val notEmpty: (String) -> Boolean = { !it.isEmpty() }

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
            val defaultRPC = JsonRPC(Networks.rinkeby.rpcUrl)
            UniversalDID.registerResolver(UportDIDResolver(defaultRPC))
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

        val iatSeconds = Math.floor(timeProvider.nowMs() / 1000.0).toLong()
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

    /**
     * @Deprecated Please use [createJWT]
     */
    @Suppress("LongParameterList")
    @Deprecated("This method has been deprecated in favor of `createJWT`", ReplaceWith("createJWT()"))
    fun create(context: Context, payload: JwtPayload, rootHandle: String, derivationPath: String, prompt: String = "", recoverable: Boolean = false, callback: (err: Exception?, encodedJWT: String) -> Unit) {
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
     * @return the JWT Header,Payload and signature as parsed objects
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
            val header = JwtHeader.fromJson(headerString)
                    ?: throw InvalidJWTException("unable to parse the JWT header for $token")
            val payload = jwtPayloadAdapter.fromJson(payloadString)
                    ?: throw InvalidJWTException("unable to parse the JWT payload for $token")
            return Triple(header, payload, signatureBytes)
        }
    }

    /**
     * Decodes a JWT into it's 3 components, keeping the payload as a Map type
     *
     * This is useful for situations where the known [JwtPayload] fields are not enough.
     */
    fun decodeRaw(token: String): Triple<JwtHeader, Map<String, Any?>, ByteArray> {
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
            val header = JwtHeader.fromJson(headerString)
                    ?: throw InvalidJWTException("unable to parse the JWT header for $token")
            val mapAdapter = moshi.mapAdapter<String, Any>(String::class.java, Any::class.java)

            val payload = mapAdapter.fromJson(payloadString)
                    ?: throw InvalidJWTException("unable to parse the JWT payload for $token")

            return Triple(header, payload, signatureBytes)
        }
    }


    /**
     * Verifies a jwt [token]
     * @params jwt token
     * @throws InvalidJWTException when the current time is not within the time range of payload iat and exp
     *          when no public key matches are found in the DID document
     * @return a [JwtPayload] if the verification is successful and `null` if it fails
     */
    suspend fun verify(token: String, auth: Boolean = false): JwtPayload {
        val (header, payload, signatureBytes) = decode(token)

        if (payload.iat != null && payload.iat > (timeProvider.nowMs() / 1000 + TIME_SKEW)) {
            throw InvalidJWTException("Jwt not valid yet (issued in the future) iat: ${payload.iat}")
        }

        if (payload.exp != null && payload.exp <= (timeProvider.nowMs() / 1000 - TIME_SKEW)) {
            throw InvalidJWTException("JWT has expired: exp: ${payload.exp}")
        }

        val publicKeys = resolveAuthenticator(header.alg, payload.iss, auth)

        val signingInputBytes = token.substringBeforeLast('.').toByteArray(utf8)

        val sigData = signatureBytes.decodeJose()

        val signatureIsValid = verificationMethod[header.alg]
                ?.invoke(publicKeys, sigData, signingInputBytes)
                ?: throw InvalidAlgorithmParameterException("JWT algorithm ${header.alg} not supported")

        if (signatureIsValid) {
            return payload
        } else {
            throw InvalidJWTException("Signature invalid for JWT. DID document for ${payload.iss} does not have any matching public keys")
        }

    }

    /**
     * maps known algorithms to the corresponding verification method
     */
    private val verificationMethod = mapOf(
            ES256K_R to ::verifyRecoverableES256K,
            ES256K to ::verifyES256K
    )

    private fun verifyES256K(publicKeys: List<PublicKeyEntry>, sigData: SignatureData, signingInputBytes: ByteArray): Boolean {

        val messageHash = signingInputBytes.sha256()

        val matches = publicKeys.map { pubKeyEntry ->

            val pkBytes = pubKeyEntry.publicKeyHex?.hexToByteArray()
                    ?: pubKeyEntry.publicKeyBase64?.decodeBase64()
                    ?: pubKeyEntry.publicKeyBase58?.decodeBase58()
                    ?: ByteArray(PUBLIC_KEY_SIZE)
            PublicKey(pkBytes.toBigInteger()).normalize()

        }.filter { publicKey ->

            ecVerify(messageHash, sigData, publicKey)
        }

        return matches.isNotEmpty()
    }

    private fun verifyRecoverableES256K(publicKeys: List<PublicKeyEntry>, sigData: SignatureData, signingInputBytes: ByteArray): Boolean {

        val recoveredPubKey: BigInteger = try {
            signedJwtToKey(signingInputBytes, sigData)
        } catch (e: SignatureException) {
            BigInteger.ZERO
        }

        val pubKeyNoPrefix = PublicKey(recoveredPubKey).normalize()
        val recoveredAddress = pubKeyNoPrefix.toAddress().cleanHex.toLowerCase()

        val matches = publicKeys.map { pubKeyEntry ->

            val pkBytes = pubKeyEntry.publicKeyHex?.hexToByteArray()
                    ?: pubKeyEntry.publicKeyBase64?.decodeBase64()
                    ?: pubKeyEntry.publicKeyBase58?.decodeBase58()
                    ?: ByteArray(PUBLIC_KEY_SIZE)
            val pubKey = PublicKey(pkBytes.toBigInteger()).normalize()

            (pubKeyEntry.ethereumAddress?.clean0xPrefix() ?: pubKey.toAddress().cleanHex)

        }.filter { ethereumAddress ->

            ethereumAddress.toLowerCase() == recoveredAddress
        }

        return matches.isNotEmpty()
    }

    /**
     * This method obtains a [DIDDocument] corresponding to the [issuer] and returns a list of [PublicKeyEntry]
     * that can be used to check JWT signatures
     *
     * @param [auth] decide if the returned list should also be filtered against the `authentication`
     * entries in the DIDDocument
     *
     */
    suspend fun resolveAuthenticator(alg: String, issuer: String, auth: Boolean): List<PublicKeyEntry> {

        if (alg !in verificationMethod.keys) {
            throw InvalidAlgorithmParameterException("JWT algorithm '$alg' not supported")
        }

        val doc: DIDDocument = UniversalDID.resolve(issuer)

        val authenticationKeys: List<String> = if (auth) {
            doc.authentication.map { it.publicKey }
        } else {
            emptyList() // return an empty list
        }

        val authenticators = doc.publicKey.filter {

            // filter public keys which belong to the list of supported key types
            supportedKeyTypes.contains(it.type) && (!auth || (authenticationKeys.contains(it.id)))
        }

        if (auth && (authenticators.isEmpty())) throw InvalidJWTException("DID document for $issuer does not have public keys suitable for authenticating user")
        if (authenticators.isEmpty()) throw InvalidJWTException("DID document for $issuer does not have public keys for $alg")

        return authenticators
    }

    companion object {
        //Create adapters with each object
        val jwtPayloadAdapter: JsonAdapter<JwtPayload> by lazy { moshi.adapter(JwtPayload::class.java) }

        /**
         * 5 minutes. The default number of seconds of validity of a JWT, in case no other interval is specified.
         */
        const val DEFAULT_JWT_VALIDITY_SECONDS = 300L

        private const val TIME_SKEW = 300L

        /**
         * List of supported key types for verifying DID JWT signatures
         */
        val supportedKeyTypes = listOf(
                Secp256k1VerificationKey2018,
                Secp256k1SignatureVerificationKey2018,
                EcdsaPublicKeySecp256k1
        )
    }

}

