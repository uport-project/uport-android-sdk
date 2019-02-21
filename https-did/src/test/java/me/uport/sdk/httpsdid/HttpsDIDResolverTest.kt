package me.uport.sdk.httpsdid

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import me.uport.sdk.core.HttpClient
import me.uport.sdk.testhelpers.coAssert
import me.uport.sdk.testhelpers.isInstanceOf
import me.uport.sdk.universaldid.AuthenticationEntry
import me.uport.sdk.universaldid.DelegateType
import me.uport.sdk.universaldid.DidResolverError
import me.uport.sdk.universaldid.PublicKeyEntry
import org.junit.Test
import java.io.IOException

class HttpsDIDResolverTest {

    private val exampleDidDoc = HttpsDIDDocument(context = "https://w3id.org/did/v1",
            id = "did:https:example.com",
            publicKey = listOf(
                    PublicKeyEntry(id = "did:https:example.com",
                            type = DelegateType.Secp256k1VerificationKey2018,
                            owner = "did:https:example.com",
                            ethereumAddress = "0x3c7d65d6daf5df62378874d35fa3626100af9d85"
                    )
            ),
            authentication = listOf(
                    AuthenticationEntry(type = DelegateType.Secp256k1SignatureAuthentication2018,
                            publicKey = "did:https:example.com#owner")
            ),
            service = emptyList()
    )


    @Test
    fun `can resolve valid dids`() {
        listOf(
                "did:https:example.com",
                "did:https:example.ngrok.com#owner"
        ).forEach {
            assert(HttpsDIDResolver().canResolve(it))
        }

    }

    @Test
    fun `fails on invalid dids`() {
        listOf(
                "did:something:example.com", //different method
                "example.com"
        ).forEach {
            assert(HttpsDIDResolver().canResolve(it)).isFalse()
        }
    }

    @Test
    fun `fails when the endpoint doesn't provide a DID document`() = runBlocking {
        val http = mockk<HttpClient>()
        val tested = HttpsDIDResolver(http)
        coEvery { http.urlGet(any()) } returns ""

        coAssert {
            tested.resolve("did:https:example.com")
        }.thrownError {
            isInstanceOf(listOf(IllegalArgumentException::class, IOException::class, SerializationException::class, DidResolverError::class))
        }
    }

    @Test
    fun `resolves document`() = runBlocking {

        val http = mockk<HttpClient>()
        val tested = HttpsDIDResolver(http)

        coEvery { http.urlGet(any()) } returns exampleDidDoc.toJson()

        val response = tested.resolve("did:https:example.com")
        assert(response).isEqualTo(exampleDidDoc)
    }

}


