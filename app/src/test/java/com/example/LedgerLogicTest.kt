package com.example

import com.example.data.local.BudgetDao
import com.example.data.local.TransactionDao
import com.example.data.model.BudgetCategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.LedgerRepository
import com.example.ui.viewmodel.LedgerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeBudgetDao : BudgetDao {
    private var nextId = 1L
    private val items = MutableStateFlow<List<BudgetCategoryEntity>>(emptyList())

    override fun observeAllBudgets(): Flow<List<BudgetCategoryEntity>> = items

    override suspend fun insertBudget(budget: BudgetCategoryEntity): Long {
        val id = if (budget.id == 0L) nextId++ else budget.id
        items.value = items.value.filterNot { it.id == id } + budget.copy(id = id)
        return id
    }

    override suspend fun updateBudget(budget: BudgetCategoryEntity) {
        items.value = items.value.map { if (it.id == budget.id) budget else it }
    }

    override suspend fun deleteBudgetById(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun deleteAllBudgets() {
        items.value = emptyList()
    }
}

private class FakeTransactionDao : TransactionDao {
    private var nextId = 1L
    private val items = MutableStateFlow<List<TransactionEntity>>(emptyList())

    override fun observeAllTransactions(): Flow<List<TransactionEntity>> =
        items.map { list -> list.sortedWith(compareByDescending<TransactionEntity> { it.timestamp }.thenByDescending { it.id }) }

    override suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val id = if (transaction.id == 0L) nextId++ else transaction.id
        items.value = items.value.filterNot { it.id == id } + transaction.copy(id = id)
        return id
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        items.value = items.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun deleteTransactionById(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun clearCategoryReference(categoryId: Long) {
        items.value = items.value.map { if (it.categoryId == categoryId) it.copy(categoryId = null) else it }
    }

    override suspend fun updateCategoryNameForTransactions(categoryId: Long, newName: String) {
        items.value = items.value.map { if (it.categoryId == categoryId) it.copy(categoryName = newName) else it }
    }

    override suspend fun deleteAllTransactions() {
        items.value = emptyList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerLogicTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: LedgerRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = LedgerRepository(FakeTransactionDao(), FakeBudgetDao())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tracking profit, expenses, and fixed category budgets computes net balance and progress`() = runTest(testDispatcher) {
        val viewModel = LedgerViewModel(repository)

        viewModel.saveBudgetCategory(
            name = "Food & Dining",
            monthlyLimit = 500.0,
            iconKey = "food"
        )
        advanceUntilIdle()

        val budgets = repository.observeBudgets().first()
        assertEquals(1, budgets.size)
        val foodId = budgets.first().id

        viewModel.saveTransaction(
            title = "Client Project",
            amount = 1500.0,
            type = TransactionType.PROFIT,
            categoryId = null,
            categoryName = "Freelance",
            note = "Milestone 1"
        )
        viewModel.saveTransaction(
            title = "Team Lunch",
            amount = 125.0,
            type = TransactionType.EXPENSE,
            categoryId = foodId,
            categoryName = "Food & Dining",
            note = ""
        )
        advanceUntilIdle()

        val state = viewModel.uiState.first { !it.isLoading && it.recentTransactions.size == 2 }
        assertEquals(1500.0, state.totalProfit, 0.01)
        assertEquals(125.0, state.totalExpense, 0.01)
        assertEquals(1375.0, state.netBalance, 0.01)
        assertEquals(1, state.categoryProgressList.size)
        val foodProgress = state.categoryProgressList.first()
        assertEquals(125.0, foodProgress.spent, 0.01)
        assertEquals(375.0, foodProgress.remaining, 0.01)
        assertTrue(!foodProgress.isOverBudget)
    }

    @Test
    fun `exceeding fixed category budget sets isOverBudget and negative remaining`() = runTest(testDispatcher) {
        val viewModel = LedgerViewModel(repository)

        viewModel.saveBudgetCategory(
            name = "Transport",
            monthlyLimit = 100.0,
            iconKey = "transport"
        )
        advanceUntilIdle()

        val transportId = repository.observeBudgets().first().first().id
        viewModel.saveTransaction(
            title = "Monthly Pass + Rides",
            amount = 140.0,
            type = TransactionType.EXPENSE,
            categoryId = transportId,
            categoryName = "Transport",
            note = ""
        )
        advanceUntilIdle()

        val state = viewModel.uiState.first { !it.isLoading && it.categoryProgressList.isNotEmpty() && it.recentTransactions.isNotEmpty() }
        val progress = state.categoryProgressList.first()
        assertEquals(140.0, progress.spent, 0.01)
        assertEquals(-40.0, progress.remaining, 0.01)
        assertTrue(progress.isOverBudget)
    }
}
