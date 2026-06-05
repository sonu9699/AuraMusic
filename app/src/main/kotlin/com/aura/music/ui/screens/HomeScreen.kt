/*
 * AuraMusic - by Antigravity
 * Licensed Under GPL-3.0
 */

package com.aura.music.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.aura.music.LocalDatabase
import com.aura.music.LocalPlayerConnection
import com.aura.music.R
import com.aura.music.constants.CustomThemeColorKey
import com.aura.music.db.entities.Song
import com.aura.music.db.entities.LocalItem
import com.aura.music.db.entities.Album
import com.aura.music.db.entities.Artist
import com.aura.music.db.entities.Playlist
import com.aura.music.innertube.models.PlaylistItem
import com.aura.music.innertube.models.SongItem
import com.aura.music.innertube.toHighResThumbnail
import com.aura.music.models.toMediaMetadata
import com.aura.music.playback.queues.YouTubeQueue
import com.aura.music.ui.component.LocalMenuState
import com.aura.music.ui.menu.SongMenu
import com.aura.music.utils.rememberPreference
import com.aura.music.viewmodels.HomeViewModel
import com.aura.music.extensions.togglePlayPause
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val homePage by viewModel.homePage.collectAsState()
    val forYouSuggestions by viewModel.forYouSuggestions.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()

    // 1. Theme Configuration
    val (customThemeColorValue) = rememberPreference(CustomThemeColorKey, defaultValue = "volt_neon")
    val isVolt = customThemeColorValue == "volt_neon"
    val isCrimson = customThemeColorValue == "crimson"

    val accent = when {
        isVolt -> Color(0xFFD2F535)
        isCrimson -> Color(0xFFE8002D)
        else -> MaterialTheme.colorScheme.primary
    }

    val background = when {
        isVolt -> Color(0xFF09090C)
        isCrimson -> Color(0xFF0D0D0D)
        else -> MaterialTheme.colorScheme.background
    }

    val surface = when {
        isVolt -> Color(0xDA121216)
        isCrimson -> Color(0xDA151515)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val border = when {
        isVolt -> Color(0x14FFFFFF)
        isCrimson -> Color(0x1A4A151B)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    }

    // Pagination logic
    LaunchedEffect(Unit) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val len = lazyListState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && lastVisibleIndex >= len - 3) {
                    viewModel.loadMoreYouTubeItems(homePage?.continuation)
                }
            }
    }

    if (selectedChip != null) {
        BackHandler {
            viewModel.toggleChip(selectedChip)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .pullToRefresh(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh
            )
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header
            item {
                HomeHeader(accent = accent, isVolt = isVolt, navController = navController)
            }

            // Greetings
            item {
                HomeGreetings()
            }

            // Filter Chips
            item {
                HomeFilterChips(
                    chips = homePage?.chips.orEmpty().map { it to it.title },
                    selectedChip = selectedChip,
                    accent = accent,
                    isVolt = isVolt,
                    onChipSelect = { viewModel.toggleChip(it) }
                )
            }

            // Search Bar
            item {
                HomeSearchBar(accent = accent, navController = navController)
            }

            // Curated / Featured Banner Card
            item {
                HomeFeaturedCard(
                    accent = accent,
                    isVolt = isVolt,
                    isCrimson = isCrimson,
                    onPlayClick = {
                        quickPicks?.takeIf { it.isNotEmpty() }?.let { picks ->
                            playerConnection.playQueue(YouTubeQueue.radio(picks.first().toMediaMetadata()))
                        }
                    }
                )
            }

            // Recently Played Section
            val keepListeningItems = keepListening.orEmpty()
            if (keepListeningItems.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = "Recently Played", onSeeAll = { navController.navigate("history") })
                }
                item {
                    HomeHorizontalScroll(
                        items = keepListeningItems,
                        navController = navController,
                        playerConnection = playerConnection
                    )
                }
            }

            // Trending Now Section
            val trendingSongs = quickPicks.orEmpty()
            if (trendingSongs.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = "Trending Now", onSeeAll = { navController.navigate("explore") })
                }
                items(trendingSongs.take(5)) { song ->
                    HomeSongRow(
                        song = song,
                        accent = accent,
                        isActive = song.id == mediaMetadata?.id,
                        isPlaying = isPlaying,
                        playerConnection = playerConnection,
                        navController = navController,
                        menuState = menuState,
                        haptic = haptic
                    )
                }
            }

            // Made For You Section
            val suggestions = forYouSuggestions.orEmpty()
            if (suggestions.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = "Made For You", onSeeAll = { navController.navigate("library") })
                }
                item {
                    HomeMadeForYouGrid(
                        playlists = suggestions.take(4),
                        surface = surface,
                        border = border,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(accent: Color, isVolt: Boolean, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "AuraMusic",
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { navController.navigate("settings") },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.settings),
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(2.dp, accent, CircleShape)
                    .background(Color(0xFF14141A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun HomeGreetings() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Good Evening",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "What do you want to hear?",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color.White
            )
        )
    }
}

