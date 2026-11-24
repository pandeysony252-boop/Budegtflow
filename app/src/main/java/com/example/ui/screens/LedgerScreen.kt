package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.PeriodSelectorBar
import com.example.ui.components.TransactionRowCard
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.viewmodel.LedgerUiState

@Composable
fun LedgerScreen(
    uiState: LedgerUiState,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onToggleAllTime: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilterChange: (TransactionType?) -> Unit,
    onCategoryFilterChange: (Long?) -> Unit,
    onEditTransactionClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalLedgerColors.current
    val budgetIconMap = uiState.budgets.associate { it.id to it.iconKey }
    var searchExpanded by remember { mutableStateOf(uiState.searchQuery.isNotEmpty()) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val contentMaxWidth = if (maxWidth >= 700.dp) 900.dp else 600.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = contentMaxWidth)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Centered "Transactions" Header + Search Icon matching Screen 3
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.nav_transactions),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = {
                            if (searchExpanded && uiState.searchQuery.isNotEmpty()) {
                                onSearchQueryChange("")
                            }
                            searchExpanded = !searchExpanded
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (searchExpanded) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.cd_search),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(visible = searchExpanded) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search transactions, categories, notes...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.cd_clear_search)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ledger_search_input")
                    )
                }
            }

            // Pill Filter Row matching Screen 3: All | Income | Expenses
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tabs = listOf(
                        Triple(null, "All", "filter_all_chip"),
                        Triple(TransactionType.PROFIT, "Income", "filter_profit_chip"),
                        Triple(TransactionType.EXPENSE, "Expenses", "filter_expense_chip")
                    )
                    tabs.forEach { (typeValue, label, tag) ->
                        val selected = uiState.typeFilter == typeValue
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (selected) null else BorderStroke(1.dp, semantic.cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(50))
                                .clickable { onTypeFilterChange(typeValue) }
                                .testTag(tag)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.budgets.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.budgets.forEach { budget ->
                            val isSelected = uiState.selectedCategoryFilterId == budget.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onCategoryFilterChange(if (isSelected) null else budget.id)
                                },
                                label = { Text(budget.name) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("filter_category_${budget.id}")
                            )
                        }
                    }
                }
            }

            item {
                PeriodSelectorBar(
                    selectedPeriod = uiState.selectedPeriod,
                    onPreviousPeriod = onPreviousPeriod,
                    onNextPeriod = onNextPeriod,
                    onToggleAllTime = onToggleAllTime
                )
            }

            if (uiState.filteredTransactions.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, semantic.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap the + button at the bottom to record an income or expense entry.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredTransactions,
                    key = { "ledger_tx_${it.id}" }
                ) { transaction ->
                    TransactionRowCard(
                        transaction = transaction,
                        currency = uiState.currency,
                        iconKey = transaction.categoryId?.let { budgetIconMap[it] },
                        onClick = { onEditTransactionClick(transaction) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
