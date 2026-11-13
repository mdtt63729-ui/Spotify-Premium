package com.manikdev.spotify.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.manikdev.spotify.ui.navigation.destination.home.AppLogDestination
import com.manikdev.spotify.ui.navigation.destination.home.CreditDestination
import com.manikdev.spotify.ui.navigation.destination.home.ListenTogetherDestination
import com.manikdev.spotify.ui.navigation.destination.home.ListenTogetherSettingsDestination
import com.manikdev.spotify.ui.navigation.destination.home.MoodDestination
import com.manikdev.spotify.ui.navigation.destination.home.NotificationDestination
import com.manikdev.spotify.ui.navigation.destination.home.SettingsDestination
import com.manikdev.spotify.ui.screen.home.ListenTogetherScreen
import com.manikdev.spotify.ui.screen.home.ListenTogetherSettingsScreen
import com.manikdev.spotify.ui.screen.home.MoodScreen
import com.manikdev.spotify.ui.screen.home.NotificationScreen
import com.manikdev.spotify.ui.screen.home.SettingScreen
import com.manikdev.spotify.ui.screen.other.AppLogScreen
import com.manikdev.spotify.ui.screen.other.CreditScreen

fun NavGraphBuilder.homeScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<CreditDestination> {
        CreditScreen(
            paddingValues = innerPadding,
            navController = navController,
        )
    }
    composable<AppLogDestination> {
        AppLogScreen(
            paddingValues = innerPadding,
            navController = navController,
        )
    }
    composable<MoodDestination> { entry ->
        val params = entry.toRoute<MoodDestination>().params
        MoodScreen(
            navController = navController,
            params = params,
        )
    }
    composable<ListenTogetherDestination> {
        ListenTogetherScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<ListenTogetherSettingsDestination> {
        ListenTogetherSettingsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<NotificationDestination> {
        NotificationScreen(
            navController = navController,
        )
    }
    composable<SettingsDestination> {
        SettingScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
}