package me.uport.sdk.credentials

/**
 * Supported (known) types of JWT requests/responses
 */
@Suppress("EnumEntryName")
enum class JWTTypes {
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
     * FIXME: there is no `verResp` type coming back from the uPort app. It has to be inferred from the presence of the `claim` field
     * This is a synthetic JWTType
     */
    verResp,

    /**
     * See also:  https://github.com/uport-project/specs/blob/develop/messages/signtypeddata.md
     */
    eip712Req,

    /**
     * See also: https://github.com/uport-project/specs/blob/develop/messages/signtypeddataresp.md
     */
    eip712Resp,

    /**
     * See also:  https://github.com/uport-project/specs/blob/develop/messages/tx.md
     */
    ethtx,

    /**
     * https://github.com/uport-project/specs/blob/develop/messages/personalsignreq.md
     */
    personalSigReq,

    /**
     * https://github.com/uport-project/specs/blob/develop/messages/personalsignresp.md
     */
    personalSignResp
}
