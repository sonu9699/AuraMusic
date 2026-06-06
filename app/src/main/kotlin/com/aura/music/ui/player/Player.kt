/*
 * AuraMusic - by Nikhil
 * Nikhil
 * Licensed Under GPL-3.0
 */



package com.aura.music.ui.player

import android.content.ClipboardManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.center
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SliderDefaults

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.aura.music.playback.PlayerConnection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.media3.common.C
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.Player.STATE_READY
import androidx.navigation.NavController
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.aura.music.playback.ExoDownloadService
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.toBitmap
import com.aura.music.LocalDownloadUtil
import com.aura.music.LocalPlayerConnection
import com.aura.music.R
import com.aura.music.constants.CustomThemeColorKey
import com.aura.music.constants.DarkModeKey
import com.aura.music.constants.DisableBlurKey
import com.aura.music.constants.PlayerBackgroundStyle
import com.aura.music.constants.PlayerBackgroundStyleKey
import com.aura.music.constants.PlayerButtonsStyle
import com.aura.music.constants.PlayerButtonsStyleKey
import com.aura.music.constants.PlayerCustomBlurKey
import com.aura.music.constants.PlayerCustomBrightnessKey
import com.aura.music.constants.PlayerCustomContrastKey
import com.aura.music.constants.PlayerCustomImageUriKey
import com.aura.music.constants.PlayerDesignStyle
import com.aura.music.constants.PlayerDesignStyleKey
import com.aura.music.constants.QueuePeekHeight
import com.aura.music.constants.SliderStyle
import com.aura.music.constants.SliderStyleKey
import com.aura.music.constants.UseNewMiniPlayerDesignKey
import com.aura.music.extensions.metadata
import com.aura.music.extensions.togglePlayPause
import com.aura.music.innertube.toHighResThumbnail
import com.aura.music.models.MediaMetadata
import com.aura.music.ui.component.BottomSheet
import com.aura.music.ui.component.BottomSheetState
import com.aura.music.ui.component.LocalBottomSheetPageState
import com.aura.music.ui.component.LocalMenuState
import com.aura.music.ui.component.rememberBottomSheetState
import com.aura.music.ui.menu.PlayerMenu
import com.aura.music.ui.screens.settings.DarkMode
import com.aura.music.ui.theme.PlayerColorExtractor
import com.aura.music.ui.utils.ShowMediaInfo
import com.aura.music.utils.rememberEnumPreference
import com.aura.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val menuState = LocalMenuState.current

    val bottomSheetPageState = LocalBottomSheetPageState.current

    val playerConnection = LocalPlayerConnection.current ?: return

    val playerDesignStyle by rememberEnumPreference(
        key = PlayerDesignStyleKey,
        defaultValue = PlayerDesignStyle.V3
    )

    val (useNewMiniPlayerDesign) = rememberPreference(
        UseNewMiniPlayerDesignKey,
        defaultValue = true
    )

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = PlayerBackgroundStyle.COLORING
    )

    val (playerCustomImageUri) = rememberPreference(PlayerCustomImageUriKey, "")
    val (playerCustomBlur) = rememberPreference(PlayerCustomBlurKey, 0f)
    val (playerCustomContrast) = rememberPreference(PlayerCustomContrastKey, 1f)
    val (playerCustomBrightness) = rememberPreference(PlayerCustomBrightnessKey, 1f)

    val (disableBlur) = rememberPreference(DisableBlurKey, true)
    val (showCodecOnPlayer) = rememberPreference(
        booleanPreferencesKey("show_codec_on_player"),
        false
    )

    val playerButtonsStyle by rememberEnumPreference(
        key = PlayerButtonsStyleKey,
        defaultValue = PlayerButtonsStyle.SECONDARY
    )

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.ON)
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }
    when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.secondary
        else ->
            if (useDarkTheme)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onPrimary
    }
    val useBlackBackground =
        remember(isSystemInDarkTheme, darkTheme, pureBlack) {
            val useDarkTheme =
                if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
            useDarkTheme && pureBlack
        }
    if (useNewMiniPlayerDesign) {
        if (useBlackBackground && state.value > state.collapsedBound) {
            val progress =
                ((state.value - state.collapsedBound) / (state.expandedBound - state.collapsedBound))
                    .coerceIn(0f, 1f)
            Color.Black.copy(alpha = progress)
        } else {
            val progress =
                ((state.value - state.collapsedBound) / (state.expandedBound - state.collapsedBound))
                    .coerceIn(0f, 1f)
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = progress)
        }
    } else {
        if (useBlackBackground) {
            lerp(MaterialTheme.colorScheme.surfaceContainer, Color.Black, state.progress)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    }

    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val currentSongLiked = currentSong?.song?.liked == true
    val queueWindows by playerConnection.queueWindows.collectAsState()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    playerConnection.service.playerVolume.collectAsState()

    val automix by playerConnection.service.automixItems.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()

    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.Circular)

    var position by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.currentPosition)
    }
    var duration by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.duration)
    }
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }

    val isLoading = playbackState == STATE_BUFFERING || sliderPosition != null

    var gradientColors by remember {
        mutableStateOf<List<Color>>(emptyList())
    }

    var previousThumbnailUrl by remember { mutableStateOf<String?>(null) }
    var previousGradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    val gradientColorsCache = remember { mutableMapOf<String, List<Color>>() }

    if (!canSkipNext && automix.isNotEmpty()) {
        playerConnection.service.addToQueueAutomix(automix[0], 0)
    }

    val defaultGradientColors =
        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
    val fallbackColor = MaterialTheme.colorScheme.surface.toArgb()

    LaunchedEffect(mediaMetadata?.id) {
        val currentThumbnail = mediaMetadata?.thumbnailUrl
        if (currentThumbnail != previousThumbnailUrl) {
            previousThumbnailUrl = currentThumbnail
            previousGradientColors = gradientColors
        }
    }

    LaunchedEffect(mediaMetadata?.id, playerBackground) {
        if (playerBackground == PlayerBackgroundStyle.GRADIENT || playerBackground == PlayerBackgroundStyle.COLORING || playerBackground == PlayerBackgroundStyle.BLUR_GRADIENT || playerBackground == PlayerBackgroundStyle.GLOW || playerBackground == PlayerBackgroundStyle.GLOW_ANIMATED) {
            val currentMetadata = mediaMetadata
            if (currentMetadata != null && currentMetadata.thumbnailUrl != null) {
                val cachedColors = gradientColorsCache[currentMetadata.id]
                if (cachedColors != null) {
                    gradientColors = cachedColors
                } else {
                    val request = ImageRequest.Builder(context)
                        .data(currentMetadata.thumbnailUrl)
                        .size(
                            PlayerColorExtractor.Config.IMAGE_SIZE,
                            PlayerColorExtractor.Config.IMAGE_SIZE
                        )
                        .allowHardware(false)
                        .build()

                    val result = runCatching {
                        withContext(Dispatchers.IO) {
                            context.imageLoader.execute(request)
                        }
                    }.getOrNull()

                    if (result != null) {
                        val bitmap = result.image?.toBitmap()
                        if (bitmap != null) {
                            val palette = withContext(Dispatchers.Default) {
                                Palette.from(bitmap)
                                    .maximumColorCount(PlayerColorExtractor.Config.MAX_COLOR_COUNT)
                                    .resizeBitmapArea(PlayerColorExtractor.Config.BITMAP_AREA)
                                    .generate()
                            }

                            val extractedColors = PlayerColorExtractor.extractGradientColors(
                                palette = palette,
                                fallbackColor = fallbackColor
                            )

                            gradientColorsCache[currentMetadata.id] = extractedColors
                            gradientColors = extractedColors
                        } else {
                            gradientColors = defaultGradientColors
                        }
                    } else {
                        gradientColors = defaultGradientColors
                    }
                }
            } else {
                gradientColors = emptyList()
            }
        } else {
            gradientColors = emptyList()
        }
    }

    state.expandedBound / 3

    val TextBackgroundColor =
        when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
            PlayerBackgroundStyle.BLUR -> Color.White
            PlayerBackgroundStyle.GRADIENT -> Color.White
            PlayerBackgroundStyle.COLORING -> Color.White
            PlayerBackgroundStyle.BLUR_GRADIENT -> Color.White
            PlayerBackgroundStyle.GLOW -> Color.White
            PlayerBackgroundStyle.GLOW_ANIMATED -> Color.White
            PlayerBackgroundStyle.CUSTOM -> Color.White
            PlayerBackgroundStyle.LIQUID_GOOEY -> Color.White
        }

    val icBackgroundColor =
        when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.surface
            PlayerBackgroundStyle.BLUR -> Color.Black
            PlayerBackgroundStyle.GRADIENT -> Color.Black
            PlayerBackgroundStyle.COLORING -> Color.Black
            PlayerBackgroundStyle.BLUR_GRADIENT -> Color.Black
            PlayerBackgroundStyle.GLOW -> Color.Black
            PlayerBackgroundStyle.GLOW_ANIMATED -> Color.Black
            PlayerBackgroundStyle.CUSTOM -> Color.Black
            PlayerBackgroundStyle.LIQUID_GOOEY -> Color.Black
        }

    val (textButtonColor, iconButtonColor) = when (playerButtonsStyle) {
        PlayerButtonsStyle.DEFAULT -> Pair(TextBackgroundColor, icBackgroundColor)
        PlayerButtonsStyle.SECONDARY -> Pair(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSecondary
        )
    }

    val sleepTimerEnabled =
        remember(
            playerConnection.service.sleepTimer.triggerTime,
            playerConnection.service.sleepTimer.pauseWhenSongEnd
        ) {
            playerConnection.service.sleepTimer.isActive
        }

    var sleepTimerTimeLeft by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft =
                    if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                        playerConnection.player.duration - playerConnection.player.currentPosition
                    } else {
                        playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                    }
                delay(1000L)
            }
        }
    }

    val (shakeToShuffle) = rememberPreference(
        com.aura.music.constants.ShakeToShuffleKey,
        defaultValue = true
    )
    var showShuffleHud by remember { mutableStateOf(false) }

    val isPlayerExpanded = !state.isCollapsed
    LaunchedEffect(isPlayerExpanded, shakeToShuffle) {
        if (!isPlayerExpanded || !shakeToShuffle) return@LaunchedEffect
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return@LaunchedEffect
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return@LaunchedEffect

        var lastShakeTime = 0L
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val gX = x / SensorManager.GRAVITY_EARTH
                val gY = y / SensorManager.GRAVITY_EARTH
                val gZ = z / SensorManager.GRAVITY_EARTH

                val gForce = kotlin.math.sqrt(gX * gX + gY * gY + gZ * gZ)

                if (gForce > 1.45f) { // Shake detected
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > 2000) {
                        lastShakeTime = now
                        try {
                            playerConnection.player.shuffleModeEnabled = !playerConnection.player.shuffleModeEnabled
                            showShuffleHud = true
                        } catch (_: Exception) {}
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        try {
            while (isActive) {
                delay(1000)
            }
        } finally {
            sensorManager.unregisterListener(listener)
        }
    }

    LaunchedEffect(showShuffleHud) {
        if (showShuffleHud) {
            delay(1600)
            showShuffleHud = false
        }
    }

    var showSleepTimerDialog by remember {
        mutableStateOf(false)
    }

    var sleepTimerValue by remember {
        mutableFloatStateOf(30f)
    }
    if (showSleepTimerDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showSleepTimerDialog = false },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.bedtime),
                    contentDescription = null
                )
            },
            title = { Text(stringResource(R.string.sleep_timer)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSleepTimerDialog = false
                        playerConnection.service.sleepTimer.start(sleepTimerValue.roundToInt())
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSleepTimerDialog = false },
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.minute,
                            sleepTimerValue.roundToInt(),
                            sleepTimerValue.roundToInt()
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Slider(
                        value = sleepTimerValue,
                        onValueChange = { sleepTimerValue = it },
                        valueRange = 5f..120f,
                        steps = (120 - 5) / 5 - 1,
                    )

                    OutlinedIconButton(
                        onClick = {
                            showSleepTimerDialog = false
                            playerConnection.service.sleepTimer.start(-1)
                        },
                    ) {
                        Text(stringResource(R.string.end_of_song))
                    }
                }
            },
        )
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(100)
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
            }
        }
    }

    val dynamicQueuePeekHeight =
        if (showCodecOnPlayer) {
            88.dp
        } else {
            QueuePeekHeight
        }

    val dismissedBound =
        dynamicQueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    val queueSheetState = rememberBottomSheetState(
        dismissedBound = dismissedBound,
        expandedBound = state.expandedBound,
        collapsedBound = dismissedBound + 1.dp,
        initialAnchor = 1
    )

    val lyricsSheetState = rememberBottomSheetState(
        dismissedBound = 0.dp,
        expandedBound = state.expandedBound,
        collapsedBound = 0.dp,
        initialAnchor = 1
    )

    BackHandler(
        enabled =
            (!lyricsSheetState.isCollapsed && !lyricsSheetState.isDismissed) ||
                    (!queueSheetState.isCollapsed && !queueSheetState.isDismissed) ||
                    (!state.isCollapsed && !state.isDismissed)
    ) {
        when {
            !lyricsSheetState.isCollapsed && !lyricsSheetState.isDismissed -> lyricsSheetState.collapseSoft()
            !queueSheetState.isCollapsed && !queueSheetState.isDismissed -> queueSheetState.collapseSoft()
            !state.isCollapsed && !state.isDismissed -> state.collapseSoft()
        }
    }

    BottomSheet(
        state = state,
        modifier = modifier,
        backgroundColor = when (playerBackground) {
            PlayerBackgroundStyle.BLUR, PlayerBackgroundStyle.GRADIENT -> {
                val progress =
                    ((state.value - state.collapsedBound) / (state.expandedBound - state.collapsedBound))
                        .coerceIn(0f, 1f)

                val fadeProgress = if (progress < 0.2f) {
                    ((0.2f - progress) / 0.2f).coerceIn(0f, 1f)
                } else {
                    0f
                }

                MaterialTheme.colorScheme.surface.copy(alpha = 1f - fadeProgress)
            }

            else -> {
                val progress =
                    ((state.value - state.collapsedBound) / (state.expandedBound - state.collapsedBound))
                        .coerceIn(0f, 1f)

                val fadeProgress = if (progress < 0.2f) {
                    ((0.2f - progress) / 0.2f).coerceIn(0f, 1f)
                } else {
                    0f
                }

                if (useBlackBackground) {
                    Color.Black.copy(alpha = 1f - fadeProgress)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 1f - fadeProgress)
                }
            }
        },
        onDismiss = {
            playerConnection.service.stopAndClearPlayback()
        },
        collapsedContent = {
            MiniPlayer(
                position = position,
                duration = duration,
                pureBlack = pureBlack,
            )
        },
    ) {
        val onSliderValueChange: (Long) -> Unit = { sliderPosition = it }
        val onSliderValueChangeFinished: () -> Unit = {
            sliderPosition?.let {
                playerConnection.player.seekTo(it)
                position = it
            }
            sliderPosition = null
        }
        duration > 0L && duration != C.TIME_UNSET

        remember(queueWindows, currentWindowIndex) {
            queueWindows.getOrNull(currentWindowIndex + 1)?.mediaItem?.metadata
        }

        val enrichedMetadata = remember(mediaMetadata, currentSong) {
            val meta = mediaMetadata ?: return@remember null
            if (meta.album != null) return@remember meta
            val dbAlbum = currentSong?.album
            val dbAlbumId = currentSong?.song?.albumId
            when {
                dbAlbum != null -> meta.copy(
                    album = MediaMetadata.Album(id = dbAlbum.id, title = dbAlbum.title)
                )

                dbAlbumId != null -> meta.copy(
                    album = MediaMetadata.Album(
                        id = dbAlbumId,
                        title = currentSong?.song?.albumName.orEmpty()
                    )
                )

                else -> meta
            }
        }

        val controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit = { mediaMetadata ->
            PlayerControlsContent(
                mediaMetadata = mediaMetadata,
                playerDesignStyle = playerDesignStyle,
                sliderStyle = sliderStyle,
                playbackState = playbackState,
                isPlaying = isPlaying,
                isLoading = isLoading,
                repeatMode = repeatMode,
                canSkipPrevious = canSkipPrevious,
                canSkipNext = canSkipNext,
                textButtonColor = textButtonColor,
                iconButtonColor = iconButtonColor,
                textBackgroundColor = TextBackgroundColor,
                icBackgroundColor = icBackgroundColor,
                sliderPosition = sliderPosition,
                position = position,
                duration = duration,
                playerConnection = playerConnection,
                navController = navController,
                state = state,
                menuState = menuState,
                bottomSheetPageState = bottomSheetPageState,
                clipboardManager = clipboardManager,
                context = context,
                onSliderValueChange = onSliderValueChange,
                onSliderValueChangeFinished = onSliderValueChangeFinished,
                onShowSleepTimer = { showSleepTimerDialog = true }
            )
        }

        if (!state.isCollapsed) {
            PlayerBackground(
                playerBackground = playerBackground,
                mediaMetadata = mediaMetadata,
                gradientColors = gradientColors,
                disableBlur = disableBlur,
                playerCustomImageUri = playerCustomImageUri,
                playerCustomBlur = playerCustomBlur,
                playerCustomContrast = playerCustomContrast,
                playerCustomBrightness = playerCustomBrightness
            )
        }

        val customThemeColorValue by rememberPreference(CustomThemeColorKey, defaultValue = "volt_neon")
        val isVolt = customThemeColorValue == "volt_neon"
        val isCrimson = customThemeColorValue == "crimson"
        val accent = when {
            isVolt -> Color(0xFF7F56D9)
            isCrimson -> Color(0xFFE8002D)
            else -> MaterialTheme.colorScheme.primary
        }

        if (!state.isCollapsed && (isVolt || isCrimson)) {
            CustomMockupPlayer(
                mediaMetadata = enrichedMetadata ?: mediaMetadata,
                isPlaying = isPlaying,
                positionMs = position,
                durationMs = duration,
                playbackState = playbackState,
                liked = currentSongLiked,
                accent = accent,
                isVolt = isVolt,
                isCrimson = isCrimson,
                playerConnection = playerConnection,
                navController = navController,
                state = state,
                queueSheetState = queueSheetState,
                lyricsSheetState = lyricsSheetState,
                menuState = menuState,
                onSliderValueChange = onSliderValueChange,
                onSliderValueChangeFinished = onSliderValueChangeFinished
            )
        } else {
            // distance
            when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                if (playerDesignStyle == PlayerDesignStyle.V5) {
                    enrichedMetadata?.let { metadata ->
                        MetroPlayerContent(
                            mediaMetadata = metadata,
                            sliderPosition = sliderPosition,
                            positionMs = position,
                            durationMs = duration,
                            textColor = TextBackgroundColor,
                            liked = currentSongLiked,
                            playerConnection = playerConnection,
                            onToggleLike = playerConnection::toggleLike,
                            onExpandQueue = queueSheetState::expandSoft,
                            onMenuClick = {
                                menuState.show {
                                    PlayerMenu(
                                        mediaMetadata = metadata,
                                        navController = navController,
                                        playerBottomSheetState = state,
                                        onShowDetailsDialog = {
                                            bottomSheetPageState.show { ShowMediaInfo(metadata.id) }
                                        },
                                        onDismiss = menuState::dismiss
                                    )
                                }
                            },
                            context = context,
                            bottomPadding = dynamicQueuePeekHeight

                        )
                    }

                } else {
                    Row(
                        modifier =
                            Modifier
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                                .padding(bottom = queueSheetState.collapsedBound + 48.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f),
                        ) {
                            val screenWidth = LocalConfiguration.current.screenWidthDp
                            val thumbnailSize = (screenWidth * 0.4).dp
                            Thumbnail(
                                sliderPositionProvider = { sliderPosition },
                                modifier = Modifier.size(thumbnailSize),
                                isPlayerExpanded = state.isExpanded
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .windowInsetsPadding(
                                        WindowInsets.systemBars.only(
                                            WindowInsetsSides.Top
                                        )
                                    ),
                        ) {
                            Spacer(Modifier.weight(1f))

                            enrichedMetadata?.let {
                                controlsContent(it)
                            }

                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            else -> {
                if (playerDesignStyle == PlayerDesignStyle.V5) {
                    enrichedMetadata?.let { metadata ->
                        MetroPlayerContent(
                            mediaMetadata = metadata,
                            sliderPosition = sliderPosition,
                            positionMs = position,
                            durationMs = duration,
                            textColor = TextBackgroundColor,
                            liked = currentSongLiked,
                            playerConnection = playerConnection,
                            onToggleLike = playerConnection::toggleLike,
                            onExpandQueue = queueSheetState::expandSoft,
                            onMenuClick = {
                                menuState.show {
                                    PlayerMenu(
                                        mediaMetadata = metadata,
                                        navController = navController,
                                        playerBottomSheetState = state,
                                        onShowDetailsDialog = {
                                            bottomSheetPageState.show { ShowMediaInfo(metadata.id) }
                                        },
                                        onDismiss = menuState::dismiss
                                    )
                                }
                            },
                            context = context,
                            bottomPadding = dynamicQueuePeekHeight

                        )
                    }

                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier =
                            Modifier
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                                .padding(bottom = queueSheetState.collapsedBound),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f),
                        ) {
                            Thumbnail(
                                sliderPositionProvider = { sliderPosition },
                                modifier = Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection),
                                isPlayerExpanded = state.isExpanded
                            )
                        }

                        enrichedMetadata?.let {
                            val isDark = isSystemInDarkTheme()
                            val glassFillColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                            val glassBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .background(
                                        color = glassFillColor,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = glassBorderColor,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(vertical = 20.dp, horizontal = 8.dp)
                            ) {
                                controlsContent(it)
                            }
                        }

                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
        }

        val queueOnBackgroundColor =
            if (useBlackBackground) Color.White else MaterialTheme.colorScheme.onSurface
        val queueSurfaceColor =
            if (useBlackBackground) Color.Black else MaterialTheme.colorScheme.surface

        val (_, _) = when (playerButtonsStyle) {
            PlayerButtonsStyle.DEFAULT -> Pair(queueOnBackgroundColor, queueSurfaceColor)
            PlayerButtonsStyle.SECONDARY -> Pair(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.onSecondary
            )
        }

        Queue(
            state = queueSheetState,
            playerBottomSheetState = state,
            navController = navController,
            backgroundColor =
                if (useBlackBackground) {
                    Color.Black
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            onBackgroundColor = queueOnBackgroundColor,
            TextBackgroundColor = TextBackgroundColor,
            textButtonColor = textButtonColor,
            iconButtonColor = iconButtonColor,
            onShowLyrics = { lyricsSheetState.expandSoft() },
            pureBlack = pureBlack,
        )

        mediaMetadata?.let { metadata ->
            BottomSheet(
                state = lyricsSheetState,
                backgroundColor = Color.Unspecified,
                onDismiss = { /* Optional dismiss action */ },
                collapsedContent = {
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = lyricsSheetState.progress.coerceIn(0f, 1f)
                            )
                        )
                ) {
                    LyricsScreen(
                        mediaMetadata = metadata,
                        onBackClick = { lyricsSheetState.collapseSoft() },
                        navController = navController
                    )
                }
            }
        }

        if (showShuffleHud) {
            val shuffleEnabled = playerConnection.player.shuffleModeEnabled
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val hudTransition = rememberInfiniteTransition(label = "hud_gooey")
                        val hudAngle by hudTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 2f * kotlin.math.PI.toFloat(),
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "hud_angle"
                        )

                        val hudGooeyEffect = remember {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val blurEffect = RenderEffect.createBlurEffect(15f, 15f, Shader.TileMode.DECAL)
                                val alphaContrastMatrix = android.graphics.ColorMatrix(floatArrayOf(
                                    1f, 0f, 0f, 0f, 0f,
                                    0f, 1f, 0f, 0f, 0f,
                                    0f, 0f, 1f, 0f, 0f,
                                    0f, 0f, 0f, 40f, -2000f
                                ))
                                val colorFilterEffect = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(alphaContrastMatrix))
                                RenderEffect.createChainEffect(colorFilterEffect, blurEffect).asComposeRenderEffect()
                            } else {
                                null
                            }
                        }

                        Canvas(
                            modifier = Modifier
                                .size(64.dp)
                                .graphicsLayer {
                                    if (hudGooeyEffect != null) {
                                        this.renderEffect = hudGooeyEffect
                                    }
                                }
                        ) {
                            val cw = size.width
                            val ch = size.height
                            val cCenter = Offset(cw / 2f, ch / 2f)

                            drawCircle(
                                color = Color.White,
                                radius = cw * 0.2f,
                                center = cCenter
                            )

                            val orbitDist = cw * 0.24f * (1.1f + 0.1f * kotlin.math.sin(hudAngle * 2f))
                            val orbitOffset = Offset(
                                x = cCenter.x + orbitDist * kotlin.math.cos(hudAngle),
                                y = cCenter.y + orbitDist * kotlin.math.sin(hudAngle)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = cw * 0.16f,
                                center = orbitOffset
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Icon(
                            painter = painterResource(R.drawable.shuffle),
                            contentDescription = null,
                            tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(26.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (shuffleEnabled) "Shuffle On" else "Shuffle Off",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetroPlayerContent(
    mediaMetadata: MediaMetadata,
    sliderPosition: Long?,
    positionMs: Long,
    durationMs: Long,
    textColor: Color,
    liked: Boolean,
    playerConnection: com.aura.music.playback.PlayerConnection,
    onToggleLike: () -> Unit,
    onExpandQueue: () -> Unit,
    onMenuClick: () -> Unit,
    context: Context,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val isLoading = playbackState == STATE_BUFFERING
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.7f)
            )
            Text(
                text = mediaMetadata.album?.title ?: "Playing from Library",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            coil3.compose.AsyncImage(
                model = mediaMetadata.thumbnailUrl?.toHighResThumbnail(),
                contentDescription = "Album Art",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = com.aura.music.constants.PlayerHorizontalPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaMetadata.title ?: "Unknown",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = mediaMetadata.artists.joinToString { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    onClick = {
                        val intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND; type =
                            "text/plain"; putExtra(
                            android.content.Intent.EXTRA_TEXT,
                            "https://music.youtube.com/watch?v=${mediaMetadata.id}"
                        )
                        }; context.startActivity(android.content.Intent.createChooser(intent, null))
                    },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.share),
                            contentDescription = "Share",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = onToggleLike,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painterResource(if (liked) R.drawable.favorite else R.drawable.favorite_border),
                            contentDescription = "Like",
                            tint = if (liked) MaterialTheme.colorScheme.error else Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val displayPositionMs = sliderPosition ?: positionMs
            StyledPlaybackSlider(
                sliderStyle = SliderStyle.Wavy,
                value = (displayPositionMs.toFloat() / durationMs.coerceAtLeast(1L)).coerceIn(
                    0f,
                    1f
                ),
                valueRange = 0f..1f,
                onValueChange = { fraction -> playerConnection.player.seekTo((durationMs * fraction).toLong()) },
                onValueChangeFinished = {},
                activeColor = textColor,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .offset(y = (-8).dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = com.aura.music.utils.makeTimeString(displayPositionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
                Text(
                    text = if (durationMs != C.TIME_UNSET) com.aura.music.utils.makeTimeString(
                        durationMs
                    ) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val playInteractionSource =
                androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val isPlayPressed by playInteractionSource.collectIsPressedAsState()
            val sideButtonWidth by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isPlayPressed) 48.dp else 64.dp,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.6f,
                    stiffness = 400f
                ),
                label = "SideWidth"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = playerConnection::seekToPrevious,
                    shape = RoundedCornerShape(50),
                    color = textColor.copy(alpha = 0.08f),
                    modifier = Modifier
                        .width(sideButtonWidth)
                        .height(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painterResource(R.drawable.skip_previous),
                            contentDescription = "Previous",
                            tint = textColor.copy(alpha = if (canSkipPrevious) 1f else 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Surface(
                    onClick = {
                        if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                            playerConnection.player.seekTo(
                                0,
                                0
                            ); playerConnection.player.playWhenReady = true
                        } else playerConnection.player.togglePlayPause()
                    },
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    interactionSource = playInteractionSource,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isLoading) com.aura.music.ui.component.AuraMusicLoader(size = 24.dp)
                        else {
                            Icon(
                                painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPlaying) "Pause" else "Play",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black
                            )
                        }
                    }
                }
                Surface(
                    onClick = playerConnection::seekToNext,
                    shape = RoundedCornerShape(50),
                    color = textColor.copy(alpha = 0.08f),
                    modifier = Modifier
                        .width(sideButtonWidth)
                        .height(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painterResource(R.drawable.skip_next),
                            contentDescription = "Next",
                            tint = textColor.copy(alpha = if (canSkipNext) 1f else 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun CustomMockupPlayer(
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    playbackState: Int,
    liked: Boolean,
    accent: Color,
    isVolt: Boolean,
    isCrimson: Boolean,
    playerConnection: PlayerConnection,
    navController: NavController,
    state: BottomSheetState,
    queueSheetState: BottomSheetState,
    lyricsSheetState: BottomSheetState,
    menuState: com.aura.music.ui.component.MenuState,
    onSliderValueChange: (Long) -> Unit,
    onSliderValueChangeFinished: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isLoading = playbackState == STATE_BUFFERING
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = queueSheetState.collapsedBound),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { state.collapseSoft() }) {
                Icon(
                    painter = painterResource(R.drawable.expand_more),
                    contentDescription = "Collapse",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "NOW PLAYING",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 2.sp
                )
            )
            IconButton(onClick = {
                mediaMetadata?.let { meta ->
                    menuState.show {
                        com.aura.music.ui.menu.PlayerMenu(
                            mediaMetadata = meta,
                            navController = navController,
                            playerBottomSheetState = state,
                            onShowDetailsDialog = {},
                            onDismiss = menuState::dismiss
                        )
                    }
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 2. ALBUM ART SECTION
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E22))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(mediaMetadata?.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.ic_music_placeholder),
                    error = painterResource(R.drawable.ic_music_placeholder),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 3. SONG INFO SECTION
        val downloadUtil = LocalDownloadUtil.current
        val downloadState by remember(mediaMetadata?.id) {
            if (mediaMetadata != null) {
                downloadUtil.getDownload(mediaMetadata.id)
            } else {
                kotlinx.coroutines.flow.flowOf(null)
            }
        }.collectAsState(initial = null)

        val isDownloaded = downloadState?.state == Download.STATE_COMPLETED
        val isDownloading = downloadState?.state == Download.STATE_QUEUED || downloadState?.state == Download.STATE_DOWNLOADING

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mediaMetadata?.title ?: "Unknown Song",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mediaMetadata?.artists?.joinToString { it.name } ?: "Unknown Artist",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.Gray
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val id = mediaMetadata?.id ?: return@IconButton
                        if (isDownloaded || isDownloading) {
                            DownloadService.sendRemoveDownload(
                                context,
                                ExoDownloadService::class.java,
                                id,
                                false
                            )
                        } else {
                            mediaMetadata?.let { meta ->
                                playerConnection.service.database.transaction {
                                    insert(meta)
                                }
                                val downloadRequest = DownloadRequest
                                    .Builder(id, id.toUri())
                                    .setCustomCacheKey(id)
                                    .setData(meta.title.toByteArray())
                                    .build()
                                DownloadService.sendAddDownload(
                                    context,
                                    ExoDownloadService::class.java,
                                    downloadRequest,
                                    false
                                )
                            }
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(color = accent, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            painter = painterResource(if (isDownloaded) R.drawable.offline else R.drawable.download),
                            contentDescription = "Download",
                            tint = if (isDownloaded) accent else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { playerConnection.toggleLike() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(if (liked) R.drawable.favorite else R.drawable.favorite_border),
                        contentDescription = "Like",
                        tint = if (liked) accent else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 4. PROGRESS SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            StyledPlaybackSlider(
                sliderStyle = SliderStyle.Simple,
                value = (positionMs).toFloat(),
                valueRange = 0f..(if (durationMs == C.TIME_UNSET) 0f else durationMs.toFloat()),
                onValueChange = { onSliderValueChange(it.toLong()) },
                onValueChangeFinished = onSliderValueChangeFinished,
                activeColor = accent,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = com.aura.music.utils.makeTimeString(positionMs),
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Gray)
                )
                Text(
                    text = com.aura.music.utils.makeTimeString(durationMs),
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Gray)
                )
            }
        }

        // 5. MAIN CONTROLS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { playerConnection.player.shuffleModeEnabled = !playerConnection.player.shuffleModeEnabled }) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = "Shuffle",
                    tint = if (playerConnection.player.shuffleModeEnabled) accent else Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = { playerConnection.seekToPrevious() },
                enabled = canSkipPrevious
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_previous),
                    contentDescription = "Previous",
                    tint = if (canSkipPrevious) Color.White else Color.DarkGray,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(accent, CircleShape)
                    .clickable { playerConnection.player.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            IconButton(
                onClick = { playerConnection.seekToNext() },
                enabled = canSkipNext
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    contentDescription = "Next",
                    tint = if (canSkipNext) Color.White else Color.DarkGray,
                    modifier = Modifier.size(26.dp)
                )
            }
            IconButton(onClick = {
                val nextRepeatMode = when (repeatMode) {
                    androidx.media3.common.Player.REPEAT_MODE_OFF -> androidx.media3.common.Player.REPEAT_MODE_ALL
                    androidx.media3.common.Player.REPEAT_MODE_ALL -> androidx.media3.common.Player.REPEAT_MODE_ONE
                    else -> androidx.media3.common.Player.REPEAT_MODE_OFF
                }
                playerConnection.player.repeatMode = nextRepeatMode
            }) {
                Icon(
                    painter = painterResource(
                        when (repeatMode) {
                            androidx.media3.common.Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                            else -> R.drawable.repeat
                        }
                    ),
                    contentDescription = "Repeat",
                    tint = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) accent else Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 6. SECONDARY CONTROLS (Lyrics/Queue Toggles & Waveform)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { lyricsSheetState.expandSoft() }) {
                Icon(
                    painter = painterResource(R.drawable.lyrics), // Lyrics
                    contentDescription = "Lyrics",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = { queueSheetState.expandSoft() }) {
                Icon(
                    painter = painterResource(R.drawable.volume_up), // Queue
                    contentDescription = "Queue",
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Waveform Animation
        if (isPlaying) {
            PlayerWaveform(accent = accent.copy(alpha = 0.25f))
        } else {
            // Static waveform placeholder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(20) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. UP NEXT DRAWER HANDLE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF121216))
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
                .clickable { queueSheetState.expandSoft() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0x22FFFFFF))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "UP NEXT",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                )
            }
            Icon(
                painter = painterResource(R.drawable.expand_less),
                contentDescription = "Open Queue",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RotatingCdDisc(thumbnailUrl: String?, isPlaying: Boolean, accent: Color) {
    val rotationAngle = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                rotationAngle.animateTo(
                    targetValue = rotationAngle.value + 360f,
                    animationSpec = tween(12000, easing = LinearEasing)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .size(280.dp)
            .padding(12.dp)
            .clip(CircleShape)
            .border(3.dp, Color(0x14FFFFFF), CircleShape)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // CD disc background with circular lines (vinyl texture)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cCenter = size.center
            // Draw vinyl circular lines
            for (r in 30..120 step 10) {
                drawCircle(
                    color = Color(0x0AFFFFFF),
                    radius = r.dp.toPx(),
                    style = Stroke(width = 1f)
                )
            }
        }

        // Center album artwork
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_music_placeholder),
            error = painterResource(R.drawable.ic_music_placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .graphicsLayer {
                    rotationZ = rotationAngle.value
                }
        )

        // Center hole of vinyl CD
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .border(2.dp, accent, CircleShape)
        )
    }
}

@Composable
fun CrimsonCover(thumbnailUrl: String?, accent: Color, isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "coverBreathe")
    val scale by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "coverScale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }
    val glowAlpha by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "coverGlow"
        )
    } else {
        remember { mutableStateOf(0.15f) }
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(260.dp)
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .border(4.dp, accent.copy(alpha = glowAlpha), RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_music_placeholder),
            error = painterResource(R.drawable.ic_music_placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PlayerWaveform(accent: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val heights = List(20) { index ->
        val delay = (index * 50) % 300
        val targetVal = when (index % 4) {
            0 -> 38f
            1 -> 24f
            2 -> 30f
            else -> 18f
        }
        infiniteTransition.animateFloat(
            initialValue = 10f,
            targetValue = targetVal,
            animationSpec = infiniteRepeatable(
                animation = tween(600 + delay, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave_$index"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { animVal ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(animVal.value.dp)
                    .background(accent, RoundedCornerShape(2.dp))
            )
        }
    }
}















