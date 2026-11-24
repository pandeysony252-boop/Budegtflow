package com.example.ui.viewmodel

import com.example.data.local.CurrencyOption
import com.example.data.local.ThemeMode
import com.example.data.model.BudgetCategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.util.Calendar

enum class ReportRange(val label: String, val months: Int?) {
    ONE_MONTH("1M", 1),
    THREE_MONTHS("3M", 3),
    SIX_MONTHS("6M", 6),
    ONE_YEAR("1Y", 12),
    ALL("All", null)
}

data class MonthlyBarPoint(
    val period: YearMonthPeriod,
    val label: String,
    val value: Double,
    val isCurrent: Boolean
)

data class TimelineChartPoint(
    val label: String,
    val income: Double,
    val expense: Double
)

data class YearMonthPeriod(
    val year: Int,
    val month: Int // 0-indexed (Calendar.JANUARY .. Calendar.DECEMBER)
) {
    fun previous(): YearMonthPeriod {
        return if (month == Calendar.JANUARY) {
            YearMonthPeriod(year - 1, Calendar.DECEMBER)
        } else {
            YearMonthPeriod(year, month - 1)
        }
    }

    fun next(): YearMonthPeriod {
        return if (month == Calendar.DECEMBER) {
            YearMonthPeriod(year + 1, Calendar.JANUARY)
        } else {
            YearMonthPeriod(year, month + 1)
        }
    }

    fun contains(timestampMillis: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        return cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
    }

    fun startMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    companion object {
        fun current(): YearMonthPeriod {
            val cal = Calendar.getInstance()
            return YearMonthPeriod(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH)
            )
        }
    }
}

data class CategoryBudgetProgress(
    val category: BudgetCategoryEntity,
    val spent: Double,
    val remaining: Double,
    val progress: Float,
    val isOverBudget: Boolean
)

data class LedgerUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: YearMonthPeriod? = YearMonthPeriod.current(),
    val reportRange: ReportRange = ReportRange.ONE_MONTH,
    val totalProfit: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val totalFixedBudget: Double = 0.0,
    val overallMonthlyBudget: Double = 0.0,
    val totalBudgetedSpent: Double = 0.0,
    val categoryProgressList: List<CategoryBudgetProgress> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val budgets: List<BudgetCategoryEntity> = emptyList(),
    val monthlyBars: List<MonthlyBarPoint> = emptyList(),
    val reportIncome: Double = 0.0,
    val reportExpense: Double = 0.0,
    val reportNetProfit: Double = 0.0,
    val reportMarginPercent: Double = 0.0,
    val reportTimelinePoints: List<TimelineChartPoint> = emptyList(),
    val searchQuery: String = "",
    val typeFilter: TransactionType? = null,
    val selectedCategoryFilterId: Long? = null,
    val currency: CurrencyOption = CurrencyOption.USD,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val userName: String = "Personal Account",
    val budgetAlertsEnabled: Boolean = true
)
