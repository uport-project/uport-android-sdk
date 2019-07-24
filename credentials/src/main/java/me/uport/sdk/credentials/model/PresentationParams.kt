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
    val type: List<String> = emptyList(),

    @Required
    @SerialName("@context")
    val context: List<String> = emptyList(),

    val id: String? = null
)