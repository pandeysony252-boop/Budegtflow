package com.example.data.repository

import com.example.data.local.BudgetDao
import com.example.data.local.TransactionDao
import com.example.data.model.BudgetCategoryEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class LedgerRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    fun observeTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.observeAllTransactions()

    fun observeBudgets(): Flow<List<BudgetCategoryEntity>> =
        budgetDao.observeAllBudgets()

    suspend fun saveTransaction(transaction: TransactionEntity): Long {
        return if (transaction.id == 0L) {
            transactionDao.insertTransaction(transaction)
        } else {
            transactionDao.updateTransaction(transaction)
            transaction.id
        }
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun saveBudgetCategory(budget: BudgetCategoryEntity): Long {
        return if (budget.id == 0L) {
            budgetDao.insertBudget(budget)
        } else {
            budgetDao.updateBudget(budget)
            transactionDao.updateCategoryNameForTransactions(budget.id, budget.name)
            budget.id
        }
    }

    suspend fun deleteBudgetCategory(id: Long) {
        transactionDao.clearCategoryReference(id)
        budgetDao.deleteBudgetById(id)
    }

    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        budgetDao.deleteAllBudgets()
    }
}
