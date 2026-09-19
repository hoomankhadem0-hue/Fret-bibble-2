package com.whoman.fretbible
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.whoman.fretbible.navigation.AppNavHost
import com.whoman.fretbible.ui.theme.FretBibleTheme
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{FretBibleTheme{AppNavHost()}}}}