@Composable
fun HomeFilterChips(
    chips: List<Pair<com.aura.music.innertube.pages.HomePage.Chip, String>>,
    selectedChip: com.aura.music.innertube.pages.HomePage.Chip?,
    accent: Color,
    isVolt: Boolean,
    onChipSelect: (com.aura.music.innertube.pages.HomePage.Chip) -> Unit
) {
    if (chips.isEmpty()) return
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chips) { (chip, title) ->
            val isSelected = selectedChip == chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (isSelected) accent else Color.Transparent)
                    .border(1.dp, if (isSelected) accent else Color(0x22FFFFFF), RoundedCornerShape(99.dp))
                    .clickable { onChipSelect(chip) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        color = if (isSelected) {
                            if (isVolt) Color.Black else Color.White
                        } else Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun HomeSearchBar(accent: Color, navController: NavController) {
    var queryText by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121216))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = queryText,
                onValueChange = { queryText = it },
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                singleLine = true,
                cursorBrush = SolidColor(accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (queryText.isNotBlank()) {
                        navController.navigate("search/${URLEncoder.encode(queryText, "UTF-8")}")
                    }
                }),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (queryText.isEmpty()) {
                        Text("Search songs, artists...", color = Color.Gray, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun HomeFeaturedCard(accent: Color, isVolt: Boolean, isCrimson: Boolean, onPlayClick: () -> Unit) {
    val bgGradient = if (isCrimson) {
        Brush.verticalGradient(listOf(Color(0xFF1E080A), Color(0xFF0D0D0D)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF181124), Color(0xFF09090C)))
    }

    val borderColor = if (isCrimson) Color(0x40E8002D) else Color(0x408B5CF6)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgGradient)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isCrimson) {
                                listOf(Color(0xFFE8002D), Color(0xFF800015))
                            } else {
                                listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                            }
                        )
                    )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CURATED & TRENDING",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Discover weekly",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "The original slow instrumental best playlists.",
                    style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(accent)
                        .clickable { onPlayClick() }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.play),
                        contentDescription = "Play",
                        tint = if (isVolt) Color.Black else Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PLAY",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = if (isVolt) Color.Black else Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        )
        Text(
            text = "See all",
            modifier = Modifier.clickable { onSeeAll() },
            style = TextStyle(fontSize = 13.sp, color = Color.Gray)
        )
    }
}

@Composable
fun HomeHorizontalScroll(
    items: List<LocalItem>,
    navController: NavController,
    playerConnection: com.aura.music.playback.PlayerConnection
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            val title = when (item) {
                is Song -> item.song.title
                is Album -> item.album.title
                is Artist -> item.artist.name
                is Playlist -> item.playlist.name
            }
            val subtitle = when (item) {
                is Song -> item.artists.joinToString { it.name }
                is Album -> item.artists?.joinToString { it.name } ?: "Album"
                is Artist -> "Artist"
                is Playlist -> "Playlist"
            }
            val artUrl = when (item) {
                is Song -> item.song.thumbnailUrl
                is Album -> item.album.thumbnailUrl
                is Artist -> item.artist.thumbnailUrl
                is Playlist -> item.playlist.thumbnailUrl
            }
            val clickAction = {
                when (item) {
                    is Song -> playerConnection.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
                    is Album -> navController.navigate("album/${item.id}")
                    is Artist -> navController.navigate("artist/${item.id}")
                    is Playlist -> navController.navigate("playlist/${item.id}")
                }
            }

            Column(
                modifier = Modifier
                    .width(80.dp)
                    .clickable { clickAction() }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.ic_music_placeholder),
                    error = painterResource(R.drawable.ic_music_placeholder),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = TextStyle(fontSize = 11.sp, color = Color.Gray),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeSongRow(
    song: Song,
    accent: Color,
    isActive: Boolean,
    isPlaying: Boolean,
    playerConnection: com.aura.music.playback.PlayerConnection,
    navController: NavController,
    menuState: com.aura.music.ui.component.MenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isActive) {
                        playerConnection.player.togglePlayPause()
                    } else {
                        playerConnection.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                        SongMenu(
                            originalSong = song,
                            navController = navController,
                            onDismiss = menuState::dismiss
                        )
                    }
                }
            )
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF14141A))
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.song.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_music_placeholder),
                error = painterResource(R.drawable.ic_music_placeholder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (isActive && isPlaying) {
                HomeEqIndicator(accent = accent)
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.song.title,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artists.joinToString { it.name },
                style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = com.aura.music.utils.makeTimeString(song.song.duration * 1000L),
            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Gray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            painter = painterResource(R.drawable.more_vert),
            contentDescription = "Options",
            tint = Color.Gray,
            modifier = Modifier
                .size(18.dp)
                .clickable {
                    menuState.show {
                        SongMenu(
                            originalSong = song,
                            navController = navController,
                            onDismiss = menuState::dismiss
                        )
                    }
                }
        )
    }
}

@Composable
fun HomeEqIndicator(accent: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val height1 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "eq1"
    )
    val height2 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "eq2"
    )
    val height3 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "eq3"
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(height1.dp)
                .background(accent, RoundedCornerShape(1.dp))
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(height2.dp)
                .background(accent, RoundedCornerShape(1.dp))
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(height3.dp)
                .background(accent, RoundedCornerShape(1.dp))
        )
    }
}

@Composable
fun HomeMadeForYouGrid(
    playlists: List<com.aura.music.innertube.models.SongItem>,
    surface: Color,
    border: Color,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val rows = playlists.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { playlist ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(surface)
                            .border(1.dp, border, RoundedCornerShape(14.dp))
                            .clickable { navController.navigate("search/${URLEncoder.encode(playlist.title, "UTF-8")}") }
                    ) {
                        Column {
                            // Top cover placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF2C2C2E),
                                                Color(0xFF0C0C0E)
                                            )
                                        )
                                    )
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(playlist.thumbnail)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = playlist.title,
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = playlist.artists.joinToString { it.name },
                                    style = TextStyle(fontSize = 11.sp, color = Color.Gray),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
