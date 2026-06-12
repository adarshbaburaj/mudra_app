package com.ada.mudra

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ada.mudra.ui.components.LocalHapticsEnabled
import com.ada.mudra.ui.navigation.MudraNavHost
import com.ada.mudra.ui.theme.MudraTheme

// AppCompatActivity (not ComponentActivity) so per-app language switching
// via AppCompatDelegate works on every supported Android version.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MudraViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            MudraTheme {
                CompositionLocalProvider(LocalHapticsEnabled provides settings.hapticsEnabled) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MudraNavHost(
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .safeDrawingPadding(),
                        )
                    }
                }
            }
        }
    }
}
