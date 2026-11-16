package com.example.vinylvault.ui

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinylvault.data.AppDatabase
import com.example.vinylvault.data.Category
import com.example.vinylvault.data.CategoryWithCount
import com.example.vinylvault.data.VinylRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VinylViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)

    val categories = db.categoryDao().getAllWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val records = db.recordDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            onDone()
        }
    }
}