package com.joilol.whitedot.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joilol.whitedot.viewmodel.StatsUiState

@Composable
fun ScreenStats(
    state: StatsUiState,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ESTADÍSTICAS", color = Color.White, fontSize = 32.sp)
        Spacer(Modifier.height(40.dp))

        Text("Clics totales: ${state.totalEver.toInt()}", color = Color.White, fontSize = 20.sp)
        Text("Clics automáticos: ${state.totalAuto.toInt()}", color = Color.White, fontSize = 20.sp)
        Text("Velocidad: x${"%.1f".format(2000f / state.autoDelay)}", color = Color.White, fontSize = 20.sp)
        Text("Prob. Crítico: ${(state.critChance * 100).toInt()}%", color = Color.White, fontSize = 20.sp)

        Spacer(Modifier.height(60.dp))

        Button(
            onClick = onBack,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("VOLVER AL PUNTO")
        }
    }
}