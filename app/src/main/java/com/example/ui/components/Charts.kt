package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.CurrencyOption
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.viewmodel.MonthlyBarPoint
import com.example.ui.viewmodel.TimelineChartPoint

@Composable
fun HeroTrendDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val lineColor = Color.White.copy(alpha = 0.88f)
        val softFill = Color.White.copy(alpha = 0.14f)

        val p1 = Offset(w * 0.05f, h * 0.85f)
        val p2 = Offset(w * 0.35f, h * 0.62f)
        val p3 = Offset(w * 0.56f, h * 0.72f)
        val p4 = Offset(w * 0.94f, h * 0.18f)

        val fillPath = Path().apply {
            moveTo(p1.x, h)
            lineTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y)
            lineTo(p4.x, h)
            close()
        }
        drawPath(path = fillPath, color = softFill)

        val linePath = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y)
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Arrowhead at p4
        val arrowPath = Path().apply {
            moveTo(p4.x - 10.dp.toPx(), p4.y + 1.dp.toPx())
            lineTo(p4.x, p4.y)
            lineTo(p4.x - 1.dp.toPx(), p4.y + 10.dp.toPx())
        }
        drawPath(
            path = arrowPath,
            color = lineColor,
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun MonthlyBarChart(
    bars: List<MonthlyBarPoint>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryBright = LocalLedgerColors.current.heroGradientStart
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val trackEmpty = MaterialTheme.colorScheme.surfaceContainerLow

    val rawMax = bars.maxOfOrNull { it.value } ?: 0.0
    val step = niceAxisStep(rawMax)
    val axisMax = step * 3.0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Y-Axis labels
            Column(
                modifier = Modifier
                    .width(38.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(axisMax, step * 2.0, step, 0.0).forEach { tick ->
                    Text(
                        text = CurrencyFormatter.formatAxisCompact(tick),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Grid + Bars
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val lineCount = 4
                    for (i in 0 until lineCount) {
                        val y = (size.height - 8.dp.toPx()) * (i / (lineCount - 1).toFloat()) + 4.dp.toPx()
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (bars.isNotEmpty()) {
                        val slotWidth = size.width / bars.size
                        val barWidth = (slotWidth * 0.48f).coerceAtMost(36.dp.toPx())
                        val maxBarHeight = size.height - 10.dp.toPx()

                        bars.forEachIndexed { idx, bar ->
                            val ratio = if (axisMax > 0.0) (bar.value / axisMax).toFloat().coerceIn(0f, 1f) else 0f
                            val barHeight = if (bar.value > 0.0) (maxBarHeight * ratio).coerceAtLeast(10.dp.toPx()) else 6.dp.toPx()
                            val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                            val top = size.height - barHeight - 4.dp.toPx()

                            val brush = if (bar.value <= 0.0) {
                                Brush.verticalGradient(listOf(trackEmpty, trackEmpty))
                            } else if (bar.isCurrent) {
                                Brush.verticalGradient(listOf(primaryBright, primary))
                            } else {
                                Brush.verticalGradient(listOf(primaryBright.copy(alpha = 0.85f), primary.copy(alpha = 0.85f)))
                            }

                            drawRoundRect(
                                brush = brush,
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis Month Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 38.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (bar.isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (bar.isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IncomeExpenseLineChart(
    points: List<TimelineChartPoint>,
    modifier: Modifier = Modifier
) {
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = LocalLedgerColors.current.chartLightBlue
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val rawMax = points.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0.0
    val step = niceAxisStep(rawMax)
    val axisMax = step * 3.0

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(incomeColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Income",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(20.dp))
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(expenseColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Expenses",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(38.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(axisMax, step * 2.0, step, 0.0).forEach { tick ->
                    Text(
                        text = CurrencyFormatter.formatAxisCompact(tick),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val padV = 10.dp.toPx()
                val usableH = size.height - padV * 2
                val padH = 12.dp.toPx()
                val usableW = (size.width - padH * 2).coerceAtLeast(1f)

                for (i in 0..3) {
                    val y = padV + usableH * (i / 3f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (points.isNotEmpty()) {
                    val stepX = if (points.size > 1) usableW / (points.size - 1) else 0f

                    fun yFor(value: Double): Float {
                        val ratio = if (axisMax > 0.0) (value / axisMax).toFloat().coerceIn(0f, 1f) else 0f
                        return padV + usableH * (1f - ratio)
                    }

                    val incomeOffsets = points.mapIndexed { idx, pt ->
                        Offset(padH + idx * stepX, yFor(pt.income))
                    }
                    val expenseOffsets = points.mapIndexed { idx, pt ->
                        Offset(padH + idx * stepX, yFor(pt.expense))
                    }

                    if (expenseOffsets.size > 1) {
                        val path = Path().apply {
                            moveTo(expenseOffsets.first().x, expenseOffsets.first().y)
                            expenseOffsets.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = expenseColor,
                            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                    if (incomeOffsets.size > 1) {
                        val path = Path().apply {
                            moveTo(incomeOffsets.first().x, incomeOffsets.first().y)
                            incomeOffsets.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = incomeColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    expenseOffsets.forEach { pt ->
                        drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = pt)
                        drawCircle(color = expenseColor, radius = 3.2.dp.toPx(), center = pt)
                    }
                    incomeOffsets.forEach { pt ->
                        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = incomeColor, radius = 3.6.dp.toPx(), center = pt)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 38.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BudgetRingOverview(
    spent: Double,
    budgetLimit: Double,
    currency: CurrencyOption,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.primaryContainer
    val remainingColor = LocalLedgerColors.current.chartLightBlue
    val overBudgetColor = LocalLedgerColors.current.expense

    val rawPercent = if (budgetLimit > 0.0) ((spent / budgetLimit) * 100.0).toInt() else 0
    val progressTarget = if (budgetLimit > 0.0) (spent / budgetLimit).toFloat().coerceIn(0f, 1f) else 0f
    val animatedSweep by animateFloatAsState(
        targetValue = progressTarget * 360f,
        animationSpec = spring(),
        label = "budgetRingSweep"
    )
    val remaining = (budgetLimit - spent).coerceAtLeast(0.0)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier.size(124.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(124.dp)) {
                val strokeWidth = 14.dp.toPx()
                val inset = strokeWidth / 2f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(inset, inset)

                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                if (animatedSweep > 0f) {
                    drawArc(
                        color = if (spent > budgetLimit && budgetLimit > 0.0) overBudgetColor else primary,
                        startAngle = -90f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            Text(
                text = "$rawPercent%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatCurrency(spent, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(remainingColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (spent > budgetLimit && budgetLimit > 0.0) "Over Budget" else "Remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatCurrency(
                            if (spent > budgetLimit && budgetLimit > 0.0) spent - budgetLimit else remaining,
                            currency
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun niceAxisStep(rawMax: Double): Double {
    if (rawMax <= 0.0) return 5000.0
    val rawStep = rawMax / 3.0
    return when {
        rawStep <= 500.0 -> 500.0
        rawStep <= 1000.0 -> 1000.0
        rawStep <= 2000.0 -> 2000.0
        rawStep <= 5000.0 -> 5000.0
        rawStep <= 10000.0 -> 10000.0
        rawStep <= 25000.0 -> 25000.0
        rawStep <= 50000.0 -> 50000.0
        else -> ((rawStep / 10000.0).toInt() + 1) * 10000.0
    }
}
