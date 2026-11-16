package com.example.vinylvault.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vinylvault.data.VinylRecord
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatDate(millis: Long): String {
    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(millis)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(vm: VinylViewModel, onDone: () -> Unit) {
    val categories by vm.categories.collectAsState()

    var artist by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    var menuOpen by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState()

    val selectedName = categories.firstOrNull { it.id == categoryId }?.name

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDone) { Text("< Back") }
            Spacer(Modifier.width(8.dp))
            Text("Add record", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = artist, onValueChange = { artist = it },
            label = { Text("Artist *") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Album title *") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        if (categories.isEmpty()) {
            Text(
                "You have no categories yet. Go back and create one first (Home > Manage categories).",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Box {
                OutlinedButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(selectedName ?: "Choose category *") }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            onClick = { categoryId = c.id; menuOpen = false }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description, onValueChange = { description = it },
            label = { Text("Description (pressing, condition)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                dateMillis?.let { "Acquired: " + formatDate(it) }
                    ?: "Date of acquisition (optional)"
            )
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                when {
                    artist.isBlank() -> error = "Please enter the artist."
                    title.isBlank() -> error = "Please enter the album title."
                    categoryId == null -> error = "Please choose a category."
                    else -> {
                        error = null
                        vm.addRecord(
                            VinylRecord(
                                artist = artist.trim(),
                                title = title.trim(),
                                categoryId = categoryId!!,
                                description = description.trim(),
                                acquisitionDate = dateMillis?.let { formatDate(it) }
                            ),
                            onDone = onDone
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save record") }

        Spacer(Modifier.height(8.dp))
        Text("* required fields", style = MaterialTheme.typography.bodySmall)
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateMillis = pickerState.selectedDateMillis
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}