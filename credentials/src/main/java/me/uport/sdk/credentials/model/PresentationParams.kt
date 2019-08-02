package me.uport.sdk.credentials.model

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Encapsulates the core fields of a w3c
 * [verifiable presentation](https://w3c.github.io/vc-data-model/#presentations)
 */
@Serializable
data class PresentationParams(

    /**
     * the list of credentials (in JWT representation) that make up this presentation.
     */
    @Required
    @SerialName("verifiableCredential")
    val verifiableCredentials: List<String>,

    /**
     * The list of types that make up this presentation.
     * By default a `VerifiablePresentation` type is used if not explicitly set.
     */
    @Required
    @SerialName("type")
    val type: List<String> = emptyList(),

    /**
     * The LD context hooks.
     */
    @Required
    @SerialName("@context")
    val context: List<String> = emptyList()
)