package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.local.ThemeMode

@Immutable
data class LedgerSemanticColors(
    val profit: Color,
    val profitContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val heroBadgePositive: Color,
    val cardBorder: Color,
    val chartLightBlue: Color
)

val LocalLedgerColors = staticCompositionLocalOf {
    LedgerSemanticColors(
        profit = IncomeGreenLight,
        profitContainer = IncomeGreenContainerLight,
        expense = ExpenseRedLight,
        expenseContainer = ExpenseRedContainerLight,
        warning = WarningAmberLight,
        warningContainer = WarningAmberContainerLight,
        heroGradientStart = BlueHeroStart,
        heroGradientEnd = BlueHeroEnd,
        heroBadgePositive = HeroBadgePositive,
        cardBorder = LightOutlineVariant,
        chartLightBlue = SkySecondary
    )
}

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueSoftContainer,
    onPrimaryContainer = BlueOnSoftContainer,
    secondary = SkySecondary,
    onSecondary = Color.White,
    secondaryContainer = SkySoftContainer,
    onSecondaryContainer = SkyOnSoftContainer,
    tertiary = IncomeGreenLight,
    onTertiary = Color.White,
    tertiaryContainer = IncomeGreenContainerLight,
    onTertiaryContainer = Color(0xFF064E3B),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceLow,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurfaceLow,
    surfaceContainer = Color(0xFFF2F5FE),
    surfaceContainerHigh = LightSurfaceHigh,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = ExpenseRedLight,
    onError = Color.White,
    errorContainer = ExpenseRedContainerLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBluePrimary,
    onPrimary = DarkBlueOnPrimary,
    primaryContainer = DarkBlueContainer,
    onPrimaryContainer = DarkBlueOnContainer,
    secondary = Color(0xFF7DD3FC),
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF133A63),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = IncomeGreenDark,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = IncomeGreenContainerDark,
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceLow,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = Color(0xFF172135),
    surfaceContainerHigh = DarkSurfaceHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = ExpenseRedDark,
    onError = Color(0xFF450A0A),
    errorContainer = ExpenseRedContainerDark,
    onErrorContainer = Color(0xFFFEE2E2)
)

@Composable
fun RedLedgerTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) {
        LedgerSemanticColors(
            profit = IncomeGreenDark,
            profitContainer = IncomeGreenContainerDark,
            expense = ExpenseRedDark,
            expenseContainer = ExpenseRedContainerDark,
            warning = WarningAmberDark,
            warningContainer = WarningAmberContainerDark,
            heroGradientStart = Color(0xFF006AFD),
            heroGradientEnd = Color(0xFF2B7FFF),
            heroBadgePositive = HeroBadgePositive,
            cardBorder = DarkOutlineVariant,
            chartLightBlue = Color(0xFF7DD3FC)
        )
    } else {
        LedgerSemanticColors(
            profit = IncomeGreenLight,
            profitContainer = IncomeGreenContainerLight,
            expense = ExpenseRedLight,
            expenseContainer = ExpenseRedContainerLight,
            warning = WarningAmberLight,
            warningContainer = WarningAmberContainerLight,
            heroGradientStart = BlueHeroStart,
            heroGradientEnd = BlueHeroEnd,
            heroBadgePositive = HeroBadgePositive,
            cardBorder = LightOutlineVariant,
            chartLightBlue = SkySecondary
        )
    }

    CompositionLocalProvider(LocalLedgerColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
