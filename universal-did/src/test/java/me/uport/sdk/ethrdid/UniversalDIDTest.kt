package me.uport.sdk.ethrdid

import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.UniversalDID
import org.junit.Assert.assertEquals
import org.junit.Test

class UniversalDIDTest {

    @Test
    fun `blank resolves to blank`() {
        UniversalDID.clearResolvers()

        assertEquals(DIDDocument.blank, UniversalDID.resolve(""))
    }

}