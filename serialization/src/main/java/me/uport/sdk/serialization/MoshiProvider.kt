package me.uport.sdk.serialization

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MoshiProvider {

    companion object {
        val default: Moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
    }
}