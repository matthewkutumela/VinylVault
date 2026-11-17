package com.example.vinylvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vinylvault.auth.AuthRepository
import com.example.vinylvault.ui.AchievementsScreen
import com.example.vinylvault.ui.AddRecordScreen
import com.example.vinylvault.ui.CategoriesScreen
import com.example.vinylvault.ui.CollectionScreen
import com.example.vinylvault.ui.HomeScreen
import com.example.vinylvault.ui.LoginScreen
import com.example.vinylvault.ui.ProgressScreen
import com.example.vinylvault.ui.RecordDetailScreen
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
    val records by vm.records.collectAsState()
    val categories by vm.categories.collectAsState()
    val celebration by vm.celebration.collectAsState()

    var tab by remember { mutableStateOf("home") }
    var showCategories by remember { mutableStateOf(false) }
    // Collection screens: "list", "add", "detail", "edit"
    var collectionScreen by remember { mutableStateOf("list") }
    var selectedId by remember { mutableStateOf<Int?>(null) }

    val selected = records.firstOrNull { it.id == selectedId }
    val collectionNow =
        if ((collectionScreen == "detail" || collectionScreen == "edit") && selected == null)
            "list"
        else
            collectionScreen

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == "home",
                    onClick = { tab = "home"; showCategories = false; collectionScreen = "list" },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == "collection",
                    onClick = { tab = "collection"; showCategories = false; collectionScreen = "list" },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Collection") }
                )
                NavigationBarItem(
                    selected = tab == "progress",
                    onClick = { tab = "progress"; showCategories = false; collectionScreen = "list" },
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
                    label = { Text("Progress") }
                )
                NavigationBarItem(
                    selected = tab == "achievements",
                    onClick = { tab = "achievements"; showCategories = false; collectionScreen = "list" },
                    icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                    label = { Text("Awards") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (tab == "home") {
                if (showCategories) {
                    CategoriesScreen(vm = vm, onBack = { showCategories = false })
                } else {
                    HomeScreen(
                        vm = vm,
                        onCategories = { showCategories = true },
                        onAddRecord = { tab = "collection"; collectionScreen = "add" },
                        onLogout = onLogout
                    )
                }
            } else if (tab == "collection") {
                when (collectionNow) {
                    "add" -> AddRecordScreen(
                        vm = vm,
                        existing = null,
                        onDone = { collectionScreen = "list" }
                    )
                    "edit" -> AddRecordScreen(
                        vm = vm,
                        existing = selected,
                        onDone = { collectionScreen = "detail" }
                    )
                    "detail" -> if (selected != null) {
                        RecordDetailScreen(
                            record = selected,
                            categoryName = categories
                                .firstOrNull { it.id == selected.categoryId }?.name ?: "-",
                            onBack = { collectionScreen = "list" },
                            onEdit = { collectionScreen = "edit" },
                            onDelete = {
                                vm.deleteRecord(selected) { collectionScreen = "list" }
                            }
                        )
                    }
                    else -> CollectionScreen(
                        vm = vm,
                        onAdd = { collectionScreen = "add" },
                        onOpen = { id -> selectedId = id; collectionScreen = "detail" }
                    )
                }
            } else if (tab == "progress") {
                ProgressScreen(vm = vm)
            } else {
                AchievementsScreen(vm = vm)
            }
        }
    }

    // Congratulation message, shown from any screen the moment something is earned
    if (celebration.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { vm.dismissCelebration() },
            title = { Text("Achievement unlocked!") },
            text = {
                Text(
                    celebration.joinToString("\n\n") { "★ ${it.name}\n${it.description}" }
                )
            },
            confirmButton = {
                TextButton(onClick = { vm.dismissCelebration() }) { Text("Nice!") }
            }
        )
    }
}