package me.uport.sdk.httpsdid

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import me.uport.sdk.universaldid.AuthenticationEntry
import me.uport.sdk.universaldid.DelegateType
import me.uport.sdk.universaldid.PublicKeyEntry
import org.junit.Test

class HttpsDIDResolverTest {

    private val publicKeyList = listOf(PublicKeyEntry(id = "did:https:example.com", type = DelegateType.Secp256k1VerificationKey2018,
            owner = "did:https:example.com",
            ethereumAddress = "0x3c7d65d6daf5df62378874d35fa3626100af9d85",
            publicKeyHex = null,
            publicKeyBase64 = null,
            publicKeyBase58 = null,
            value = null))

    private val authList = listOf(AuthenticationEntry(type = DelegateType.Secp256k1SignatureAuthentication2018, publicKey = "did:https:example.com#owner"))
    private val exampleDidDoc = HttpsIdentityDocument(context = "https://w3id.org/did/v1",
            id = "did:https:example.com",
            publicKey = publicKeyList, authentication = authList, service = emptyList())


    @Test
    fun can_resolve_valid_dids() {
        listOf(
                "did:https:example.com",
                "did:https:example.ngrok.com#owner"
        ).forEach {
            assert(HttpsDIDResolver().canResolve(it))
        }

    }

    @Test
    fun fails_on_invalid_dids() {
        listOf(
                "did:something:example.com", //different method
                "example.com"
        ).forEach {
            assert(HttpsDIDResolver().canResolve(it)).isFalse()
        }


    }

    @Test
    fun `resolves document`() = runBlocking {

        val https = spyk<HttpsDIDResolver>()

        //Stubbing network call to domain well-known path, providing did doc JSON
        every { https.getProfileDocument(any(), invoke(null, exampleDidDoc.toJson())) } just runs

        val response = https.resolve("did:https:example.com")
        assert(response).isEqualTo(exampleDidDoc)
    }

}