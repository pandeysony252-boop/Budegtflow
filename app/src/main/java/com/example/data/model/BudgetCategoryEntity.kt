package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_categories")
data class BudgetCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val monthlyLimit: Double,
    val iconKey: String = "other",
    val createdAt: Long = System.currentTimeMillis()
)
