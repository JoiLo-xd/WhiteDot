package com.joilol.whitedot.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape // Necesario para los cuadrados
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joilol.whitedot.viewmodel.DotUiState
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect

@Composable
fun ScreenDot(
    state: DotUiState,
    onDotClick: () -> Unit,
    onAutoClickClick: () -> Unit,
    onBack: () -> Unit,
    onStatsClick:() -> Unit,
    onStoreClick: () -> Unit,
    onDismissOfflineGains: () -> Unit = {},
    onStartSensor: () -> Unit = {},
    onStopSensor: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    DisposableEffect(Unit) {
        onStartSensor()
        onDispose {
            onStopSensor()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (state.showOfflineGains) {
            AlertDialog(
                onDismissRequest = { onDismissOfflineGains() },
                confirmButton = {
                    TextButton(onClick = onDismissOfflineGains) {
                        Text("¡GENIAL!", color = Color.Black)
                    }
                },
                title = { Text("¡Bienvenido de nuevo!") },
                text = { Text("Mientras no estabas, has ganado ${state.offlineGains.toInt()} puntos.") },
                containerColor = Color.White,
                textContentColor = Color.Black,
                titleContentColor = Color.Black
            )
        }

        Text(
            text = "TOTAL: ${state.count}",
            color = Color.White,
            fontSize = 48.sp, // Un poco más grande
            modifier = Modifier.padding(top = 60.dp)
        )

        Button(
            onClick = {
                onDotClick()
                scope.launch {
                    scale.snapTo(0.8f)
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            },
            // size(200.dp) fuerza que ancho y alto sean iguales -> Cuadrado
            modifier = Modifier
                .size(220.dp)
                .scale(scale.value),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            // RectangleShape quita las esquinas redondeadas
            shape = RectangleShape
        ) {
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Button(
                onClick = onAutoClickClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isAutoClicking) Color.DarkGray else Color.White,
                    contentColor = if (state.isAutoClicking) Color.White else Color.Black
                ),
                shape = RectangleShape, // Rectangular
                modifier = Modifier.fillMaxWidth() // Que ocupe todo el ancho
            ) {
                Text(
                    text = if (state.isAutoClicking) "AUTO-CLICK: ACTIVO" else "ACTIVAR AUTO-CLICK (2s)",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onBack,
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().background(Color.Black) // Fondo negro explícito
            ) {
                Text("VOLVER AL INICIO", color = Color.Gray)
            }
            Button(
                onClick = onStatsClick, // <--- Nueva acción
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("VER STATS")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStoreClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("TIENDA")
            }
        }
    }
}