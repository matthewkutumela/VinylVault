package com.example.vinylvault.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CollectionScreen(vm: VinylViewModel, onAdd: () -> Unit, onOpen: (Int) -> Unit) {
    val records by vm.records.collectAsState()
    val categories by vm.categories.collectAsState()
    var query by remember { mutableStateOf("") }

    val categoryNames = categories.associate { it.id to it.name }

    val shown = records.filter { r ->
        query.isBlank() ||
                r.artist.contains(query, ignoreCase = true) ||
                r.title.contains(query, ignoreCase = true) ||
                (categoryNames[r.categoryId] ?: "").contains(query, ignoreCase = true)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("My Collection", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query, onValueChange = { query = it },
                label = { Text("Search artist, title or category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            if (records.isEmpty()) {
                Text("No records yet. Tap + to add your first one.")
            } else if (shown.isEmpty()) {
                Text("No records match your search.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(shown, key = { it.id }) { r ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(r.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PhotoImage(
                                path = r.photoPath,
                                modifier = Modifier.size(72.dp),
                                maxSize = 200
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(r.title, style = MaterialTheme.typography.titleMedium)
                                Text(r.artist, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Category: " + (categoryNames[r.categoryId] ?: "-"),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                r.acquisitionDate?.let {
                                    Text("Acquired: $it", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Filled.Add, contentDescription = "Add record") }
    }
}