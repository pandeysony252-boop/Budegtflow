package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.local.CurrencyOption
import com.example.data.local.ThemeMode
import com.example.data.model.BudgetCategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class FilterState(
    val selectedPeriod: YearMonthPeriod? = YearMonthPeriod.current(),
    val reportRange: ReportRange = ReportRange.ONE_MONTH,
    val searchQuery: String = "",
    val typeFilter: TransactionType? = null,
    val selectedCategoryFilterId: Long? = null
)

class LedgerViewModel(
    private val repository: LedgerRepository,
    private val preferences: AppPreferences? = null
) : ViewModel() {

    private val filterState = MutableStateFlow(FilterState())

    private val prefsFlow = if (preferences != null) {
        combine(
            preferences.currency,
            preferences.themeMode,
            preferences.userName,
            preferences.budgetAlertsEnabled,
            preferences.overallMonthlyBudget
        ) { currency, themeMode, userName, alerts, overallBudget ->
            PrefsSnapshot(currency, themeMode, userName, alerts, overallBudget)
        }
    } else {
        MutableStateFlow(PrefsSnapshot())
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        repository.observeTransactions(),
        repository.observeBudgets(),
        filterState,
        prefsFlow
    ) { transactions, budgets, filters, prefs ->
        val activePeriod = filters.selectedPeriod
        val periodTransactions = if (activePeriod != null) {
            transactions.filter { activePeriod.contains(it.timestamp) }
        } else {
            transactions
        }

        val totalProfit = periodTransactions
            .filter { it.type == TransactionType.PROFIT }
            .sumOf { it.amount }

        val totalExpense = periodTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val netBalance = totalProfit - totalExpense
        val profitMarginPercent = if (totalProfit > 0.0) {
            ((netBalance / totalProfit) * 100.0)
        } else {
            0.0
        }

        val expenseByCategoryId = periodTransactions
            .filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
            .groupBy { it.categoryId!! }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val categoryProgressList = budgets.map { category ->
            val spent = expenseByCategoryId[category.id] ?: 0.0
            val remaining = category.monthlyLimit - spent
            val progress = if (category.monthlyLimit > 0.0) {
                (spent / category.monthlyLimit).toFloat()
            } else {
                0f
            }
            CategoryBudgetProgress(
                category = category,
                spent = spent,
                remaining = remaining,
                progress = progress.coerceAtLeast(0f),
                isOverBudget = spent > category.monthlyLimit
            )
        }

        val totalFixedBudget = budgets.sumOf { it.monthlyLimit }
        val totalBudgetedSpent = if (totalFixedBudget > 0.0) {
            categoryProgressList.sumOf { it.spent }
        } else {
            totalExpense
        }

        val filteredTransactions = periodTransactions.filter { tx ->
            val matchesType = filters.typeFilter == null || tx.type == filters.typeFilter
            val matchesCategory = filters.selectedCategoryFilterId == null ||
                tx.categoryId == filters.selectedCategoryFilterId
            val query = filters.searchQuery.trim()
            val matchesQuery = query.isEmpty() ||
                tx.title.contains(query, ignoreCase = true) ||
                tx.categoryName.contains(query, ignoreCase = true) ||
                tx.note.contains(query, ignoreCase = true)
            matchesType && matchesCategory && matchesQuery
        }

        // Last 5 months bars for Home "This Month" chart
        val anchorPeriod = activePeriod ?: YearMonthPeriod.current()
        val fiveMonths = buildList {
            var p = anchorPeriod
            repeat(5) {
                add(0, p)
                p = p.previous()
            }
        }
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val monthlyBars = fiveMonths.map { period ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, period.year)
                set(Calendar.MONTH, period.month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val monthTxs = transactions.filter { period.contains(it.timestamp) }
            val monthIncome = monthTxs.filter { it.type == TransactionType.PROFIT }.sumOf { it.amount }
            val monthExpense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val barValue = (monthIncome - monthExpense).coerceAtLeast(0.0).let { net ->
                if (net > 0.0) net else monthIncome.coerceAtLeast(monthExpense)
            }
            MonthlyBarPoint(
                period = period,
                label = monthFormat.format(cal.time),
                value = barValue,
                isCurrent = period == anchorPeriod
            )
        }

        // Reports metrics & timeline chart
        val reportTransactions = filterTransactionsByRange(transactions, filters.reportRange)
        val reportIncome = reportTransactions.filter { it.type == TransactionType.PROFIT }.sumOf { it.amount }
        val reportExpense = reportTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val reportNetProfit = reportIncome - reportExpense
        val reportMarginPercent = if (reportIncome > 0.0) {
            (reportNetProfit / reportIncome) * 100.0
        } else {
            0.0
        }
        val timelinePoints = buildTimelinePoints(reportTransactions, filters.reportRange)

        LedgerUiState(
            isLoading = false,
            selectedPeriod = filters.selectedPeriod,
            reportRange = filters.reportRange,
            totalProfit = totalProfit,
            totalExpense = totalExpense,
            netBalance = netBalance,
            profitMarginPercent = profitMarginPercent,
            totalFixedBudget = totalFixedBudget,
            overallMonthlyBudget = prefs.overallMonthlyBudget,
            totalBudgetedSpent = totalBudgetedSpent,
            categoryProgressList = categoryProgressList,
            recentTransactions = periodTransactions.take(5),
            filteredTransactions = filteredTransactions,
            allTransactions = transactions,
            budgets = budgets,
            monthlyBars = monthlyBars,
            reportIncome = reportIncome,
            reportExpense = reportExpense,
            reportNetProfit = reportNetProfit,
            reportMarginPercent = reportMarginPercent,
            reportTimelinePoints = timelinePoints,
            searchQuery = filters.searchQuery,
            typeFilter = filters.typeFilter,
            selectedCategoryFilterId = filters.selectedCategoryFilterId,
            currency = prefs.currency,
            themeMode = prefs.themeMode,
            userName = prefs.userName,
            budgetAlertsEnabled = prefs.budgetAlertsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LedgerUiState(isLoading = true)
    )

    private fun filterTransactionsByRange(
        transactions: List<TransactionEntity>,
        range: ReportRange
    ): List<TransactionEntity> {
        val months = range.months ?: return transactions
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -(months - 1))
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMillis = cal.timeInMillis
        return transactions.filter { it.timestamp >= startMillis }
    }

    private fun buildTimelinePoints(
        transactions: List<TransactionEntity>,
        range: ReportRange
    ): List<TimelineChartPoint> {
        if (range == ReportRange.ONE_MONTH) {
            val dayBuckets = listOf("1", "7", "14", "21", "28")
            val cal = Calendar.getInstance()
            val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
            val currentPeriod = YearMonthPeriod.current()
            val currentMonthTxs = transactions.filter { currentPeriod.contains(it.timestamp) }

            return dayBuckets.mapIndexed { idx, dayLabel ->
                val minDay = if (idx == 0) 1 else dayBuckets[idx - 1].toInt() + 1
                val maxDay = if (idx == dayBuckets.lastIndex) 31 else dayLabel.toInt()
                val bucketTxs = currentMonthTxs.filter { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                    val day = txCal.get(Calendar.DAY_OF_MONTH)
                    day in minDay..maxDay
                }
                TimelineChartPoint(
                    label = "$dayLabel $monthName",
                    income = bucketTxs.filter { it.type == TransactionType.PROFIT }.sumOf { it.amount },
                    expense = bucketTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                )
            }
        }

        val monthsCount = range.months ?: 6
        val periods = buildList {
            var p = YearMonthPeriod.current()
            repeat(monthsCount.coerceAtMost(6)) {
                add(0, p)
                p = p.previous()
            }
        }
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        return periods.map { period ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, period.year)
                set(Calendar.MONTH, period.month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val monthTxs = transactions.filter { period.contains(it.timestamp) }
            TimelineChartPoint(
                label = monthFormat.format(cal.time),
                income = monthTxs.filter { it.type == TransactionType.PROFIT }.sumOf { it.amount },
                expense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }
    }

    fun previousPeriod() {
        val current = filterState.value.selectedPeriod ?: YearMonthPeriod.current()
        filterState.value = filterState.value.copy(selectedPeriod = current.previous())
    }

    fun nextPeriod() {
        val current = filterState.value.selectedPeriod ?: YearMonthPeriod.current()
        filterState.value = filterState.value.copy(selectedPeriod = current.next())
    }

    fun toggleAllTime() {
        val current = filterState.value.selectedPeriod
        filterState.value = filterState.value.copy(
            selectedPeriod = if (current == null) YearMonthPeriod.current() else null
        )
    }

    fun setReportRange(range: ReportRange) {
        filterState.value = filterState.value.copy(reportRange = range)
    }

    fun setSearchQuery(query: String) {
        filterState.value = filterState.value.copy(searchQuery = query)
    }

    fun setTypeFilter(type: TransactionType?) {
        filterState.value = filterState.value.copy(typeFilter = type)
    }

    fun setCategoryFilter(categoryId: Long?) {
        filterState.value = filterState.value.copy(selectedCategoryFilterId = categoryId)
    }

    fun setCurrency(currency: CurrencyOption) {
        preferences?.setCurrency(currency)
    }

    fun setThemeMode(mode: ThemeMode) {
        preferences?.setThemeMode(mode)
    }

    fun setUserName(name: String) {
        preferences?.setUserName(name)
    }

    fun setBudgetAlertsEnabled(enabled: Boolean) {
        preferences?.setBudgetAlertsEnabled(enabled)
    }

    fun setOverallMonthlyBudget(amount: Double) {
        preferences?.setOverallMonthlyBudget(amount)
    }

    fun saveTransaction(
        existingId: Long = 0L,
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        categoryName: String,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.saveTransaction(
                TransactionEntity(
                    id = existingId,
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    categoryName = categoryName.trim(),
                    note = note.trim(),
                    timestamp = timestamp
                )
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.saveTransaction(transaction.copy(id = 0L))
        }
    }

    fun saveBudgetCategory(
        existingId: Long = 0L,
        name: String,
        monthlyLimit: Double,
        iconKey: String,
        createdAt: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.saveBudgetCategory(
                BudgetCategoryEntity(
                    id = existingId,
                    name = name.trim(),
                    monthlyLimit = monthlyLimit,
                    iconKey = iconKey,
                    createdAt = createdAt
                )
            )
        }
    }

    fun deleteBudgetCategory(id: Long) {
        viewModelScope.launch {
            if (filterState.value.selectedCategoryFilterId == id) {
                filterState.value = filterState.value.copy(selectedCategoryFilterId = null)
            }
            repository.deleteBudgetCategory(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            preferences?.setOverallMonthlyBudget(0.0)
        }
    }

    fun buildCsvExport(): String {
        val txs = uiState.value.allTransactions
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val sb = StringBuilder()
        sb.appendLine("Date,Title,Type,Category,Amount,Note")
        txs.forEach { tx ->
            val date = dateFormat.format(Date(tx.timestamp))
            val title = tx.title.replace("\"", "\"\"")
            val cat = tx.categoryName.replace("\"", "\"\"")
            val note = tx.note.replace("\"", "\"\"")
            sb.appendLine("\"$date\",\"$title\",\"${tx.type.name}\",\"$cat\",${tx.amount},\"$note\"")
        }
        return sb.toString()
    }
}

private data class PrefsSnapshot(
    val currency: CurrencyOption = CurrencyOption.USD,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val userName: String = "Personal Account",
    val budgetAlertsEnabled: Boolean = true,
    val overallMonthlyBudget: Double = 0.0
)
