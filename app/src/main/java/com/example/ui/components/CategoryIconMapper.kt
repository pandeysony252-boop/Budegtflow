package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

data class BudgetPreset(
    val name: String,
    val iconKey: String
)

object CategoryCatalog {
    val iconOptions = listOf(
        CategoryIconOption("food", "Dining", Icons.Rounded.Restaurant),
        CategoryIconOption("home", "Housing", Icons.Rounded.Home),
        CategoryIconOption("transport", "Transport", Icons.Rounded.DirectionsCar),
        CategoryIconOption("shopping", "Shopping", Icons.Rounded.ShoppingBag),
        CategoryIconOption("utilities", "Bills", Icons.Rounded.Bolt),
        CategoryIconOption("health", "Health", Icons.Rounded.Favorite),
        CategoryIconOption("entertainment", "Leisure", Icons.Rounded.Movie),
        CategoryIconOption("business", "Business", Icons.Rounded.Work),
        CategoryIconOption("travel", "Travel", Icons.Rounded.Flight),
        CategoryIconOption("education", "Learning", Icons.Rounded.School),
        CategoryIconOption("savings", "Reserve", Icons.Rounded.AccountBalanceWallet),
        CategoryIconOption("other", "General", Icons.Rounded.ReceiptLong)
    )

    val spendingPresets = listOf(
        BudgetPreset("Food & Dining", "food"),
        BudgetPreset("Housing & Rent", "home"),
        BudgetPreset("Transport", "transport"),
        BudgetPreset("Shopping", "shopping"),
        BudgetPreset("Utilities & Bills", "utilities"),
        BudgetPreset("Health & Fitness", "health"),
        BudgetPreset("Entertainment", "entertainment"),
        BudgetPreset("Business Ops", "business")
    )

    val profitSources = listOf(
        "Sales",
        "Salary",
        "Freelance",
        "Investments",
        "Side Hustle",
        "Refund",
        "Other"
    )

    fun iconFor(key: String?): ImageVector {
        return iconOptions.firstOrNull { it.key == key }?.icon ?: Icons.Rounded.ReceiptLong
    }
}
