package com.example.vinylvault.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "records",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("categoryId")]
)
data class VinylRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val artist: String,
    val title: String,
    val categoryId: Int,
    val description: String = "",
    val acquisitionDate: String? = null,
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)