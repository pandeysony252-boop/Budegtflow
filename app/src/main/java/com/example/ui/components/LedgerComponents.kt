package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CurrencyOption
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.viewmodel.CategoryBudgetProgress
import com.example.ui.viewmodel.YearMonthPeriod

@Composable
fun PeriodSelectorBar(
    selectedPeriod: YearMonthPeriod?,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onToggleAllTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalLedgerColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, semantic.cardBorder)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onPreviousPeriod,
                    enabled = selectedPeriod != null,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("previous_period_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Previous month"
                    )
                }
                Text(
                    text = CurrencyFormatter.formatPeriodLabel(selectedPeriod),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = onNextPeriod,
                    enabled = selectedPeriod != null,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("next_period_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Next month"
                    )
                }
            }
        }

        val isAllTime = selectedPeriod == null
        Surface(
            shape = RoundedCornerShape(50),
            color = if (isAllTime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            border = if (isAllTime) null else BorderStroke(1.dp, semantic.cardBorder),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onToggleAllTime)
                .testTag("toggle_all_time_button")
        ) {
            Text(
                text = if (isAllTime) "Monthly" else "All Time",
                style = MaterialTheme.typography.labelLarge,
                color = if (isAllTime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
fun BudgetProgressCard(
    item: CategoryBudgetProgress,
    currency: CurrencyOption,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val semantic = LocalLedgerColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = item.progress.coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "budgetProgress"
    )
    val barColor = when {
        item.isOverBudget -> semantic.expense
        item.progress >= 0.85f -> semantic.warning
        else -> MaterialTheme.colorScheme.primary
    }
    val statusBadgeBg = when {
        item.isOverBudget -> semantic.expenseContainer
        item.progress >= 0.85f -> semantic.warningContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val statusBadgeText = when {
        item.isOverBudget -> semantic.expense
        item.progress >= 0.85f -> semantic.warning
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, semantic.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("budget_card_${item.category.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryCatalog.iconFor(item.category.iconKey),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${CurrencyFormatter.formatCurrency(item.spent, currency)} of ${CurrencyFormatter.formatCurrency(item.category.monthlyLimit, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusBadgeBg
                ) {
                    val statusLabel = if (item.isOverBudget) {
                        "${CurrencyFormatter.formatCurrency(-item.remaining, currency)} over"
                    } else {
                        "${CurrencyFormatter.formatCurrency(item.remaining, currency)} left"
                    }
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusBadgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                if (onEditClick != null) {
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("edit_budget_${item.category.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit budget",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (animatedProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(barColor)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionRowCard(
    transaction: TransactionEntity,
    currency: CurrencyOption,
    iconKey: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalLedgerColors.current
    val isProfit = transaction.type == TransactionType.PROFIT
    val badgeBg = if (isProfit) semantic.profitContainer else semantic.expenseContainer
    val badgeTint = if (isProfit) semantic.profit else semantic.expense
    val amountColor = if (isProfit) semantic.profit else semantic.expense
    val iconVector = when {
        iconKey != null -> CategoryCatalog.iconFor(iconKey)
        isProfit -> when (transaction.categoryName) {
            "Freelance", "Salary", "Sales" -> Icons.Rounded.Work
            else -> Icons.Rounded.AccountBalanceWallet
        }
        else -> Icons.Rounded.ShoppingCart
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, semantic.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("transaction_card_${transaction.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = badgeTint,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = buildString {
                    append(CurrencyFormatter.formatDateShort(transaction.timestamp))
                    if (transaction.categoryName.isNotBlank()) {
                        append(" • ")
                        append(transaction.categoryName)
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = CurrencyFormatter.formatSignedCurrency(transaction.amount, transaction.type, currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
