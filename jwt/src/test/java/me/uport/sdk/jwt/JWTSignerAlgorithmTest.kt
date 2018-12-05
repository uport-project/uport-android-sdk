package me.uport.sdk.jwt

import com.uport.sdk.signer.KPSigner
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K_R
import org.junit.Assert.assertEquals
import org.junit.Test

class JWTSignerAlgorithmTest {

    @Test
    fun `can sign using non recoverable key algorithm`() = runBlocking {

        val signer = KPSigner("65fc670d9351cb87d1f56702fb56a7832ae2aab3427be944ab8c9f2a0ab87960")

        val expectedSignature = "a82BRGGDrxk8pKFy1cXCY0WQOyR3DZC115D3Sp3sH2jiuFs8ksm0889Y3kbnmX2O-24UsuUy0T36Iu4C86Q9XQ"
        val signature = JWTSignerAlgorithm(ES256K).sign("Hello, world!", signer)

        assertEquals(expectedSignature, signature)
        assertEquals(64, signature.decodeBase64().size)

    }

    @Test
    fun `can sign using recoverable key algorithm`() = runBlocking {

        val signer = KPSigner("65fc670d9351cb87d1f56702fb56a7832ae2aab3427be944ab8c9f2a0ab87960")

        val expectedSignature = "a82BRGGDrxk8pKFy1cXCY0WQOyR3DZC115D3Sp3sH2jiuFs8ksm0889Y3kbnmX2O-24UsuUy0T36Iu4C86Q9XQE"
        val signature = JWTSignerAlgorithm(ES256K_R).sign("Hello, world!", signer)

        assertEquals(expectedSignature, signature)
        assertEquals(65, signature.decodeBase64().size)

    }

    @Test
    fun `can sign using vector from did-jwt`() = runBlocking {

        val signer = KPSigner("278a5de700e29faae8e40e366ec5012b5ec63d36ec77e8a2417154cc1d25383f")

        //the signature data from https://github.com/uport-project/did-jwt/blob/develop/src/__tests__/__snapshots__/SimpleSigner-test.js.snap
        // JOSE encoded with recovery param
        val expectedSignature = "jsvdLwqr-O206hkegoq6pbo7LJjCaflEKHCvfohBP9XJ4C7mG2TPL9YjyKEpYSXqqkUrfRoCxQecHR11Uh7POwA"

        val signature = JWTSignerAlgorithm(ES256K_R).sign("thequickbrownfoxjumpedoverthelazyprogrammer", signer)

        assertEquals(expectedSignature, signature)
    }
}