package me.uport.sdk.credentials

import com.uport.sdk.signer.Signer
import me.uport.sdk.core.ITimeProvider
import me.uport.sdk.core.SystemTimeProvider

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

    @Suppress("EnumEntryName")
    enum class RequestType {
        shareReq,
        shareResp,
        verReq,
        ethtx
    }

}