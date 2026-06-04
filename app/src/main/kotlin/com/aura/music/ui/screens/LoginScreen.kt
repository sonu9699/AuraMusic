/*
 * AuraMusic - by Nikhil
 * Nikhil
 * Licensed Under GPL-3.0
 */



package com.aura.music.ui.screens

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.aura.music.LocalPlayerAwareWindowInsets
import com.aura.music.R
import com.aura.music.constants.AccountChannelHandleKey
import com.aura.music.constants.AccountEmailKey
import com.aura.music.constants.AccountNameKey
import com.aura.music.constants.DataSyncIdKey
import com.aura.music.constants.InnerTubeCookieKey
import com.aura.music.constants.PoTokenKey
import com.aura.music.constants.VisitorDataKey
import com.aura.music.ui.component.IconButton
import com.aura.music.ui.utils.backToMain
import com.aura.music.utils.rememberPreference
import com.aura.music.utils.reportException
import com.aura.music.innertube.YouTube
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun LoginScreen(
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    var visitorData by rememberPreference(VisitorDataKey, "")
    var dataSyncId by rememberPreference(DataSyncIdKey, "")
    var innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    var poToken by rememberPreference(PoTokenKey, "")
    var accountName by rememberPreference(AccountNameKey, "")
    var accountEmail by rememberPreference(AccountEmailKey, "")
    var accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")

    var webView: WebView? = null

    AndroidView(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        loadUrl("javascript:Android.onRetrieveVisitorData(window.yt.config_.VISITOR_DATA)")
                        loadUrl("javascript:Android.onRetrieveDataSyncId(window.yt.config_.DATASYNC_ID)")
                        loadUrl("javascript:void((function(){try{var c=window.ytcfg;if(c&&c.get){var t=c.get('PO_TOKEN');if(t){Android.onRetrievePoToken(t);return}}var s=document.querySelectorAll('script');for(var i=0;i<s.length;i++){var m=s[i].textContent.match(/\"PO_TOKEN\":\"([^\"]+)\"/);if(m){Android.onRetrievePoToken(m[1]);return}}}catch(e){}})())")

                        if (url?.startsWith("https://music.youtube.com") == true) {
                            innerTubeCookie = CookieManager.getInstance().getCookie(url)
                            coroutineScope.launch {
                                YouTube.accountInfo().onSuccess {
                                    accountName = it.name
                                    accountEmail = it.email.orEmpty()
                                    accountChannelHandle = it.channelHandle.orEmpty()
                                    if (navController.currentBackStackEntry?.destination?.route == "login") {
                                        navController.navigateUp()
                                    }
                                }.onFailure {
                                    reportException(it)
                                    if (navController.currentBackStackEntry?.destination?.route == "login") {
                                        navController.navigateUp()
                                    }
                                }
                            }
                        }
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onRetrieveVisitorData(newVisitorData: String?) {
                        if (newVisitorData != null) {
                            visitorData = newVisitorData
                        }
                    }
                    @JavascriptInterface
                    fun onRetrieveDataSyncId(newDataSyncId: String?) {
                        if (newDataSyncId != null) {
                            dataSyncId = newDataSyncId.substringBefore("||")
                        }
                    }
                    @JavascriptInterface
                    fun onRetrievePoToken(newPoToken: String?) {
                        if (!newPoToken.isNullOrBlank()) {
                            poToken = newPoToken
                        }
                    }
                }, "Android")
                webView = this
                loadUrl("https://accounts.google.com/ServiceLogin?continue=https%3A%2F%2Fmusic.youtube.com")
            }
        }
    )

    TopAppBar(
        title = { Text(stringResource(R.string.login)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        }
    )

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }
}
