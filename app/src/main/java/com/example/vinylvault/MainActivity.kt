package com.example.vinylvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vinylvault.auth.AuthRepository
import com.example.vinylvault.ui.CategoriesScreen
import com.example.vinylvault.ui.LoginScreen
import com.example.vinylvault.ui.RegisterScreen
import com.example.vinylvault.ui.VinylViewModel
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
        mutableStateOf(if (AuthRepository.isLoggedIn()) "main" else "login")
    }

    when (screen) {
        "login" -> LoginScreen(
            onLoggedIn = { screen = "main" },
            onGoToRegister = { screen = "register" }
        )
        "register" -> RegisterScreen(
            onRegistered = { screen = "main" },
            onBack = { screen = "login" }
        )
        else -> MainScaffold(onLogout = {
            AuthRepository.logout()
            screen = "login"
        })
    }
}

@Composable
fun MainScaffold(onLogout: () -> Unit) {
    val vm: VinylViewModel = viewModel()
    var tab by remember { mutableStateOf("home") }
    var showCategories by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == "home",
                    onClick = { tab = "home"; showCategories = false },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == "collection",
                    onClick = { tab = "collection" },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Collection") }
                )
                NavigationBarItem(
                    selected = tab == "progress",
                    onClick = { tab = "progress" },
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
                    label = { Text("Progress") }
                )
                NavigationBarItem(
                    selected = tab == "achievements",
                    onClick = { tab = "achievements" },
                    icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                    label = { Text("Awards") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                tab == "home" && showCategories ->
                    CategoriesScreen(vm = vm, onBack = { showCategories = false })
                tab == "home" ->
                    HomePlaceholder(
                        onCategories = { showCategories = true },
                        onLogout = onLogout
                    )
                else -> ComingSoon(tab)
            }
        }
    }
}

// Temporary screen. We replace it with the real Home screen later.
@Composable
fun HomePlaceholder(onCategories: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Welcome, ${AuthRepository.username()}!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onCategories) { Text("Manage categories") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onLogout) { Text("Log out") }
    }
}

@Composable
fun ComingSoon(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name screen coming soon")
    }
}