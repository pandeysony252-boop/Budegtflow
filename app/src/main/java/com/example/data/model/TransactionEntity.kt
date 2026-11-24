package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long? = null,
    val categoryName: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
