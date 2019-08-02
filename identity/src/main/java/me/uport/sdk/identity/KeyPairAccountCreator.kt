package me.uport.sdk.identity

import me.uport.sdk.signer.KPSigner

/**
 * [KeyPairAccountCreator] backed by a [KPSigner] that creates a [KeyPairAccount]
 *
 * This account creator is still experimental and should only be used in test a environment
 *
 * API volatility: __high__
 */
class KeyPairAccountCreator(privateKey: String) : AccountCreator {

    private val signer = KPSigner(privateKey)

    private fun createOrImportAccount(networkId: String): KeyPairAccount {

        return KeyPairAccount(
                networkId,
                signer
        )
    }

    override suspend fun createAccount(networkId: String, forceRecreate: Boolean): KeyPairAccount {
        return createOrImportAccount(networkId)
    }

    override suspend fun importAccount(networkId: String, seedPhrase: String, forceRecreate: Boolean): KeyPairAccount {
        return createOrImportAccount(networkId)
    }

    override suspend fun deleteAccount(handle: String) {
        //do nothing
    }
}
