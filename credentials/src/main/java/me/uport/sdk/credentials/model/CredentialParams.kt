package me.uport.sdk.credentials.model

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.uport.sdk.jwt.model.ArbitraryMapSerializer

/**
 * Encapsulates the core fields of a w3c
 * [verifiable credential](https://w3c.github.io/vc-data-model/#credentials)
 */
@Serializable
data class CredentialParams(

    /**
     * The list of types that make up this presentation.
     * By default a `VerifiableCredential` type is used if not explicitly set.
     */
    @Required
    @SerialName("type")
    val type: List<String> = emptyList(),

    /**
     * The actual payload of the credential.
     * This is a dictionary of arbitrary data.
     */
    @Required
    @SerialName("credentialSubject")
    @Serializable(with = ArbitraryMapSerializer::class)
    val credentialSubject: Map<String, @ContextualSerialization Any>,

    /**
     * The LD context hooks.
     */
    @Required
    @SerialName("@context")
    val context: List<String> = emptyList()

)