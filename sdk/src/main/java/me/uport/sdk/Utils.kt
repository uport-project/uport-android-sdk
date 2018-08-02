package me.uport.sdk

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Moshi instance used to (de)serialize objects in requests/responses
 */
internal val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()