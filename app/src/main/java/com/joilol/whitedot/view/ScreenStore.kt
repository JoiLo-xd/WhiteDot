package com.joilol.whitedot.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joilol.whitedot.viewmodel.DotUiState

@Composable
fun ScreenStore(
    state: DotUiState,
    onBuyClickMultiplier: () -> Unit,
    onBuyAutoMultiplier: () -> Unit,
    onBuyAutoSpeed: () -> Unit,
    onBuyCritChance: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "TIENDA",
            color = Color.White,
            fontSize = 48.sp,
            modifier = Modifier.padding(top = 40.dp)
        )

        Text(
            text = "PUNTOS: ${state.count.toInt()}",
            color = Color.White,
            fontSize = 24.sp
        )

        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val clickCost = 50 * state.clickMultiplier
            Button(
                onClick = onBuyClickMultiplier,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.count >= clickCost
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MEJORAR CLICK (x${state.clickMultiplier.toInt()} -> x${state.clickMultiplier.toInt() + 1})")
                    Text("COSTE: ${clickCost.toInt()}", fontSize = 12.sp)
                }
            }

            val autoCost = 100 * state.autoMultiplier
            Button(
                onClick = onBuyAutoMultiplier,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.count >= autoCost
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MEJORAR AUTO-CLICK (x${state.autoMultiplier.toInt()} -> x${state.autoMultiplier.toInt() + 1})")
                    Text("COSTE: ${autoCost.toInt()}", fontSize = 12.sp)
                }
            }

            val speedCost = 500f * (2000L / state.autoClickDelay)
            val canBuySpeed = state.autoClickDelay > 100L
            Button(
                onClick = onBuyAutoSpeed,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.count >= speedCost && canBuySpeed
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val currentSpeed = 2000f / state.autoClickDelay
                    val nextSpeed = 2000f / (state.autoClickDelay * 0.8f)
                    Text(if (canBuySpeed) "VELOCIDAD AUTO (x${"%.1f".format(currentSpeed)} -> x${"%.1f".format(nextSpeed)})" else "VELOCIDAD MÁXIMA")
                    if (canBuySpeed) Text("COSTE: ${speedCost.toInt()}", fontSize = 12.sp)
                }
            }

            val critCost = 1000f * (1 + state.critChance * 10)
            val canBuyCrit = state.critChance < 0.8f
            Button(
                onClick = onBuyCritChance,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.count >= critCost && canBuyCrit
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (canBuyCrit) "PROB. CRÍTICO (${(state.critChance * 100).toInt()}% -> ${((state.critChance + 0.05f) * 100).toInt()}%)" else "CRÍTICO MÁXIMO (80%)")
                    if (canBuyCrit) Text("COSTE: ${critCost.toInt()}", fontSize = 12.sp)
                }
            }
        }

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RectangleShape,
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)
        ) {
            Text("VOLVER AL JUEGO")
        }
    }
}
