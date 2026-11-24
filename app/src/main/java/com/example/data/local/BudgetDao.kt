package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BudgetCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_categories ORDER BY createdAt ASC")
    fun observeAllBudgets(): Flow<List<BudgetCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetCategoryEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetCategoryEntity)

    @Query("DELETE FROM budget_categories WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)

    @Query("DELETE FROM budget_categories")
    suspend fun deleteAllBudgets()
}
