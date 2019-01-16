package me.uport.sdk.jwt

import android.support.test.InstrumentationRegistry
import assertk.assert
import assertk.assertions.isEqualTo
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K_R
import org.junit.Before
import org.junit.Test

class JWTSignerAlgorithmRuntimeTest {

    private val referenceSeed = "vessel ladder alter error federal sibling chat ability sun glass valve picture"
    private var appContext = InstrumentationRegistry.getTargetContext()
    private lateinit var rootAddress: String

    @Before
    fun run_before_every_test() {
        val (handle, _) = runBlocking { ensureSeedIsImported(appContext, referenceSeed) }
        rootAddress = handle
    }

    @Test
    fun can_sign_using_non_recoverable_alg() = runBlocking {

        val referencePayload = "Hello, world!"

        val testedSigner = UportHDSignerImpl(
                context = appContext,
                uportHDSigner = UportHDSigner(),
                rootAddress = rootAddress,
                deviceAddress = rootAddress
        )

        val signature = JWTSignerAlgorithm(ES256K).sign(referencePayload, testedSigner)

        val expectedSignature = "a82BRGGDrxk8pKFy1cXCY0WQOyR3DZC115D3Sp3sH2jiuFs8ksm0889Y3kbnmX2O-24UsuUy0T36Iu4C86Q9XQ"
        assert(signature).isEqualTo(expectedSignature)
        assert(signature.decodeBase64().size).isEqualTo(64)
    }

    @Test
    fun can_sign_using_recoverable_alg() = runBlocking {

        val referencePayload = "Hello, world!"

        val testedSigner = UportHDSignerImpl(
                context = appContext,
                uportHDSigner = UportHDSigner(),
                rootAddress = rootAddress,
                deviceAddress = rootAddress
        )

        val signature = JWTSignerAlgorithm(ES256K_R).sign(referencePayload, testedSigner)

        val expectedSignature = "a82BRGGDrxk8pKFy1cXCY0WQOyR3DZC115D3Sp3sH2jiuFs8ksm0889Y3kbnmX2O-24UsuUy0T36Iu4C86Q9XQE"
        assert(signature).isEqualTo(expectedSignature)
        assert(signature.decodeBase64().size).isEqualTo(65)
    }
}