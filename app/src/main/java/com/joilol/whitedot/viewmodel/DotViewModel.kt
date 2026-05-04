package com.joilol.whitedot.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joilol.whitedot.model.GameData
import com.joilol.whitedot.R
import com.joilol.whitedot.model.local.GameDatabase
import com.joilol.whitedot.model.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.joilol.whitedot.model.persistance.RetrofitClient
import com.joilol.whitedot.model.persistance.UserSyncRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class DotUiState(
    val count: Float = 0f,
    val isAutoClicking: Boolean = false,
    val clickMultiplier: Float = 1f,
    val autoMultiplier: Float = 1f,
    val autoClickDelay: Long = 2000L,
    val critChance: Float = 0f,
    val offlineGains: Float = 0f,
    val showOfflineGains: Boolean = false
)

class DotViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val repository: GameRepository
    private val _uiState = MutableStateFlow(DotUiState())
    val uiState = _uiState.asStateFlow()
    private var autoClickJob: Job? = null

    private var soundPool: SoundPool? = null
    private var beepSoundId: Int = 0

    private val sensorManager: SensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastShakeTime: Long = 0

    init {
        val db = GameDatabase.getDatabase(application)
        repository = GameRepository(db.gameDao())

        viewModelScope.launch {
            val savedData = repository.gameData.first()
            savedData?.let { entity ->
                GameData.username = entity.username
                _uiState.update { it.copy(
                    count = entity.totalClicks,
                    clickMultiplier = entity.clickMultiplier,
                    autoMultiplier = entity.autoMultiplier,
                    autoClickDelay = entity.autoClickDelay,
                    critChance = entity.critChance
                ) }
                GameData.totalClicksEver = entity.totalClicks
                GameData.autoClicksCount = entity.autoClicks
                GameData.autoClickDelay = entity.autoClickDelay
                GameData.critChance = entity.critChance

                if (GameData.username.isNotEmpty()) {
                    checkOfflineGains(entity.autoMultiplier, entity.autoClickDelay)
                }
            }
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        val context = getApplication<Application>()
        soundPool?.let { sp ->
            beepSoundId = sp.load(context, R.raw.click, 1)
        }
    }

    fun onUserLoggedIn(username: String) {
        GameData.username = username
        viewModelScope.launch {
            val savedData = repository.gameData.first()
            val autoMult = savedData?.autoMultiplier ?: _uiState.value.autoMultiplier
            val autoDelay = savedData?.autoClickDelay ?: _uiState.value.autoClickDelay
            checkOfflineGains(autoMult, autoDelay)
            persistData() // Asegurar que el username se guarde localmente
        }
    }

    private fun checkOfflineGains(autoMultiplier: Float, autoClickDelay: Long) {
        if (autoMultiplier <= 0) return
        
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getLastLogout(GameData.username)
                if (response.isSuccessful) {
                    val lastLogoutStr = response.body()?.last_logout
                    if (!lastLogoutStr.isNullOrEmpty()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val cleanDate = lastLogoutStr.substringBefore(".")
                        val lastLogoutTime: Date? = try { sdf.parse(cleanDate) } catch(e: Exception) { null }
                        
                        val now = Date()
                        if (lastLogoutTime != null) {
                            val secondsOffline = (now.time - lastLogoutTime.time) / 1000
                            if (secondsOffline > 0) {
                                val clicksPerSecond = 1000f / autoClickDelay
                                val totalOfflineClicks = secondsOffline * clicksPerSecond * autoMultiplier
                                if (totalOfflineClicks > 0f) {
                                    _uiState.update { it.copy(
                                        count = it.count + totalOfflineClicks,
                                        offlineGains = totalOfflineClicks,
                                        showOfflineGains = true
                                    ) }
                                    GameData.totalClicksEver += totalOfflineClicks
                                    persistData()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun dismissOfflineGains() {
        _uiState.update { it.copy(showOfflineGains = false) }
    }

    fun incrementCount() {
        _uiState.update { current ->
            val isCrit = (0..100).random() < (current.critChance * 100)
            val multiplier = if (isCrit) current.clickMultiplier * 2 else current.clickMultiplier
            val next = current.count + multiplier
            GameData.totalClicksEver = next
            current.copy(count = next)
        }
        persistData()
        if (beepSoundId != 0) {
            soundPool?.play(beepSoundId, 1f, 1f, 0, 0, 1f)
        }
    }

    fun buyClickMultiplier() {
        val cost = 50f * _uiState.value.clickMultiplier
        if (_uiState.value.count >= cost) {
            _uiState.update { current ->
                val nextCount = current.count - cost
                val nextMult = current.clickMultiplier + 1.0f
                GameData.totalClicksEver = nextCount
                current.copy(count = nextCount, clickMultiplier = nextMult)
            }
            persistData()
        }
    }

    fun buyAutoMultiplier() {
        val cost = 100f * _uiState.value.autoMultiplier
        if (_uiState.value.count >= cost) {
            _uiState.update { current ->
                val nextCount = current.count - cost
                val nextMult = current.autoMultiplier + 1.0f
                GameData.totalClicksEver = nextCount
                current.copy(count = nextCount, autoMultiplier = nextMult)
            }
            persistData()
        }
    }

    fun buyAutoSpeed() {
        val currentDelay = _uiState.value.autoClickDelay
        if (currentDelay <= 100L) return
        val cost = 500f * (2000L / currentDelay)
        if (_uiState.value.count >= cost) {
            _uiState.update { current ->
                val nextCount = current.count - cost
                val nextDelay = (current.autoClickDelay * 0.8).toLong().coerceAtLeast(100L)
                GameData.totalClicksEver = nextCount
                GameData.autoClickDelay = nextDelay
                current.copy(count = nextCount, autoClickDelay = nextDelay)
            }
            if (_uiState.value.isAutoClicking) {
                autoClickJob?.cancel()
                startAutoClick()
            }
            persistData()
        }
    }

    fun buyCritChance() {
        val currentCrit = _uiState.value.critChance
        if (currentCrit >= 0.8f) return
        val cost = 1000f * (1 + currentCrit * 10)
        if (_uiState.value.count >= cost) {
            _uiState.update { current ->
                val nextCount = current.count - cost
                val nextCrit = current.critChance + 0.05f
                GameData.totalClicksEver = nextCount
                GameData.critChance = nextCrit
                current.copy(count = nextCount, critChance = nextCrit)
            }
            persistData()
        }
    }

    private fun persistData() {
        viewModelScope.launch {
            val current = _uiState.value
            repository.saveData(
                totalClicks = current.count,
                autoClicks = GameData.autoClicksCount,
                clickMult = current.clickMultiplier,
                autoMult = current.autoMultiplier,
                autoDelay = current.autoClickDelay,
                critChance = current.critChance,
                username = GameData.username
            )

            if (GameData.username.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val now = sdf.format(Date())
                    RetrofitClient.api.syncData(
                        UserSyncRequest(
                            username = GameData.username,
                            points = current.count.toInt(),
                            multiplier = current.clickMultiplier,
                            last_logout = now
                        )
                    )
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    fun toggleAutoClick() {
        val currentlyActive = _uiState.value.isAutoClicking
        _uiState.update { it.copy(isAutoClicking = !currentlyActive) }
        if (!currentlyActive) startAutoClick() else autoClickJob?.cancel()
    }

    private fun startAutoClick() {
        autoClickJob = viewModelScope.launch {
            while (true) {
                delay(_uiState.value.autoClickDelay)
                val increase = _uiState.value.autoMultiplier
                _uiState.update { current ->
                    val next = current.count + increase
                    GameData.totalClicksEver = next
                    GameData.autoClicksCount += increase
                    current.copy(count = next)
                }
                persistData()
            }
        }
    }

    fun stopAutoClick() { autoClickJob?.cancel() }
    fun startSensor() { accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) } }
    fun stopSensor() { sensorManager.unregisterListener(this) }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        soundPool?.release()
        soundPool = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val currentTime = System.currentTimeMillis()
            if (acceleration > 15f && currentTime - lastShakeTime > 1000) {
                lastShakeTime = currentTime
                toggleAutoClick()
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
