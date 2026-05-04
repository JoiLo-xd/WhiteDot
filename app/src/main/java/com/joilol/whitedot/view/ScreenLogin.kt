package com.joilol.whitedot.view
import android.text.style.BackgroundColorSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.joilol.whitedot.viewmodel.LoginUiState

// COM PINTO LA PANTALLA?
@Composable
fun ScreenLogin(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCloseClick: () -> Unit,
){
    Surface (
        modifier = Modifier.fillMaxSize(),
        color = Color.Black

    ){

    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            color = Color.White,
            text = state.message,
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
                .fillMaxWidth()
        )

        OutlinedTextField( value = state.username, onValueChange = onUsernameChange, label = { Text("User") })
        OutlinedTextField( value = state.password, onValueChange = onPasswordChange, label = { Text("Pass") })

        Row {
            Button(
                onClick = onRegisterClick,
                enabled = !state.isOffline,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isOffline) Color.Gray else Color.White,
                    contentColor = Color.Black,
                )
            ) { Text("Crear usuari") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onLoginClick,
                enabled = !state.isOffline,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isOffline) Color.Gray else Color.White,
                    contentColor = Color.Black,
                )
                ) { Text("Entrar") }
        }

        Button(onClick = onCloseClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,

            )

            ) {Text("Tancar")}

        Text(
            color = Color.White,
            text = state.errorMsg,
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
                .fillMaxWidth()
        )
    }
    }
}


