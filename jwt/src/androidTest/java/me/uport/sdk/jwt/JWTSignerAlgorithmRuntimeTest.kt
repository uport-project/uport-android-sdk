package me.uport.sdk.jwt

import android.support.test.InstrumentationRegistry
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import com.uport.sdk.signer.encryption.KeyProtection
import com.uport.sdk.signer.importHDSeed
import kotlinx.coroutines.experimental.runBlocking
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K_R
import org.junit.Assert.assertEquals
import org.junit.Test

class JWTSignerAlgorithmRuntimeTest {

    @Test
    fun can_sign_using_non_recoverable_alg() = runBlocking {

        val targetContext = InstrumentationRegistry.getTargetContext()

        val referenceSeed = "vessel ladder alter error federal sibling chat ability sun glass valve picture"
        val referencePayload = "Hello, world!"

        val baseSigner = UportHDSigner()
        val (rootAddress, _) = baseSigner.importHDSeed(targetContext, KeyProtection.Level.SIMPLE, referenceSeed)

        val testedSigner = UportHDSignerImpl(
                context = targetContext,
                uportHDSigner = baseSigner,
                rootAddress = rootAddress,
                deviceAddress = rootAddress
        )

        val signature = JWTSignerAlgorithm(ES256K).sign(referencePayload, testedSigner)

        val expectedSignature = "a82BRGGDrxk8pKFy1cXCY0WQOyR3DZC115D3Sp3sH2jiuFs8ksm0889Y3kbnmX2O-24UsuUy0T36Iu4C86Q9XQ"
        assertEquals(expectedSignature, signature)
        assertEquals(64, signature.decodeBase64().size)
    }


    @Test
    fun can_sign_using_recoverable_alg() = runBlocking {

        val targetContext = InstrumentationRegistry.getTargetContext()

        val referenceSeed = "vessel ladder alter error federal sibling chat ability sun glass valve picture"
        val referencePayload = "Hello, world!"

        val baseSigner = UportHDSigner()
        val (rootAddress, _) = baseSigner.importHDSeed(targetContext, KeyProtection.Level.SIMPLE, referenceSeed)

        val testedSigner = UportHDSignerImpl(
                context = targetContext,
                uportHDSigner = baseSigner,
                rootAddress = rootAddress,
                deviceAddress = rootAddress
        )

        val signature = JWTSignerAlgorithm(ES256K_R).sign(referencePayload, testedSigner)

        val expectedSignature = "a82BRGGDrxk8pKFy1cXCY0WQOyR3DZC115D3Sp3sH2jiuFs8ksm0889Y3kbnmX2O-24UsuUy0T36Iu4C86Q9XQE"
        assertEquals(expectedSignature, signature)
        assertEquals(65, signature.decodeBase64().size)
    }
}