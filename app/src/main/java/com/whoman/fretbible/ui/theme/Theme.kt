package com.whoman.fretbible.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp
val Background=Color(0xFF090A0C);val Surface=Color(0xFF111317);val ElevatedSurface=Color(0xFF171A1F);val Border=Color(0xFF242932);val TextPrimary=Color(0xFFF5F7FA);val TextSecondary=Color(0xFF9AA2AE);val TextMuted=Color(0xFF626A76);val Lime=Color(0xFFB8F34A);val Warning=Color(0xFFF2C94C);val Error=Color(0xFFFF5C67);val Info=Color(0xFF69A7FF)
private val Colors=darkColorScheme(primary=Lime,onPrimary=Background,secondary=Info,background=Background,onBackground=TextPrimary,surface=Surface,onSurface=TextPrimary,surfaceVariant=ElevatedSurface,onSurfaceVariant=TextSecondary,outline=Border,error=Error)
@Composable fun FretBibleTheme(content:@Composable()->Unit){MaterialTheme(colorScheme=Colors,typography=MaterialTheme.typography.copy(displayLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Black,fontSize=56.sp),displayMedium=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Black,fontSize=48.sp),headlineLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=30.sp),headlineSmall=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=22.sp),titleLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.SemiBold,fontSize=18.sp),bodyLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontSize=16.sp),bodyMedium=TextStyle(fontFamily=FontFamily.SansSerif,fontSize=14.sp),labelLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=12.sp)),content=content)}
