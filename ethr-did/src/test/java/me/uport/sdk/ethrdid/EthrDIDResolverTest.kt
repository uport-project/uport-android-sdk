package me.uport.sdk.ethrdid

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import pm.gnosis.utils.hexToByteArray
import java.math.BigInteger

class EthrDIDResolverTest {

    @Test
    fun lastChanged() {
        runBlocking {
            val imaginaryAddress = "0x1234"
            val lastChanged = EthrDIDResolver().lastChanged(imaginaryAddress)
            assertEquals(BigInteger.ZERO, BigInteger(1, lastChanged.hexToByteArray()))
        }
    }
}