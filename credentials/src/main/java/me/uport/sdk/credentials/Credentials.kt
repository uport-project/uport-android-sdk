package me.uport.sdk.credentials

import android.support.annotation.Keep
import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE
import com.uport.sdk.signer.Signer
import me.uport.mnid.MNID
import me.uport.sdk.core.ITimeProvider
import me.uport.sdk.core.SystemTimeProvider
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.jwt.model.JwtHeader

/**
 * The [Credentials] class should allow you to create the signed payloads used in uPort including
 * verifiable claims and signed mobile app requests (ex. selective disclosure requests
 * for user data). It should also provide signature verification over signed payloads.
 */
class Credentials(
        private val did: String,
        private val signer: Signer,
        private val clock: ITimeProvider = SystemTimeProvider
) {

    /**
     * Supported (known) types of requests/responses
     */
    @Keep
    @Suppress("EnumEntryName")
    enum class RequestType {
        /**
         * a selective disclosure request
         * See also:  https://github.com/uport-project/specs/blob/develop/messages/sharereq.md
         */
        shareReq,

        /**
         * a selective disclosure response
         * See also: https://github.com/uport-project/specs/blob/develop/messages/shareresp.md
         */
        shareResp,

        /**
         * See also:  https://github.com/uport-project/specs/blob/develop/messages/verificationreq.md
         */
        verReq,

        /**
         * See also:  https://github.com/uport-project/specs/blob/develop/messages/signtypeddata.md
         */
        eip712Req,

        /**
         * See also:  https://github.com/uport-project/specs/blob/develop/messages/tx.md
         */
        ethtx
    }

    /**
     *  Creates a JWT using the given [payload]
     */
    suspend fun signJWT(payload: Map<String, Any>, expiresInSeconds: Long = 300L): String {
        val normDID = normalizeKnownDID(this.did)
        val alg = if (normDID.startsWith("did:uport:")) JwtHeader.ES256K else JwtHeader.ES256K_R
        return JWTTools(clock).createJWT(payload, normDID, this.signer, expiresInSeconds = expiresInSeconds, algorithm = alg)
    }

    companion object {

        /**
         * Attempts to normalize a [potentialDID] to a known format.
         *
         * This will transform an ethereum address into an ethr-did and an MNID string into a uport-did
         */
        @VisibleForTesting(otherwise = PRIVATE)
        internal fun normalizeKnownDID(potentialDID: String): String {

            //ignore if it's already a did
            if (potentialDID.matches("^did:(.*)?:.*".toRegex()))
                return potentialDID

            //match an ethereum address
            "^(0[xX])*([0-9a-fA-F]{40})".toRegex().find(potentialDID)?.let {
                val (_, hexDigits) = it.destructured
                return "did:ethr:0x$hexDigits"
            }

            //match an MNID
            if (MNID.isMNID(potentialDID)) {
                return "did:uport:$potentialDID"
            }

            return potentialDID
        }
    }

}