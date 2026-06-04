/*
 * AuraMusic - by Nikhil
 * Nikhil
 * Licensed Under GPL-3.0
 */

package com.aura.music.together

import kotlinx.serialization.json.Json

object TogetherJson {
    val json: Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
            classDiscriminator = "type"
        }
}
