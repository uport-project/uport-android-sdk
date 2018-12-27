package me.uport.sdk.transport

import android.content.Intent
import android.net.Uri
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IntentParserTest {

    @get:Rule
    val expectedExceptionRule: ExpectedException = ExpectedException.none()

    @Test
    fun `extracts token from simple intent`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("myapp:my-dapp.com#access_token=header.payload.signature"))
        val token = ResponseParser.extractTokenFromIntent(intent)
        Assert.assertEquals("header.payload.signature", token)
    }

    @Test
    fun `reject null intent`() {
        expectedExceptionRule.expect(IllegalArgumentException::class.java)
        val token = ResponseParser.extractTokenFromIntent(null)
        Assert.assertNull(token)
    }

    @Test
    fun `reject null data`() {
        expectedExceptionRule.expect(IllegalArgumentException::class.java)
        val token = ResponseParser.extractTokenFromIntent(Intent(Intent.ACTION_VIEW, null))
        Assert.assertNull(token)
    }


    @Test
    fun `rejects wrong action`() {
        expectedExceptionRule.expect(IllegalArgumentException::class.java)
        val intent = Intent("view my intent", Uri.parse("myapp:my-dapp.com#access_token=header.payload.signature"))
        val token = ResponseParser.extractTokenFromIntent(intent)
        Assert.assertNull(token)
    }

    @Test
    fun `rejects malformed uri`() {
        expectedExceptionRule.expect(IllegalArgumentException::class.java)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("access_token=header.payload.signature"))
        val token = ResponseParser.extractTokenFromIntent(intent)
        Assert.assertNull(token)
    }

    @Test
    fun `rejects with correct error message`() {
        expectedExceptionRule.expect(RuntimeException::class.java)
        expectedExceptionRule.expectMessage("access_denied")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("myapp:my-dapp.com#error=access_denied"))
        val token = ResponseParser.extractTokenFromIntent(intent)
        Assert.assertNull(token)
    }

}