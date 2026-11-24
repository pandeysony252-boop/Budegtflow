package com.example.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CurrencyOption(
    val code: String,
    val symbol: String,
    val displayLabel: String
) {
    INR("INR", "₹", "INR (₹)"),
    USD("USD", "$", "USD ($)"),
    EUR("EUR", "€", "EUR (€)"),
    GBP("GBP", "£", "GBP (£)")
}

enum class ThemeMode(val label: String) {
    LIGHT("Light (White & Blue)"),
    DARK("Dark"),
    SYSTEM("System Default")
}

class AppPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "profit_track_prefs",
        Context.MODE_PRIVATE
    )

    private val _currency = MutableStateFlow(loadCurrency())
    val currency: StateFlow<CurrencyOption> = _currency.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _userName = MutableStateFlow(
        prefs.getString(KEY_USER_NAME, "Personal Account") ?: "Personal Account"
    )
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _budgetAlertsEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_ALERTS_ENABLED, true)
    )
    val budgetAlertsEnabled: StateFlow<Boolean> = _budgetAlertsEnabled.asStateFlow()

    private val _overallMonthlyBudget = MutableStateFlow(
        prefs.getFloat(KEY_OVERALL_BUDGET, 0f).toDouble()
    )
    val overallMonthlyBudget: StateFlow<Double> = _overallMonthlyBudget.asStateFlow()

    private fun loadCurrency(): CurrencyOption {
        val code = prefs.getString(KEY_CURRENCY, CurrencyOption.USD.code)
        return CurrencyOption.entries.firstOrNull { it.code == code } ?: CurrencyOption.USD
    }

    private fun loadThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name)
        return ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.LIGHT
    }

    fun setCurrency(option: CurrencyOption) {
        prefs.edit().putString(KEY_CURRENCY, option.code).apply()
        _currency.value = option
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setUserName(name: String) {
        val clean = name.trim().ifEmpty { "Personal Account" }
        prefs.edit().putString(KEY_USER_NAME, clean).apply()
        _userName.value = clean
    }

    fun setBudgetAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALERTS_ENABLED, enabled).apply()
        _budgetAlertsEnabled.value = enabled
    }

    fun setOverallMonthlyBudget(amount: Double) {
        prefs.edit().putFloat(KEY_OVERALL_BUDGET, amount.toFloat()).apply()
        _overallMonthlyBudget.value = amount
    }

    private companion object {
        const val KEY_CURRENCY = "currency_code"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_USER_NAME = "user_name"
        const val KEY_ALERTS_ENABLED = "budget_alerts_enabled"
        const val KEY_OVERALL_BUDGET = "overall_monthly_budget"
    }
}
