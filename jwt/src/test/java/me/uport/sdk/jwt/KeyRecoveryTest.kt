package me.uport.sdk.jwt

import com.uport.sdk.signer.KPSigner
import com.uport.sdk.signer.signJWT
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.kethereum.crypto.model.PrivateKey
import org.kethereum.crypto.publicKeyFromPrivate
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.extensions.toHexStringNoPrefix
import org.kethereum.hashes.sha256
import org.walleth.khex.toHexString

class KeyRecoveryTest {

    //  Uncomment `@Test` to iterate over 1000 * 1000 key/message combinations. Takes a lot of time.
    //@Test
    fun `can recover key from JWT signature`() = runBlocking {
        val tools = JWTTools()

        for (i in 0 until 1000) {
            val privateKey = "super secret $i".toByteArray().sha256().toHexString()
            val pubKey = publicKeyFromPrivate(PrivateKey(privateKey.hexToBigInteger())).key.toHexStringNoPrefix()
            val signer = KPSigner(privateKey)
            println("trying key $i on 1000 messages")
            for (j in 0 until 1000) {
                val message = "hello $i".toByteArray(Charsets.UTF_8)

                val sigData = signer.signJWT(message)

                val recovered = tools.signedJwtToKey(message, sigData).toHexStringNoPrefix()

                assertEquals("failed at key $i, message $j", pubKey, recovered)
            }
        }
    }

}