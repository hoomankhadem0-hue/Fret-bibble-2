package com.whoman.fretbible.ui.components
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.ui.theme.*
@Composable fun Fretboard(highlightedString:Int?=null,highlightedFret:Int?=null,modifier:Modifier=Modifier){val p=rememberInfiniteTransition(label="fretPulse");val alpha by p.animateFloat(.55f,1f,infiniteRepeatable(tween(650),RepeatMode.Reverse),label="pulse");Canvas(modifier.fillMaxWidth().height(250.dp)){val left=20f;val right=size.width-20f;val top=22f;val bottom=size.height-22f;for(s in 0..5){val y=top+(bottom-top)*s/5f;drawLine(TextMuted.copy(alpha=.8f),Offset(left,y),Offset(right,y),strokeWidth=2f+s*.35f,cap=StrokeCap.Round)};for(f in 0..12){val x=left+(right-left)*f/12f;drawLine(Border,Offset(x,top),Offset(x,bottom),strokeWidth=if(f==0)8f else 2f)};listOf(3,5,7,9).forEach{f->val x=left+(right-left)*(f-.5f)/12f;drawCircle(TextMuted.copy(alpha=.45f),4f,Offset(x,(top+bottom)/2f))};val x12=left+(right-left)*11.5f/12f;drawCircle(TextMuted.copy(alpha=.45f),4f,Offset(x12,(top+bottom)/2f-24f));drawCircle(TextMuted.copy(alpha=.45f),4f,Offset(x12,(top+bottom)/2f+24f));if(highlightedString!=null&&highlightedFret!=null){val y=top+(bottom-top)*highlightedString/5f;val hx=left+(right-left)*(highlightedFret-.5f)/12f;drawCircle(Lime.copy(alpha=alpha),17f,Offset(hx,y));drawCircle(Lime,8f,Offset(hx,y))}}}
