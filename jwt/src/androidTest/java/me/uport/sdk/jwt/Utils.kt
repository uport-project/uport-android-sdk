package me.uport.sdk.jwt

import android.content.Context
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import com.uport.sdk.signer.importHDSeed
import kotlinx.coroutines.runBlocking

fun ensureSeedIsImported(appContext: Context, phrase: String) = runBlocking {
    //ensure seed is imported
    UportHDSigner().importHDSeed(appContext, KeyProtection.Level.SIMPLE, phrase)
}