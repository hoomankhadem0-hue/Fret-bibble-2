package com.whoman.fretbible.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.sp

val Background = Color(0xFF080A0C)
val Surface = Color(0xFF101317)
val ElevatedSurface = Color(0xFF171B20)
val SurfaceStrong = Color(0xFF1C2127)
val Border = Color(0xFF252B33)
val TextPrimary = Color(0xFFF4F6F8)
val TextSecondary = Color(0xFF9CA5B2)
val TextMuted = Color(0xFF65707C)
val Lime = Color(0xFFA8C95A)
val LimeSoft = Lime.copy(alpha = .12f)
val Warning = Color(0xFFF2C94C)
val Error = Color(0xFFFF5C67)
val Info = Color(0xFF69A7FF)

private val Colors = darkColorScheme(
    primary = Lime,
    onPrimary = Background,
    secondary = Info,
    onSecondary = Background,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedSurface,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    outlineVariant = Border.copy(alpha = .55f),
    error = Error,
    onError = Background
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

@Composable
fun FretBibleTheme(content: @Composable () -> Unit) {
    val sans = FontFamily.SansSerif
    MaterialTheme(
        colorScheme = Colors,
        shapes = AppShapes,
        typography = MaterialTheme.typography.copy(
            displayLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Black, fontSize = 56.sp, letterSpacing = (-1.5).sp),
            displayMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Black, fontSize = 46.sp, letterSpacing = (-1.1).sp),
            headlineLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 30.sp, letterSpacing = (-.6).sp),
            headlineMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = (-.45).sp),
            headlineSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 22.sp),
            titleLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
            titleMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
            titleSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
            bodyLarge = TextStyle(fontFamily = sans, fontSize = 16.sp, lineHeight = 23.sp),
            bodyMedium = TextStyle(fontFamily = sans, fontSize = 14.sp, lineHeight = 20.sp),
            bodySmall = TextStyle(fontFamily = sans, fontSize = 12.sp, lineHeight = 17.sp),
            labelLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = .35.sp),
            labelMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = .65.sp),
            labelSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = .55.sp)
        ),
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Background,
                contentColor = TextPrimary,
                tonalElevation = 0.dp
            ) { content() }
        }
    )
}
