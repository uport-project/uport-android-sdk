package me.uport.sdk.credentials.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.uport.sdk.jwt.model.ArbitraryMapSerializer

@Serializable
data class VerifiableCredentialParams(

    val type: List<String> = listOf("VerifiableCredential"),

    @Serializable(with = ArbitraryMapSerializer::class)
    val credentialSubject: Map<String, Any>,

    @SerialName("@context")
    val context: List<String> = emptyList()

)