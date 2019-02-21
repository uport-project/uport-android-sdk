package me.uport.sdk.demoapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import me.uport.sdk.ethrdid.EthrDIDDocument
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDDocument
import me.uport.sdk.uportdid.UportDIDResolver
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test

class DIDResolverTest {

    @get:Rule
    val activityRule = ActivityTestRule(DIDResolverActivity::class.java)

    @Test
    fun can_resolve_dids_to_documents() {
        //given
        val rpc = mockk<JsonRPC>()
        val uportResolver = spyk(UportDIDResolver(rpc)) {
            coEvery { resolve(eq("did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC")) } returns UportDIDDocument.fromJson("""{"id":"did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC","publicKey":[{"id":"did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC#keys-1","type":"Secp256k1VerificationKey2018","owner":"did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC","publicKeyHex":"04e8989d1826cd6258906cfaa71126e2db675eaef47ddeb9310ee10db69b339ab960649e1934dc1e1eac1a193a94bd7dc5542befc5f7339845265ea839b9cbe56f"},{"id":"did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC#keys-2","type":"Curve25519EncryptionPublicKey","owner":"did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC","publicKeyBase64":"k8q5G4YoIMP7zvqMC9q84i7xUBins6dXGt8g5H007F0="}],"authentication":[{"type":"Secp256k1SignatureAuthentication2018","publicKey":"did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC#keys-1"}],"service":[],"@context":"https://w3id.org/did/v1","uportProfile":{"@type":"Person"}}""")
        }
        val ethrResolver = spyk(EthrDIDResolver(rpc)) {
            coEvery { resolve(eq("did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74")) } returns EthrDIDDocument.fromJson("""{"id":"did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74","publicKey":[{"id":"did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74#owner","type":"Secp256k1VerificationKey2018","owner":"did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74","ethereumAddress":"0xf3beac30c498d9e26865f34fcaa57dbb935b0d74","publicKeyHex":null,"publicKeyBase64":null,"publicKeyBase58":null,"value":null}],"authentication":[{"type":"Secp256k1SignatureAuthentication2018","publicKey":"did:ethr:0xf3beac30c498d9e26865f34fcaa57dbb935b0d74#owner"}],"service":[],"@context":"https://w3id.org/did/v1"}""")
        }
        UniversalDID.registerResolver(uportResolver)
        UniversalDID.registerResolver(ethrResolver)

        //when
        onView(withId(R.id.resolve_btn)).perform(click())

        //then
        onView(withId(R.id.ethr_did_doc)).check(matches(withText(containsString("Ethr DID Document"))))
        onView(withId(R.id.uport_did_doc)).check(matches(withText(containsString("Uport DID Document"))))
    }
}