package com.example.vinylvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vinylvault.auth.AuthRepository
import com.example.vinylvault.ui.LoginScreen
import com.example.vinylvault.ui.RegisterScreen
import com.example.vinylvault.ui.theme.VinylVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VinylVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    var screen by remember {
        mutableStateOf(if (AuthRepository.isLoggedIn()) "home" else "login")
    }

    when (screen) {
        "login" -> LoginScreen(
            onLoggedIn = { screen = "home" },
            onGoToRegister = { screen = "register" }
        )
        "register" -> RegisterScreen(
            onRegistered = { screen = "home" },
            onBack = { screen = "login" }
        )
        else -> HomePlaceholder(onLogout = {
            AuthRepository.logout()
            screen = "login"
        })
    }
}

// Temporary screen. We replace it with the real Home screen later.
@Composable
fun HomePlaceholder(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome, ${AuthRepository.username()}!",
            style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLogout) { Text("Log out") }
    }
}