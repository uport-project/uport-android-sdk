package me.uport.sdk.credentials.model

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.uport.sdk.jwt.model.ArbitraryMapSerializer

@Serializable
data class CredentialParams(

    @Required
    @SerialName("type")
    val type: List<String> = listOf("VerifiableCredential"),

    @Required
    @SerialName("credentialSubject")
    @Serializable(with = ArbitraryMapSerializer::class)
    val credentialSubject: Map<String, Any>,

    @Required
    @SerialName("@context")
    val context: List<String> = listOf("https://www.w3.org/2018/credentials/v1")

)