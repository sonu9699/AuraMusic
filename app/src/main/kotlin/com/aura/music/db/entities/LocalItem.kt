/*
 * AuraMusic - by Nikhil
 * Nikhil
 * Licensed Under GPL-3.0
 */



package com.aura.music.db.entities

sealed class LocalItem {
    abstract val id: String
    abstract val title: String
    abstract val thumbnailUrl: String?
}
