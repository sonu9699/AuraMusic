/*
 * AuraMusic - by Nikhil
 * Nikhil
 * Licensed Under GPL-3.0
 */



package com.aura.music.models

import com.aura.music.innertube.models.YTItem
import com.aura.music.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
