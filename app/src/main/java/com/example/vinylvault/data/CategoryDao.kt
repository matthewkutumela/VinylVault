package com.example.vinylvault.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("""
        SELECT c.id, c.name, c.goal, COUNT(r.id) AS owned
        FROM categories c LEFT JOIN records r ON r.categoryId = c.id
        GROUP BY c.id ORDER BY c.name
    """)
    fun getAllWithCount(): Flow<List<CategoryWithCount>>

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAll(): Flow<List<Category>>
}