package com.example.ui.components

import com.example.data.local.CurrencyOption
import com.example.data.model.TransactionType
import com.example.ui.viewmodel.YearMonthPeriod
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {
    fun formatCurrency(amount: Double, currency: CurrencyOption = CurrencyOption.USD): String {
        val formatter = if (amount % 1.0 == 0.0) {
            DecimalFormat("#,##0")
        } else {
            DecimalFormat("#,##0.00")
        }
        return "${currency.symbol}${formatter.format(abs(amount))}"
    }

    fun formatSignedCurrency(
        amount: Double,
        type: TransactionType,
        currency: CurrencyOption = CurrencyOption.USD
    ): String {
        val prefix = if (type == TransactionType.PROFIT) "+" else "-"
        return "$prefix${formatCurrency(abs(amount), currency)}"
    }

    fun formatNetCurrency(amount: Double, currency: CurrencyOption = CurrencyOption.USD): String {
        val prefix = if (amount < 0) "-" else ""
        return "$prefix${formatCurrency(abs(amount), currency)}"
    }

    fun formatAxisCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "${(amount / 1_000_000).toInt()}M"
            amount >= 1_000 -> "${(amount / 1_000).toInt()}k"
            else -> "${amount.toInt()}"
        }
    }

    fun formatPeriodLabel(period: YearMonthPeriod?): String {
        if (period == null) return "All Time"
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, period.year)
            set(Calendar.MONTH, period.month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun formatDateShort(timestampMillis: Long): String {
        return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestampMillis))
    }
}
