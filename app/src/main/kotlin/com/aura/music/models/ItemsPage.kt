/*
 * AuraMusic - by Nikhil
 * Nikhil
 * Licensed Under GPL-3.0
 */



package com.aura.music.models

import com.aura.music.innertube.models.YTItem

data class ItemsPage(
    val items: List<YTItem>,
    val continuation: String?,
)
