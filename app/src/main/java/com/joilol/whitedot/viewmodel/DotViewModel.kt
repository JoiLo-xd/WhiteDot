package com.joilol.whitedot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joilol.whitedot.model.GameData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DotUiState(
    val count: Int = 0,
    val isAutoClicking: Boolean = false
)

class DotViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DotUiState())
    val uiState = _uiState.asStateFlow()
    private var autoClickJob: Job? = null

    fun incrementCount() {
        _uiState.update { it.copy(count = it.count + 1) }
        GameData.totalClicksEver++ // Guardamos en el almacén global
    }

    fun toggleAutoClick() {
        val currentlyActive = _uiState.value.isAutoClicking
        _uiState.update { it.copy(isAutoClicking = !currentlyActive) }

        if (!currentlyActive) {
            autoClickJob = viewModelScope.launch {
                while (true) {
                    delay(2000)
                    incrementCount()
                    GameData.autoClicksCount++
                }
            }
        } else {
            autoClickJob?.cancel()
        }
    }
}