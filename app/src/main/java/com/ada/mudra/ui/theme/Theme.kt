package com.ada.mudra.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// High-contrast, senior-first palette. Light theme only on purpose: sudden
// dark-mode switches confuse and frighten the people this app is for.
val MudraBlue = Color(0xFF0B4FA2)
val CallGreen = Color(0xFF166B33)
val WhatsAppGreen = Color(0xFF075E54)
val PhotoPurple = Color(0xFF5B2C86)
val InkText = Color(0xFF14181D)
val SoftBlueContainer = Color(0xFFE7F0FE)
val ErrorRed = Color(0xFFB3261E)

private val MudraColors = lightColorScheme(
    primary = MudraBlue,
    onPrimary = Color.White,
    secondaryContainer = SoftBlueContainer,
    onSecondaryContainer = InkText,
    background = Color.White,
    onBackground = InkText,
    surface = Color.White,
    onSurface = InkText,
    surfaceVariant = Color(0xFFF1F3F6),
    onSurfaceVariant = Color(0xFF3C4248),
    outline = Color(0xFF44474C),
    error = ErrorRed,
    onError = Color.White,
)

// Senior minimums from CLAUDE.md: body >= 22sp, buttons 26-32sp. These are the
// 100% sizes; everything must still work at Android font scale 200%.
private val MudraTypography = Typography(
    headlineMedium = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    bodyMedium = TextStyle(fontSize = 22.sp, lineHeight = 30.sp),
    labelLarge = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
)

private val MudraShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun MudraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MudraColors,
        typography = MudraTypography,
        shapes = MudraShapes,
        content = content,
    )
}
