@file:Suppress("DEPRECATION")

package me.uport.sdk.uportdid

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import me.uport.mnid.Account
import me.uport.sdk.core.HttpClient
import me.uport.sdk.core.Networks
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.universaldid.DelegateType
import org.junit.Test
import org.walleth.khex.clean0xPrefix

class UportDIDResolverTest {

    @Test
    fun `can encode a uPort registry Get method call`() {
        val expectedEncoding = "0x447885f075506f727450726f66696c654950465331323230000000000000000000000000000000000000000000000000f12c30cd32b4a027710c150ae742f50db0749213000000000000000000000000f12c30cd32b4a027710c150ae742f50db0749213"
        val acc = Account.from(network = "0x04", address = "0xf12c30cd32b4a027710c150ae742f50db0749213")
        val rpc = mockk<JsonRPC>()
        val encoding = UportDIDResolver(rpc).encodeRegistryGetCall("uPortProfileIPFS1220", acc, acc)

        assert(encoding).isEqualTo(expectedEncoding)
    }

    @Test
    fun `calls registry with correct payload`() = runBlocking {

        val encodedCallSlot = slot<String>()

        val rpc = mockk<JsonRPC>() {
            coEvery { ethCall(any(), capture(encodedCallSlot)) } returns "0x807a7cb8b670125774d70cf94d35e2355bb18bb51cf604f376c9996057f92fbf"
        }

        val docAddressHex = UportDIDResolver(rpc).getIpfsHash("2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC")

        assert(encodedCallSlot.captured).isEqualTo("0x447885f075506f727450726f66696c654950465331323230000000000000000000000000000000000000000000000000f12c30cd32b4a027710c150ae742f50db0749213000000000000000000000000f12c30cd32b4a027710c150ae742f50db0749213")
        assert(docAddressHex).isEqualTo("QmWzBDtv8m21ph1aM57yVDWxdG7LdQd3rNf5xrRiiV2D2E")
    }

    @Test
    fun `can convert legacy UportIdentityDocument to DIDDocument`() {
        val publicKeyHex = "0x04e8989d1826cd6258906cfaa71126e2db675eaef47ddeb9310ee10db69b339ab960649e1934dc1e1eac1a193a94bd7dc5542befc5f7339845265ea839b9cbe56f"
        val publicEncKey = "k8q5G4YoIMP7zvqMC9q84i7xUBins6dXGt8g5H007F0="
        val legacyDDO = UportIdentityDocument(
                context = "http://schema.org",
                type = "Person",
                publicKey = publicKeyHex,
                publicEncKey = publicEncKey,
                description = null,
                image = ProfilePicture(),
                name = null
        )

        val convertedDDO = legacyDDO.convertToDIDDocument("2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC")

        val expectedOwner = "did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC"
        assert(convertedDDO.id).isEqualTo(expectedOwner)

        val verificationPkMatch = convertedDDO.publicKey.find { it ->
            it.id.startsWith(expectedOwner) &&
                    it.owner == expectedOwner &&
                    it.type == DelegateType.Secp256k1VerificationKey2018 &&
                    it.publicKeyHex == publicKeyHex.clean0xPrefix()
        }
        assert(verificationPkMatch).isNotNull()

        val encPkMatch = convertedDDO.publicKey.find { it ->
            it.id.startsWith(expectedOwner) &&
                    it.owner == expectedOwner &&
                    it.type == DelegateType.Curve25519EncryptionPublicKey &&
                    it.publicKeyBase64 == publicEncKey
        }
        assert(encPkMatch).isNotNull()
    }

    @Test
    fun `can get profile document for mnid`() = runBlocking {
        val http = mockk<HttpClient>() {
            //language=json
            coEvery { urlGet(any()) } returns """{"@context":"http://schema.org","@type":"Person","publicKey":"0x04e8989d1826cd6258906cfaa71126e2db675eaef47ddeb9310ee10db69b339ab960649e1934dc1e1eac1a193a94bd7dc5542befc5f7339845265ea839b9cbe56f","publicEncKey":"k8q5G4YoIMP7zvqMC9q84i7xUBins6dXGt8g5H007F0="}"""
        }
        val rpc = spyk(JsonRPC(Networks.rinkeby.rpcUrl, http)) {
            coEvery { ethCall(any(), any()) } returns "0x807a7cb8b670125774d70cf94d35e2355bb18bb51cf604f376c9996057f92fbf"
        }

        val expectedDDO = UportIdentityDocument(
                context = "http://schema.org",
                type = "Person",
                publicKey = "0x04e8989d1826cd6258906cfaa71126e2db675eaef47ddeb9310ee10db69b339ab960649e1934dc1e1eac1a193a94bd7dc5542befc5f7339845265ea839b9cbe56f",
                publicEncKey = "k8q5G4YoIMP7zvqMC9q84i7xUBins6dXGt8g5H007F0=",
                description = null,
                image = null,
                name = null
        )

        val ddo = UportDIDResolver(rpc).getProfileDocumentFor(mnid = "2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC")

        assert(ddo).isEqualTo(expectedDDO)
    }


    @Test
    fun can_resolve_valid_dids() {
        val tested = UportDIDResolver(JsonRPC(Networks.rinkeby.rpcUrl))
        listOf(
                "2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX",
                "5A8bRWU3F7j3REx3vkJWxdjQPp4tqmxFPmab1Tr",
                "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX",
                "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX#owner"
        ).forEach {
            assert(tested.canResolve(it)).isTrue()
        }

    }

    @Test
    fun fails_on_invalid_dids() {
        val tested = UportDIDResolver(JsonRPC(Networks.rinkeby.rpcUrl))
        listOf(
                "did:something:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX", //different method
                "QmXuNqXmrkxs4WhTDC2GCnXEep4LUD87bu97LQMn1rkxmQ", //not mnid
                "1GbVUSW5WJmRCpaCJ4hanUny77oDaWW4to", //not mnid
                "0x00521965e7bd230323c423d96c657db5b79d099f", //not mnid
                "did:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX", //missing method
                "did:uport:", //missing mnid
                "did:uport" //missing mnid and colon
        ).forEach {
            assert(tested.canResolve(it)).isFalse()
        }


    }


}