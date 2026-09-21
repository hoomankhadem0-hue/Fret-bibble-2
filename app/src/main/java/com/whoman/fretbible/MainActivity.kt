package com.whoman.fretbible

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.whoman.fretbible.navigation.AppNavHost
import com.whoman.fretbible.profile.UserProfileStore
import com.whoman.fretbible.ui.screens.NameOnboardingScreen
import com.whoman.fretbible.ui.screens.SplashScreen
import com.whoman.fretbible.ui.theme.FretBibleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FretBibleTheme {
                val context = LocalContext.current
                var splash by remember { mutableStateOf(true) }
                var userName by remember { mutableStateOf(UserProfileStore.loadName(context)) }

                when {
                    splash -> SplashScreen(onFinished = { splash = false })
                    userName.isBlank() -> NameOnboardingScreen { name ->
                        UserProfileStore.saveName(context, name)
                        userName = name
                    }
                    else -> AppNavHost(
                        userName = userName,
                        onUserNameChanged = {
                            UserProfileStore.saveName(context, it)
                            userName = it
                        }
                    )
                }
            }
        }
    }
}
