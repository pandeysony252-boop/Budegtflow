package com.example

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.ThemeMode
import com.example.data.model.BudgetCategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.LedgerRepository
import com.example.ui.components.AddEditBudgetSheet
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.RedLedgerTheme
import com.example.ui.viewmodel.LedgerViewModel
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_TRANSACTIONS = "transactions"
const val ROUTE_REPORTS = "reports"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_BUDGETS = "budgets"

private data class NavDestinationItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val TopLevelDestinations = listOf(
    NavDestinationItem(
        route = ROUTE_HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    ),
    NavDestinationItem(
        route = ROUTE_TRANSACTIONS,
        labelRes = R.string.nav_transactions,
        selectedIcon = Icons.Rounded.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong,
        testTag = "nav_transactions"
    ),
    NavDestinationItem(
        route = ROUTE_REPORTS,
        labelRes = R.string.nav_reports,
        selectedIcon = Icons.Rounded.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        testTag = "nav_reports"
    ),
    NavDestinationItem(
        route = ROUTE_SETTINGS,
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        setContent {
            ProfitTrackApp()
        }
    }
}

@Composable
fun ProfitTrackApp() {
    val context = LocalContext.current
    val ledgerViewModel: LedgerViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = AppDatabase.getInstance(app)
                val prefs = AppPreferences(app)
                LedgerViewModel(LedgerRepository(db.transactionDao(), db.budgetDao()), prefs)
            }
        }
    )

    val uiState by ledgerViewModel.uiState.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (uiState.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    DisposableEffect(useDarkTheme) {
        val activity = context as? ComponentActivity
        activity?.enableEdgeToEdge(
            statusBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            },
            navigationBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            }
        )
        onDispose {}
    }

    RedLedgerTheme(themeMode = uiState.themeMode) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showTransactionSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var defaultTxType by remember { mutableStateOf(TransactionType.PROFIT) }
    var preselectedCategoryId by remember { mutableStateOf<Long?>(null) }

    var showBudgetSheet by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetCategoryEntity?>(null) }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openNewTransaction(type: TransactionType, categoryId: Long? = null) {
        editingTransaction = null
        defaultTxType = type
        preselectedCategoryId = categoryId
        showTransactionSheet = true
    }

    fun openEditTransaction(tx: TransactionEntity) {
        editingTransaction = tx
        defaultTxType = tx.type
        preselectedCategoryId = tx.categoryId
        showTransactionSheet = true
    }

    fun openNewBudget() {
        editingBudget = null
        showBudgetSheet = true
    }

    fun openEditBudget(budget: BudgetCategoryEntity) {
        editingBudget = budget
        showBudgetSheet = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavRail) {
                    ProfitTrackBottomBar(
                        currentRoute = currentRoute,
                        onSelectTab = ::navigateToTab,
                        onCenterAddClick = { openNewTransaction(TransactionType.EXPENSE) }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Spacer(modifier = Modifier.weight(0.1f))
                        TopLevelDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_HOME) {
                        DashboardScreen(
                            uiState = uiState,
                            onAddExpenseClick = { catId ->
                                openNewTransaction(TransactionType.EXPENSE, catId)
                            },
                            onAddBudgetClick = ::openNewBudget,
                            onEditBudgetClick = ::openEditBudget,
                            onEditTransactionClick = ::openEditTransaction,
                            onOpenReports = { navigateToTab(ROUTE_REPORTS) },
                            onOpenBudgets = { navController.navigate(ROUTE_BUDGETS) },
                            onOpenTransactions = { navigateToTab(ROUTE_TRANSACTIONS) }
                        )
                    }

                    composable(ROUTE_TRANSACTIONS) {
                        LedgerScreen(
                            uiState = uiState,
                            onPreviousPeriod = ledgerViewModel::previousPeriod,
                            onNextPeriod = ledgerViewModel::nextPeriod,
                            onToggleAllTime = ledgerViewModel::toggleAllTime,
                            onSearchQueryChange = ledgerViewModel::setSearchQuery,
                            onTypeFilterChange = ledgerViewModel::setTypeFilter,
                            onCategoryFilterChange = ledgerViewModel::setCategoryFilter,
                            onEditTransactionClick = ::openEditTransaction
                        )
                    }

                    composable(ROUTE_REPORTS) {
                        ReportsScreen(
                            uiState = uiState,
                            onSelectRange = ledgerViewModel::setReportRange,
                            onOpenBudgets = { navController.navigate(ROUTE_BUDGETS) }
                        )
                    }

                    composable(ROUTE_BUDGETS) {
                        BudgetsScreen(
                            uiState = uiState,
                            onBackClick = { navController.popBackStack() },
                            onSaveOverallBudget = { amount ->
                                ledgerViewModel.setOverallMonthlyBudget(amount)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Monthly budget saved")
                                }
                            },
                            onAddBudgetClick = ::openNewBudget,
                            onEditBudgetClick = ::openEditBudget,
                            onQuickLogForCategory = { catId ->
                                openNewTransaction(TransactionType.EXPENSE, catId)
                            }
                        )
                    }

                    composable(ROUTE_SETTINGS) {
                        SettingsScreen(
                            uiState = uiState,
                            onSelectCurrency = ledgerViewModel::setCurrency,
                            onSelectThemeMode = ledgerViewModel::setThemeMode,
                            onUpdateUserName = ledgerViewModel::setUserName,
                            onToggleBudgetAlerts = ledgerViewModel::setBudgetAlertsEnabled,
                            onOpenBudgets = { navController.navigate(ROUTE_BUDGETS) },
                            onExportCsv = {
                                val csv = ledgerViewModel.buildCsvExport()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "ProfitTrack Export")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export ProfitTrack CSV"))
                            },
                            onClearAllData = {
                                ledgerViewModel.clearAllData()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("All local data cleared")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showTransactionSheet) {
        AddEditTransactionSheet(
            initialTransaction = editingTransaction,
            initialType = defaultTxType,
            preselectedCategoryId = preselectedCategoryId,
            budgets = uiState.budgets,
            currency = uiState.currency,
            onDismiss = { showTransactionSheet = false },
            onRequestNewBudget = {
                showTransactionSheet = false
                openNewBudget()
            },
            onSave = { id, title, amount, type, categoryId, categoryName, note, timestamp ->
                ledgerViewModel.saveTransaction(
                    existingId = id,
                    title = title,
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    categoryName = categoryName,
                    note = note,
                    timestamp = timestamp
                )
                showTransactionSheet = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = if (id == 0L) "Transaction added" else "Transaction updated",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            onDelete = { tx ->
                ledgerViewModel.deleteTransaction(tx.id)
                showTransactionSheet = false
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Deleted \"${tx.title}\"",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        ledgerViewModel.restoreTransaction(tx)
                    }
                }
            }
        )
    }

    if (showBudgetSheet) {
        AddEditBudgetSheet(
            initialBudget = editingBudget,
            currency = uiState.currency,
            onDismiss = { showBudgetSheet = false },
            onSave = { id, name, limit, iconKey, createdAt ->
                ledgerViewModel.saveBudgetCategory(
                    existingId = id,
                    name = name,
                    monthlyLimit = limit,
                    iconKey = iconKey,
                    createdAt = createdAt
                )
                showBudgetSheet = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = if (id == 0L) "Fixed budget \"$name\" saved" else "Budget updated",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            onDelete = { budget ->
                ledgerViewModel.deleteBudgetCategory(budget.id)
                showBudgetSheet = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Removed budget \"${budget.name}\"",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }
    }
}

@Composable
private fun ProfitTrackBottomBar(
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    onCenterAddClick: () -> Unit
) {
    val semantic = LocalLedgerColors.current
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, semantic.cardBorder),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavSlot(
                item = TopLevelDestinations[0],
                selected = currentRoute == TopLevelDestinations[0].route,
                onClick = { onSelectTab(TopLevelDestinations[0].route) },
                modifier = Modifier.weight(1f)
            )
            BottomNavSlot(
                item = TopLevelDestinations[1],
                selected = currentRoute == TopLevelDestinations[1].route,
                onClick = { onSelectTab(TopLevelDestinations[1].route) },
                modifier = Modifier.weight(1f)
            )

            // Center Circular Blue '+' Button matching Mockup
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onCenterAddClick)
                    .testTag("main_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.cd_add_transaction),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            BottomNavSlot(
                item = TopLevelDestinations[2],
                selected = currentRoute == TopLevelDestinations[2].route,
                onClick = { onSelectTab(TopLevelDestinations[2].route) },
                modifier = Modifier.weight(1f)
            )
            BottomNavSlot(
                item = TopLevelDestinations[3],
                selected = currentRoute == TopLevelDestinations[3].route,
                onClick = { onSelectTab(TopLevelDestinations[3].route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomNavSlot(
    item: NavDestinationItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp)
            .testTag(item.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = stringResource(item.labelRes),
            tint = contentColor,
            modifier = Modifier.size(23.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1
        )
    }
}
