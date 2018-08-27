package com.uport.sdk.signer.protocol.exceptions

class ValidationException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    constructor(message: String, cause: Throwable) : super(message, cause)
}