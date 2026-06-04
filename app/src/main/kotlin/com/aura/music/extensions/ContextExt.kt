/*
 * AuraMusic - by Nikhil
 * Nikhil
 * Licensed Under GPL-3.0
 */



package com.aura.music.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.aura.music.constants.InnerTubeCookieKey
import com.aura.music.constants.YtmSyncKey
import com.aura.music.utils.dataStore
import com.aura.music.utils.get
import com.aura.music.innertube.utils.parseCookieString

fun Context.isSyncEnabled(): Boolean {
    return dataStore.get(YtmSyncKey, true) && isUserLoggedIn()
}

fun Context.isUserLoggedIn(): Boolean {
    val cookie = dataStore[InnerTubeCookieKey] ?: ""
    return "SAPISID" in parseCookieString(cookie) && isInternetConnected()
}

fun Context.isInternetConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
}
