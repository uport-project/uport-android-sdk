package me.uport.sdk.credentials

import me.uport.sdk.jwt.model.JwtPayload

data class UportProfile(val did: String,
                        val network: String,
                        val boxPub: String,
                        val name: String?,
                        val email: String?,
                        val extras: Map<String, Any>,
                        val invalid: List<String>,
                        val valid: List<JwtPayload>)