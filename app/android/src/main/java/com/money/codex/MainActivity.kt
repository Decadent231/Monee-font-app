package com.money.codex

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.money.codex.ui.screens.AddRecordDialog
import com.money.codex.ui.screens.CategoriesScreen
import com.money.codex.ui.screens.DashboardScreen
import com.money.codex.ui.screens.EditRecordDialog
import com.money.codex.ui.screens.RecordsScreen
import com.money.codex.ui.screens.SettingsScreen
import com.money.codex.ui.screens.StatisticsScreen
import com.money.codex.ui.theme.AppBackground
import com.money.codex.ui.theme.MoneyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            MoneyTheme(themePreset = vm.selectedTheme) {
                MoneyApp(vm)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
private fun MoneyApp(vm: MainViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        vm.loadReminderSettings(context)
    }

    LaunchedEffect(vm.toastMessage) {
        vm.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearToast()
        }
    }

    if (vm.addRecordDialogVisible) {
        AddRecordDialog(
            categories = vm.categories,
            onDismiss = { vm.showAddRecordDialog(false) },
            onSubmit = { type, categoryId, amount, remark, date ->
                vm.addRecord(type, categoryId, amount, remark, date)
            }
        )
    }
    vm.editingRecord?.let { record ->
        EditRecordDialog(
            record = record,
            categories = vm.categories,
            onDismiss = { vm.closeEditRecord() },
            onDelete = { vm.deleteRecord(record.id) },
            onUpdate = { type, categoryId, amount, remark, date ->
                vm.updateRecord(record.id, type, categoryId, amount, remark, date)
            }
        )
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "记账本 · ${vm.currentTab.title}",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                listOf(
                    AppTab.Dashboard to Pair(Icons.Default.Home, "概览"),
                    AppTab.Records to Pair(Icons.AutoMirrored.Filled.List, "记账"),
                    AppTab.Statistics to Pair(Icons.Default.BarChart, "统计"),
                    AppTab.Categories to Pair(Icons.Default.Apps, "分类"),
                    AppTab.Settings to Pair(Icons.Default.Settings, "设置")
                ).forEach { (tab, iconLabel) ->
                    NavigationBarItem(
                        selected = vm.currentTab == tab,
                        onClick = { vm.selectTab(tab) },
                        icon = { Icon(iconLabel.first, contentDescription = iconLabel.second) },
                        label = { Text(iconLabel.second) },
                        alwaysShowLabel = false
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.showAddRecordDialog(true) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "记一笔")
            }
        }
    ) { padding ->
        val contentModifier = Modifier.padding(padding).fillMaxSize()
        val pullRefreshState = rememberPullRefreshState(
            refreshing = vm.isLoading,
            onRefresh = { vm.refreshAll() }
        )
        Box(
            modifier = contentModifier.pullRefresh(pullRefreshState)
        ) {
            when (vm.currentTab) {
                AppTab.Dashboard -> Box(Modifier.fillMaxSize()) {
                    DashboardScreen(vm.dashboard) { vm.openEditRecord(it) }
                }
                AppTab.Records -> Box(Modifier.fillMaxSize()) {
                    RecordsScreen(
                        ui = vm.records,
                        filters = vm.recordFilters,
                        categories = vm.categories,
                        onFiltersChange = { startDate, endDate, type, categoryId, keyword ->
                            vm.updateRecordFilters(startDate, endDate, type, categoryId, keyword)
                        },
                        onSearch = { vm.searchRecords() },
                        onReset = { vm.resetRecordFilters() },
                        onDelete = { vm.deleteRecord(it) },
                        onEdit = { vm.openEditRecord(it) }
                    )
                }
                AppTab.Statistics -> Box(Modifier.fillMaxSize()) {
                    StatisticsScreen(
                        ui = vm.statistics,
                        onPeriodChange = { vm.changeStatisticsPeriod(it) },
                        onMonthChange = { vm.changeStatisticsMonth(it) },
                        onYearChange = { vm.changeStatisticsYear(it) }
                    )
                }
                AppTab.Categories -> Box(Modifier.fillMaxSize()) {
                    CategoriesScreen(
                        categories = vm.categories,
                        onAdd = { type, icon, name, description, sort ->
                            vm.addCategory(type, icon, name, description, sort)
                        },
                        onUpdate = { id, type, icon, name, description, sort ->
                            vm.updateCategory(id, type, icon, name, description, sort)
                        },
                        onDelete = { id ->
                            vm.deleteCategory(id)
                        }
                    )
                }
                AppTab.Settings -> Box(Modifier.fillMaxSize()) {
                    SettingsScreen(
                        ui = vm.budget,
                        selectedTheme = vm.selectedTheme,
                        onThemeChange = { vm.setTheme(it) },
                        onSetBudget = { vm.setBudget(it) },
                        reminderEnabled = vm.reminderEnabled,
                        reminderHour = vm.reminderHour,
                        reminderMinute = vm.reminderMinute,
                        onReminderEnabledChange = { enabled ->
                            if (
                                enabled &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            vm.setReminderEnabled(context, enabled)
                        },
                        onReminderTimeChange = { hour, minute -> vm.setReminderTime(context, hour, minute) }
                    )
                }
            }
            PullRefreshIndicator(
                refreshing = vm.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
