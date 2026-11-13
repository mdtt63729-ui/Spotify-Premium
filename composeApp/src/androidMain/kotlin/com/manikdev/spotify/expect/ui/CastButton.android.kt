package com.manikdev.spotify.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.spotify.cast.CastIconButton
import org.spotify.cast.isCastAvailable
import org.spotify.cast.rememberCastRoutes
import org.spotify.cast.selectCastRoute
import org.spotify.cast.stopCasting

@Composable
actual fun PlatformCastButton(
    modifier: Modifier,
    tint: Color,
) {
    CastIconButton(modifier = modifier, tint = tint)
}

actual fun isPlatformCastAvailable(): Boolean = isCastAvailable()

@Composable
actual fun rememberCastReceivers(discover: Boolean): CastReceivers {
    val context = LocalContext.current
    val routes = rememberCastRoutes(discover)
    return CastReceivers(
        receivers = routes.map { CastReceiver(id = it.id, name = it.name, isConnected = it.isSelected) },
        connect = { id -> selectCastRoute(context, id) },
        disconnect = { stopCasting(context) },
    )
}

