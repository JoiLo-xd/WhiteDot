package com.joilol.whitedot.viewmodel

import androidx.lifecycle.ViewModel
import com.joilol.whitedot.model.GameData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StatsUiState(
    val totalEver: Float = 0f,
    val totalAuto: Float = 0f,
    val autoDelay: Long = 2000L,
    val critChance: Float = 0f
)

class StatsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState = _uiState.asStateFlow()

    // Cargamos los datos actuales del almacén
    fun loadStats() {
        _uiState.value = StatsUiState(
            totalEver = GameData.totalClicksEver,
            totalAuto = GameData.autoClicksCount,
            autoDelay = GameData.autoClickDelay,
            critChance = GameData.critChance
        )
    }
}