package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.api.ApiKeyConfigScreen
import com.example.ui.assistant.AssistantScreen
import com.example.ui.assistant.AssistantViewModel
import com.example.ui.assistant.Screen
import com.example.ui.history.HistoryScreen
import com.example.ui.memory.MemoryScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: AssistantViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                // System back handler across all sub-screens
                BackHandler(enabled = currentScreen != Screen.ASSISTANT) {
                    viewModel.onBackNavigation()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = JarvisBackground
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { target ->
                        when (target) {
                            Screen.ASSISTANT -> AssistantScreen(viewModel = viewModel)
                            Screen.API_CONFIG -> ApiKeyConfigScreen(viewModel = viewModel)
                            Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            Screen.MEMORY -> MemoryScreen(viewModel = viewModel)
                            Screen.HISTORY -> HistoryScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
