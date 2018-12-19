package me.uport.sdk.transport

import android.content.Intent
import android.net.Uri
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IntentParserTest {

    @Test
    fun `extracts token from simple intent`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("myapp:my-dapp.com#access_token=header.payload.signature"))
        val token = ResponseParser.extractTokenFromIntent(intent)
        Assert.assertEquals("header.payload.signature", token)
    }

    @Test
    fun `rejects wrong action`() {
        val intent = Intent("view my intent", Uri.parse("myapp:my-dapp.com#access_token=header.payload.signature"))
        val token = ResponseParser.extractTokenFromIntent(intent)
        Assert.assertNull(token)
    }

    @Test
    fun `rejects null intent`() {
        val token = ResponseParser.extractTokenFromIntent(null)
        Assert.assertNull(token)
    }

    @Test
    fun `rejects malformed uri`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("access_token=header.payload.signature"))
        val token = ResponseParser.extractTokenFromIntent(intent)
        Assert.assertNull(token)
    }

}