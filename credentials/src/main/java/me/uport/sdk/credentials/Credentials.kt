package me.uport.sdk.credentials

import com.uport.sdk.signer.Signer

class Credentials(
        private val did: String,
        private val signer: Signer
) {

}