package com.example.minishop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ShopPrimary,
    background = ShopBackground,
    surface = ShopWhite,
    onPrimary = Color.White,
    onBackground = ShopTextDark,
    onSurface = ShopTextDark
)

@Composable
fun MiniShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
