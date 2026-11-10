package com.paisatrail.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paisatrail.data.share.ShareInbox
import com.paisatrail.ui.calc.FdRdScreen
import com.paisatrail.ui.calc.TaxScreen
import com.paisatrail.ui.capture.CaptureScreen
import com.paisatrail.ui.home.HomeScreen
import com.paisatrail.ui.importer.ImportScreen
import com.paisatrail.ui.planner.PlannerScreen
import com.paisatrail.ui.settings.SettingsScreen
import com.paisatrail.ui.split.AddSharedExpenseScreen
import com.paisatrail.ui.split.SettleScreen
import com.paisatrail.ui.split.SplitScreen
import com.paisatrail.ui.txn.TxnEditScreen

/** Every navigation route in the app, in one place. */
object Routes {
    const val HOME = "home"
    const val SPLIT = "split"
    const val PLANNER = "planner"
    const val SETTINGS = "settings"

    const val TXN = "txn?id={id}"
    const val CAPTURE = "capture"
    const val IMPORT = "import"
    const val SHARED_EXPENSE = "split/expense?friendId={friendId}&groupId={groupId}"
    const val SETTLE = "split/settle/{friendId}"
    const val FD_RD = "calc/fdrd"
    const val TAX = "calc/tax"

    fun txn(id: Long = -1L): String = "txn?id=$id"
    fun sharedExpense(friendId: Long, groupId: Long): String = "split/expense?friendId=$friendId&groupId=$groupId"
    fun settle(friendId: Long): String = "split/settle/$friendId"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab(Routes.HOME, "Home", Icons.Filled.Home),
    Tab(Routes.SPLIT, "Split", Icons.Filled.Groups),
    Tab(Routes.PLANNER, "Planner", Icons.Filled.CalendarMonth),
    Tab(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
)

@Composable
fun AppRoot(inbox: ShareInbox) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onTab = TABS.any { it.route == currentRoute }
    var showAddSheet by remember { mutableStateOf(false) }

    // A file arrived (share intent). Open the screen that will take it from the inbox.
    val pending by inbox.pending.collectAsStateWithLifecycle()
    LaunchedEffect(pending) {
        val item = pending ?: return@LaunchedEffect
        showAddSheet = false
        navController.navigate(if (item.isPdf) Routes.IMPORT else Routes.CAPTURE)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Screens handle the status bar themselves; only the bottom bar is accounted for here.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (onTab) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.openTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Routes.HOME) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add a transaction")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenTransaction = { id -> navController.navigate(Routes.txn(id)) },
                    onOpenPlanner = { navController.openTab(Routes.PLANNER) }
                )
            }
            composable(Routes.SPLIT) {
                SplitScreen(
                    onAddExpense = { friendId, groupId ->
                        navController.navigate(Routes.sharedExpense(friendId, groupId))
                    },
                    onSettle = { friendId -> navController.navigate(Routes.settle(friendId)) }
                )
            }
            composable(Routes.PLANNER) {
                PlannerScreen(
                    onOpenFdRd = { navController.navigate(Routes.FD_RD) },
                    onOpenTax = { navController.navigate(Routes.TAX) }
                )
            }
            composable(Routes.SETTINGS) { SettingsScreen() }

            composable(
                route = Routes.TXN,
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
            ) {
                TxnEditScreen(onClose = { navController.goBack() })
            }
            composable(Routes.CAPTURE) {
                CaptureScreen(
                    onClose = { navController.goBack() },
                    onEdit = { id ->
                        navController.navigate(Routes.txn(id)) {
                            popUpTo(Routes.CAPTURE) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.IMPORT) {
                ImportScreen(onClose = { navController.goBack() })
            }
            composable(
                route = Routes.SHARED_EXPENSE,
                arguments = listOf(
                    navArgument("friendId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("groupId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                AddSharedExpenseScreen(
                    friendId = entry.arguments?.getLong("friendId") ?: -1L,
                    groupId = entry.arguments?.getLong("groupId") ?: -1L,
                    onClose = { navController.goBack() }
                )
            }
            composable(
                route = Routes.SETTLE,
                arguments = listOf(navArgument("friendId") { type = NavType.LongType })
            ) { entry ->
                SettleScreen(
                    friendId = entry.arguments?.getLong("friendId") ?: -1L,
                    onClose = { navController.goBack() },
                    onOpenSettings = { navController.openTab(Routes.SETTINGS) }
                )
            }
            composable(Routes.FD_RD) { FdRdScreen(onClose = { navController.goBack() }) }
            composable(Routes.TAX) { TaxScreen(onClose = { navController.goBack() }) }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 24.dp)) {
                Text(
                    "Add to your trail",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                AddOption(
                    icon = Icons.Filled.Edit,
                    title = "Type it in",
                    subtitle = "Add one expense or income by hand"
                ) {
                    showAddSheet = false
                    navController.navigate(Routes.txn())
                }
                AddOption(
                    icon = Icons.Filled.Image,
                    title = "From a payment screenshot",
                    subtitle = "Read on this phone, nothing is uploaded"
                ) {
                    showAddSheet = false
                    navController.navigate(Routes.CAPTURE)
                }
                AddOption(
                    icon = Icons.Filled.PictureAsPdf,
                    title = "Import a bank statement",
                    subtitle = "One-month PDF, reviewed before saving"
                ) {
                    showAddSheet = false
                    navController.navigate(Routes.IMPORT)
                }
            }
        }
    }
}

@Composable
private fun AddOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

/** Switches bottom-bar tab, keeping each tab's own state and a single copy on the back stack. */
private fun NavController.openTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Pops only when there is somewhere to go back to, so a double tap can never close Home. */
private fun NavController.goBack() {
    if (previousBackStackEntry != null) popBackStack()
}
