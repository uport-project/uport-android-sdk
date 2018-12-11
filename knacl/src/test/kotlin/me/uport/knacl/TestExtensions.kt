package me.uport.knacl

import java.util.*

internal fun String.decodeBase64() = Base64.getDecoder().decode(this)!!