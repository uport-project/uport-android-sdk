package me.uport.sdk.httpsdid

import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.experimental.runBlocking
import me.uport.sdk.universaldid.AuthenticationEntry
import me.uport.sdk.universaldid.DelegateType
import me.uport.sdk.universaldid.PublicKeyEntry

import com.nhaarman.mockitokotlin2.*
import org.mockito.ArgumentMatchers.anyString

typealias CBI = (Exception?, Any?) -> Unit

class HttpsDIDResolverTest {

    private val publicKeyList = listOf(PublicKeyEntry(id="did:https:example.com", type=DelegateType.Secp256k1VerificationKey2018,
            owner="did:https:example.com",
            ethereumAddress="0x3c7d65d6daf5df62378874d35fa3626100af9d85",
            publicKeyHex=null,
            publicKeyBase64=null,
            publicKeyBase58=null,
            value=null))

    private val authList = listOf(AuthenticationEntry(type= DelegateType.Secp256k1SignatureAuthentication2018, publicKey="did:https:example.com#owner"))
    private val exampleDidDoc = HttpsIdentityDocument(context="https://w3id.org/did/v1",
            id="did:https:example.com",
            publicKey=publicKeyList, authentication=authList, service= emptyList())

    private val https = mock<HttpsDIDResolver>()

    @Test
    fun can_resolve_valid_dids() {
        listOf(
                "did:https:example.com",
                "did:https:example.ngrok.com#owner"
        ).forEach {
            assertTrue("fails to resolve resolve '$it'", HttpsDIDResolver().canResolve(it))
        }

    }

    @Test
    fun fails_on_invalid_dids() {
        listOf(
                "did:something:example.com", //different method
                "example.com"
        ).forEach {
            assertFalse("claims to be able to resolve '$it", HttpsDIDResolver().canResolve(it))
        }


    }

    @Test
    fun `resolves document`() = runBlocking {
        //Stubbing network call to domain well-known path, providing did doc JSON
        whenever(https.getProfileDocument(anyString(), any())).then {
            (it.arguments.last() as CBI).invoke(null, exampleDidDoc.toJson())
        }

        //class property method is null for mocked HttpsDIDResolver
        whenever(https.canResolve(anyString())).thenReturn(true)

        //Use real implementation of resolve
        whenever(https.resolve(anyString())).thenCallRealMethod()

        val response = https.resolve("did:https:example.com")
        assertEquals(exampleDidDoc, response)
    }

}