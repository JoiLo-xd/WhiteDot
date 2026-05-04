package com.joilol.whitedot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joilol.whitedot.model.User
import com.joilol.whitedot.util.NetworkUtils
import com.joilol.whitedot.model.GameData
import com.joilol.whitedot.model.persistance.LoginRequest
import com.joilol.whitedot.model.persistance.RetrofitClient
import com.joilol.whitedot.model.persistance.UserSyncRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue

enum class AppScreens{
    LOGIN,
    WELCOME,
    SETTINGS
}
// Estat inicial per la UI de Login.
// Els tres continguts en blanc.
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val message: String = "",
    val errorMsg: String = "",
    val screenState: AppScreens = AppScreens.LOGIN,
    val isOffline: Boolean = false
)

// sealed: només pot ser un objecte definit a la mateixa llibreria que LoginEvent.
// A dins de la interfície li diem que només accepti CloseApp com a LoginEvent vàlid.
sealed interface LoginEvent {
    data object  CloseApp : LoginEvent
}

// ViewModel és una classe de Kotling per aplicacions.
// Aquí estem creant una extensió d'aquesta classe.
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    // La lògica de la nostra App ha de tenir el Hashmap de clients:
    private val users = mutableMapOf<String, User>()

    // L'estat de l'aplicació és privat, només el viewmodel el pot canviar
    // Però la vista l'ha de poder veure.
    // Per assegurar que la vista veu però no canvia, creem _uiState i uiState.
    private val _uiState = MutableStateFlow(LoginUiState())
    // Iniciem tot en blanc amb el LoginUiState, però serà mutable.
    val uiState = _uiState.asStateFlow() // Aquesta, agafará sempre el valor de la privada


    // Configuració dels events que ens poden canviar la App IMPERATIVAMENT
    // Igual que els estats pero Shared en lloc de States.
    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        checkConnection()
    }

    private fun checkConnection() {
        val online = NetworkUtils.isOnline(getApplication())
        if (!online) {
            _uiState.value = _uiState.value.copy(
                errorMsg = "No se ha podido establecer conexión con el servidor, usando datos en local",
                isOffline = true
            )
            // Opcional: Podem saltar automàticament després d'un segon o directament
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000) // Donem temps a llegir el missatge
                _uiState.value = _uiState.value.copy(
                    username = "LocalUser",
                    screenState = AppScreens.WELCOME
                )
            }
        }
    }



    fun onUsernameChange(input: String) {
        _uiState.value = _uiState.value.copy(
            username = input, // Asegúrate de nombrar el parámetro
            message = "",
            errorMsg = ""
        )
    }

    fun onPasswordChange(input: String) {
        _uiState.value = _uiState.value.copy(password = input, message = "", errorMsg = "")
    }

    fun onRegisterClick() {
        val current = _uiState.value
        if (current.username.isBlank() || current.password.isBlank()) {
            _uiState.value = current.copy(errorMsg = "ERROR: Usuari i contrasenya obligatoris")
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.register(LoginRequest(current.username, current.password))
                if (response.isSuccessful) {
                    _uiState.value = current.copy(
                        message = "Usuari registrat correctament !!",
                        username = "",
                        password = "",
                        errorMsg = ""
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = current.copy(errorMsg = "ERROR: $errorBody", message = "")
                }
            } catch (e: Exception) {
                _uiState.value = current.copy(errorMsg = "ERROR de connexió: ${e.message}", message = "")
            }
        }
    }

    fun onLoginClick() {
        val current = _uiState.value
        if (current.username.isBlank() || current.password.isBlank()) {
            _uiState.value = current.copy(errorMsg = "ERROR: Usuari i contrasenya obligatoris")
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(current.username, current.password))
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    userResponse?.let {
                        GameData.username = it.username
                        GameData.totalClicksEver = it.points.toFloat()
                        GameData.autoClicksCount = it.points.toFloat()
                        // Podríamos sincronizar más campos aquí si el backend los tuviera
                    }
                    _uiState.value = current.copy(
                        message = "Login Exitós !!",
                        errorMsg = "",
                        password = "",
                        screenState = AppScreens.WELCOME
                    )
                } else {
                    _uiState.value = current.copy(message = "", errorMsg = "ERROR: Credencials invàlides !!")
                }
            } catch (e: Exception) {
                _uiState.value = current.copy(errorMsg = "ERROR de connexió: ${e.message}", message = "")
            }
        }
    }

    fun onLogoutClick(){
        _uiState.value = _uiState.value.copy(message = "", errorMsg = "", username = "", password ="", screenState = AppScreens.LOGIN)
    }

    fun onCloseClick(){
        viewModelScope.launch { _eventFlow.emit(LoginEvent.CloseApp) }
    }
}