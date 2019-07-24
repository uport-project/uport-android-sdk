package me.uport.sdk.credentials.model

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresentationParams(

    @Required
    @SerialName("verifiableCredential")
    val verifiableCredentials: List<String>,

    @Required
    @SerialName("type")
    val type: List<String> = listOf("VerifiablePresentation"),

    @Required
    @SerialName("@context")
    val context: List<String> = listOf("https://www.w3.org/2018/credentials/v1"),

    val id: String? = null
)