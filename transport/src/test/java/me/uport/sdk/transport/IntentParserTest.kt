package me.uport.sdk.transport

import android.content.Intent
import android.net.Uri
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IntentParserTest {

    @Test
    fun `extracts token from simple intent`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("myapp:my-dapp.com#access_token=header.payload.signature"))
        val response = ResponseParser.extractTokenFromIntent(intent)
        assertThat((response as JWTUriResponse).token).isEqualTo("header.payload.signature")
    }

    @Test
    fun `reject null intent`() {
        assertThat {
            ResponseParser.extractTokenFromIntent(null)
        }.thrownError {
            isInstanceOf(IllegalArgumentException::class)
        }
    }

    @Test
    fun `reject null data`() {
        assertThat {
            ResponseParser.extractTokenFromIntent(Intent(Intent.ACTION_VIEW, null))
        }.thrownError {
            isInstanceOf(IllegalArgumentException::class)
        }
    }


    @Test
    fun `rejects wrong action`() {
        val intent = Intent("view my intent", Uri.parse("myapp:my-dapp.com#access_token=header.payload.signature"))
        assertThat {
            ResponseParser.extractTokenFromIntent(intent)
        }.thrownError {
            isInstanceOf(IllegalArgumentException::class)
        }
    }

    @Test
    fun `rejects malformed uri`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("access_token=header.payload.signature"))
        assertThat {
            ResponseParser.extractTokenFromIntent(intent)
        }.thrownError {
            isInstanceOf(IllegalArgumentException::class)
        }
    }

    @Test
    fun `rejects with correct error message`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("myapp:my-dapp.com#error=access_denied"))
        val response = ResponseParser.extractTokenFromIntent(intent)
        assertThat((response as ErrorUriResponse).message).isEqualTo("access_denied")
    }

}