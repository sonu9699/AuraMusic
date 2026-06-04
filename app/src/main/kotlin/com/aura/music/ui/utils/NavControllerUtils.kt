/*
 * AuraMusic - by Nikhil
 * Nikhil
 * Licensed Under GPL-3.0
 */



package com.aura.music.ui.utils

import androidx.navigation.NavController
import com.aura.music.ui.screens.Screens

fun NavController.backToMain() {
    val mainRoutes = Screens.MainScreens.map { it.route }

    while (previousBackStackEntry != null &&
        currentBackStackEntry?.destination?.route !in mainRoutes
    ) {
        popBackStack()
    }
}
