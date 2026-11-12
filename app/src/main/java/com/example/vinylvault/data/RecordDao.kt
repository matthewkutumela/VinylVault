package com.example.vinylvault.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: VinylRecord): Long

    @Update
    suspend fun update(record: VinylRecord)

    @Delete
    suspend fun delete(record: VinylRecord)

    @Query("SELECT * FROM records ORDER BY createdAt DESC")
    fun getAll(): Flow<List<VinylRecord>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: Int): VinylRecord?

    @Query("SELECT COUNT(*) FROM records")
    fun countAll(): Flow<Int>
}