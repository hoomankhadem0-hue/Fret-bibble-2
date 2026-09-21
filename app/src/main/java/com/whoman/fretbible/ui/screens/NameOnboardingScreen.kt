package com.whoman.fretbible.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.whoman.fretbible.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.R
import com.whoman.fretbible.ui.theme.*
import com.whoman.fretbible.ui.components.*
import kotlinx.coroutines.delay

@Composable
fun NameOnboardingScreen(onNameSaved: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var entered by remember { mutableStateOf(false) }
    val pulse = rememberInfiniteTransition(label = "namePulse")
    val scale by pulse.animateFloat(
        0.96f, 1.04f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing)),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(120)
        entered = true
    }

    Box(Modifier.fillMaxSize().background(Background)) {
        AnimatedBackground()

        AnimatedVisibility(
            visible = entered,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(96.dp).scale(scale),
                    shape = RoundedCornerShape(26.dp),
                    color = Color(0xFF0A0D0A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Lime.copy(alpha = .22f))
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_fret_bible_mark),
                        contentDescription = "Fret Bible",
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text("Welcome to Fret Bible.", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(7.dp))
                Text(
                    "What should we call you?",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(24) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Your name") },
                    placeholder = { Text("e.g. Hooman") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                Spacer(Modifier.height(12.dp))
                PrimaryAction(
                    text = "CONTINUE",
                    onClick = { if (name.trim().isNotEmpty()) onNameSaved(name.trim()) },
                    enabled = name.trim().isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))
                Text(
                    "You can change it later in Profile.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
