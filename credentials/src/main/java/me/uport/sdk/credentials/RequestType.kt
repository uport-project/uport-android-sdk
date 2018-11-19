package me.uport.sdk.credentials

import android.support.annotation.Keep

/**
 * Supported (known) types of JWT requests/responses
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