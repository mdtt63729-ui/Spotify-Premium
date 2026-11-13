package com.manikdev.spotify.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.manikdev.spotify.expect.ui.DesktopMiniPlayerClearance
import com.manikdev.spotify.expect.ui.VerticalScrollBar
import com.manikdev.spotify.ui.component.CenterLoadingBox
import com.manikdev.spotify.ui.component.EndOfPage
import com.manikdev.spotify.ui.component.MoodAndGenresContentItem
import com.manikdev.spotify.ui.component.NormalAppBar
import com.manikdev.spotify.ui.icon.ArrowBackIosNew
import com.manikdev.spotify.ui.icon.SpotifyIcons
import com.manikdev.spotify.ui.theme.typo
import com.manikdev.spotify.viewModel.MoodViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import spotify.composeapp.generated.resources.*

@Composable
fun MoodScreen(
    navController: NavController,
    viewModel: MoodViewModel = koinViewModel(),
    params: String?,
) {
    val moodData by viewModel.moodsMomentObject.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = params) {
        if (params != null) {
            viewModel.getMood(params)
        }
    }

    Column {
        NormalAppBar(
            title = {
                Text(
                    text = moodData?.header ?: "",
                    style = typo().labelMedium,
                )
            },
            leftIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        SpotifyIcons.ArrowBackIosNew,
                        contentDescription = "Back",
                    )
                }
            },
        )
        AnimatedVisibility(visible = !loading) {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(moodData?.items ?: emptyList()) { item ->
                    MoodAndGenresContentItem(
                        data = item,
                        navController = navController,
                    )
                }
                item {
                    EndOfPage()
                }
            }
            VerticalScrollBar(
                scrollState = listState,
                modifier = Modifier.padding(bottom = DesktopMiniPlayerClearance),
            )
        }
        AnimatedVisibility(visible = loading) {
            CenterLoadingBox(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}