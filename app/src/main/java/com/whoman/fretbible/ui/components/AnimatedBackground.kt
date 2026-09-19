package com.whoman.fretbible.ui.components
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import com.whoman.fretbible.ui.theme.*
import kotlin.math.sin
@Composable fun AnimatedBackground(modifier:Modifier=Modifier){val t=rememberInfiniteTransition(label="background");val phase by t.animateFloat(0f,1f,infiniteRepeatable(tween(7000,easing=LinearEasing)),label="phase");Canvas(modifier.fillMaxSize()){drawRect(Background);val x=size.width*(.15f+.55f*phase);val y=size.height*(.20f+.15f*sin(phase*Math.PI*2).toFloat());drawCircle(brush=Brush.radialGradient(colors=listOf(Lime.copy(alpha=.09f),Color.Transparent),center=Offset(x,y),radius=size.minDimension*.55f),radius=size.minDimension*.55f,center=Offset(x,y))}}
