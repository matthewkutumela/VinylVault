package com.example.vinylvault.ui

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinylvault.data.AppDatabase
import com.example.vinylvault.data.Category
import com.example.vinylvault.data.CategoryWithCount
import com.example.vinylvault.data.VinylRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VinylViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val prefs =
        app.getSharedPreferences("vinylvault_achievements", Context.MODE_PRIVATE)

    val categories = db.categoryDao().getAllWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val records = db.recordDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Achievements the user has unlocked (saved on the phone)
    private val _unlocked = MutableStateFlow<Set<String>>(
        prefs.getStringSet("unlocked", emptySet())?.toSet() ?: emptySet()
    )
    val unlocked: StateFlow<Set<String>> = _unlocked

    // Achievements just earned, waiting to be shown in a congratulation message
    private val _celebration = MutableStateFlow<List<Achievement>>(emptyList())
    val celebration: StateFlow<List<Achievement>> = _celebration

    init {
        viewModelScope.launch {
            var firstCheck = true
            combine(
                db.recordDao().getAll(),
                db.categoryDao().getAllWithCount()
            ) { recordList, categoryList ->
                earnedIds(
                    recordCount = recordList.size,
                    anyGoalComplete = categoryList.any { it.owned >= it.goal }
                )
            }.collect { earned ->
                val newOnes = earned - _unlocked.value
                if (newOnes.isNotEmpty()) {
                    val all = _unlocked.value + newOnes
                    prefs.edit().putStringSet("unlocked", all).apply()
                    _unlocked.value = all
                    // The first check only catches up silently when the app opens
                    if (!firstCheck) {
                        _celebration.value = ALL_ACHIEVEMENTS.filter { it.id in newOnes }
                    }
                }
                firstCheck = false
            }
        }
    }

    fun dismissCelebration() {
        _celebration.value = emptyList()
    }

    // onResult gets null on success, or an error message
    fun addCategory(name: String, goal: Int, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                db.categoryDao().insert(Category(name = name, goal = goal))
                onResult(null)
            } catch (e: SQLiteConstraintException) {
                onResult("A category with that name already exists.")
            }
        }
    }

    fun updateCategory(id: Int, name: String, goal: Int, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                db.categoryDao().update(Category(id = id, name = name, goal = goal))
                onResult(null)
            } catch (e: SQLiteConstraintException) {
                onResult("A category with that name already exists.")
            }
        }
    }

    fun deleteCategory(c: CategoryWithCount, onResult: (String?) -> Unit) {
        if (c.owned > 0) {
            onResult("This category still has records. Move or delete them first.")
            return
        }
        viewModelScope.launch {
            db.categoryDao().delete(Category(id = c.id, name = c.name, goal = c.goal))
            onResult(null)
        }
    }

    fun addRecord(record: VinylRecord, onDone: () -> Unit) {
        viewModelScope.launch {
            db.recordDao().insert(record)
            onDone()
        }
    }

    fun updateRecord(record: VinylRecord, onDone: () -> Unit) {
        viewModelScope.launch {
            db.recordDao().update(record)
            onDone()
        }
    }

    fun deleteRecord(record: VinylRecord, onDone: () -> Unit) {
        viewModelScope.launch {
            db.recordDao().delete(record)
            record.photoPath?.let { java.io.File(it).delete() }
            onDone()
        }
    }
}