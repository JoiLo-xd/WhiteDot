package com.joilol.whitedot

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.joilol.whitedot.navigation.AppNavigation
import com.joilol.whitedot.ui.theme.WhiteDotTheme

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Iniciar música de fondo
        mediaPlayer = MediaPlayer.create(this, R.raw.jazzfondo)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        enableEdgeToEdge()
        setContent {
            WhiteDotTheme {
                AppNavigation (
                    onCloseApp = ::finalitzarAplicacio
                )
            }
        }
    }
    private fun finalitzarAplicacio() {
        this.finish();
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
