/*
 * AuraMusic - by Nikhil
 * Nikhil
 * Licensed Under GPL-3.0
 */



package com.aura.music.lyrics

import android.content.Context
import com.aura.music.constants.EnableSimpMusicLyricsKey
import com.aura.music.simpmusic.SimpMusicLyrics
import com.aura.music.utils.dataStore
import com.aura.music.utils.get

object SimpMusicLyricsProvider : LyricsProvider {
    override val name: String = "SimpMusic"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableSimpMusicLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
    ): Result<String> = SimpMusicLyrics.getLyrics(videoId = id, duration = duration)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        SimpMusicLyrics.getAllLyrics(videoId = id, duration = duration, callback = callback)
    }
}

