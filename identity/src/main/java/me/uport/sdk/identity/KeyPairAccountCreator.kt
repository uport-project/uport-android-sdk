package me.uport.sdk.identity

import com.uport.sdk.signer.KPSigner

/**
 * [KeyPairAccountCreator] backed by a [KPSigner] that creates a [KeyPairAccount]
 *
 */
class KeyPairAccountCreator(private val privateKey: String) : AccountCreator {

    private val signer = KPSigner(privateKey)

    private fun createOrImportAccount(networkId: String): KeyPairAccount {

        return KeyPairAccount(
                signer.getAddress(),
                signer.getAddress(),
                networkId,
                signer.getAddress(),
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
        TODO("not implemented") //this does not apply to KeyPairAccountCreator
    }
}