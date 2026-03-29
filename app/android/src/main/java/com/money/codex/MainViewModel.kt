package com.money.codex

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.money.codex.data.BudgetData
import com.money.codex.data.Category
import com.money.codex.data.CategoryPayload
import com.money.codex.data.CategoryStat
import com.money.codex.data.MoneyRepository
import com.money.codex.data.MonthlyStats
import com.money.codex.data.RecordItem
import com.money.codex.data.RecordPayload
import com.money.codex.data.StatisticsBundle
import com.money.codex.data.TrendData
import com.money.codex.data.currentMonthString
import com.money.codex.data.currentYearValue
import com.money.codex.data.todayString
import com.money.codex.ui.theme.AppThemePreset
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    Dashboard("概览"),
    Records("记账"),
    Statistics("统计"),
    Categories("分类"),
    Settings("设置")
}

data class DashboardUi(
    val stats: MonthlyStats = MonthlyStats(),
    val budgetRemaining: Double = 0.0,
    val budgetPercent: Double = 0.0,
    val dailyAvailable: Double = 0.0,
    val daysRemaining: Int = 0,
    val recentRecords: List<RecordItem> = emptyList(),
    val expenseStats: List<CategoryStat> = emptyList()
)

data class RecordsUi(
    val list: List<RecordItem> = emptyList(),
    val page: Int = 1,
    val total: Int = 0
)

data class RecordFilters(
    val startDate: String = "",
    val endDate: String = "",
    val type: String = "",
    val categoryId: Int? = null,
    val keyword: String = ""
)

data class StatisticsUi(
    val stats: MonthlyStats = MonthlyStats(),
    val expenseStats: List<CategoryStat> = emptyList(),
    val incomeStats: List<CategoryStat> = emptyList(),
    val trend: TrendData = TrendData(),
    val period: String = "month",
    val month: String = currentMonthString(),
    val year: Int = currentYearValue()
)

data class BudgetUi(
    val budget: BudgetData = BudgetData(),
    val dailyAvailable: Double = 0.0,
    val daysRemaining: Int = 0
)

class MainViewModel(
    private val repository: MoneyRepository = MoneyRepository()
) : ViewModel() {

    var currentTab by mutableStateOf(AppTab.Dashboard)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var categories by mutableStateOf(emptyList<Category>())
        private set

    var dashboard by mutableStateOf(DashboardUi())
        private set

    var records by mutableStateOf(RecordsUi())
        private set

    var recordFilters by mutableStateOf(RecordFilters())
        private set

    var statistics by mutableStateOf(StatisticsUi())
        private set

    var budget by mutableStateOf(BudgetUi())
        private set

    var currentMonth by mutableStateOf(currentMonthString())
        private set

    var selectedTheme by mutableStateOf(AppThemePreset.Ocean)
        private set

    var statisticsPeriod by mutableStateOf("month")
        private set

    var statisticsMonth by mutableStateOf(currentMonthString())
        private set

    var statisticsYear by mutableStateOf(currentYearValue())
        private set

    var addRecordDialogVisible by mutableStateOf(false)
        private set

    var toastMessage by mutableStateOf<String?>(null)
        private set

    var editingRecord by mutableStateOf<RecordItem?>(null)
        private set

    init {
        refreshAll()
    }

    fun selectTab(tab: AppTab) {
        currentTab = tab
    }

    fun showAddRecordDialog(show: Boolean) {
        addRecordDialogVisible = show
    }

    fun setTheme(preset: AppThemePreset) {
        selectedTheme = preset
    }

    fun clearToast() {
        toastMessage = null
    }

    fun openEditRecord(record: RecordItem) {
        editingRecord = record
    }

    fun closeEditRecord() {
        editingRecord = null
    }

    fun refreshAll() {
        viewModelScope.launch {
            isLoading = true
            categories = repository.loadCategories()

            val dashboardData = repository.loadDashboard(currentMonth)
            val percent = if (dashboardData.budget.budget <= 0.0) 0.0
            else (dashboardData.budget.spent / dashboardData.budget.budget) * 100
            dashboard = DashboardUi(
                stats = dashboardData.stats,
                budgetRemaining = dashboardData.budget.remaining,
                budgetPercent = percent.coerceIn(0.0, 100.0),
                dailyAvailable = dashboardData.dailyBudget.dailyAvailable,
                daysRemaining = dashboardData.dailyBudget.daysRemaining,
                recentRecords = dashboardData.records,
                expenseStats = dashboardData.categoryStats
            )

            val recordsData = repository.loadRecords(
                page = 1,
                size = 20,
                startDate = recordFilters.startDate.ifBlank { null },
                endDate = recordFilters.endDate.ifBlank { null },
                type = recordFilters.type.ifBlank { null },
                categoryId = recordFilters.categoryId,
                keyword = recordFilters.keyword.ifBlank { null }
            )
            records = RecordsUi(recordsData.records, 1, recordsData.total)

            val statisticsData: StatisticsBundle = repository.loadStatistics(
                period = statisticsPeriod,
                month = statisticsMonth,
                year = statisticsYear
            )
            statistics = StatisticsUi(
                statisticsData.stats,
                statisticsData.expenseStats,
                statisticsData.incomeStats,
                statisticsData.trend,
                statisticsPeriod,
                statisticsMonth,
                statisticsYear
            )

            val (budgetData, dailyData) = repository.loadBudget(currentMonth)
            budget = BudgetUi(budgetData, dailyData.dailyAvailable, dailyData.daysRemaining)
            isLoading = false
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            val data = repository.loadDashboard(currentMonth)
            val percent = if (data.budget.budget <= 0.0) 0.0 else (data.budget.spent / data.budget.budget) * 100
            dashboard = DashboardUi(
                stats = data.stats,
                budgetRemaining = data.budget.remaining,
                budgetPercent = percent.coerceIn(0.0, 100.0),
                dailyAvailable = data.dailyBudget.dailyAvailable,
                daysRemaining = data.dailyBudget.daysRemaining,
                recentRecords = data.records,
                expenseStats = data.categoryStats
            )
        }
    }

    fun loadRecords(page: Int = 1) {
        viewModelScope.launch {
            val data = repository.loadRecords(
                page = page,
                size = 20,
                startDate = recordFilters.startDate.ifBlank { null },
                endDate = recordFilters.endDate.ifBlank { null },
                type = recordFilters.type.ifBlank { null },
                categoryId = recordFilters.categoryId,
                keyword = recordFilters.keyword.ifBlank { null }
            )
            records = RecordsUi(data.records, page, data.total)
        }
    }

    fun updateRecordFilters(
        startDate: String = recordFilters.startDate,
        endDate: String = recordFilters.endDate,
        type: String = recordFilters.type,
        categoryId: Int? = recordFilters.categoryId,
        keyword: String = recordFilters.keyword
    ) {
        recordFilters = RecordFilters(startDate, endDate, type, categoryId, keyword)
    }

    fun resetRecordFilters() {
        recordFilters = RecordFilters()
        loadRecords(1)
    }

    fun searchRecords() {
        loadRecords(1)
    }

    fun loadStatistics() {
        viewModelScope.launch {
            val data: StatisticsBundle = repository.loadStatistics(
                period = statisticsPeriod,
                month = statisticsMonth,
                year = statisticsYear
            )
            statistics = StatisticsUi(
                data.stats,
                data.expenseStats,
                data.incomeStats,
                data.trend,
                statisticsPeriod,
                statisticsMonth,
                statisticsYear
            )
        }
    }

    fun changeStatisticsPeriod(period: String) {
        statisticsPeriod = period
        loadStatistics()
    }

    fun changeStatisticsMonth(month: String) {
        statisticsMonth = month
        loadStatistics()
    }

    fun changeStatisticsYear(year: Int) {
        statisticsYear = year
        loadStatistics()
    }

    fun loadBudget() {
        viewModelScope.launch {
            val (budgetData, dailyData) = repository.loadBudget(currentMonth)
            budget = BudgetUi(budgetData, dailyData.dailyAvailable, dailyData.daysRemaining)
        }
    }

    fun addRecord(
        type: String,
        categoryId: Int,
        amount: Double,
        remark: String,
        date: String = todayString()
    ) {
        viewModelScope.launch {
            if (amount <= 0.0 || categoryId <= 0) {
                toastMessage = "请先填写正确的金额与分类"
                return@launch
            }
            val ok = repository.addRecord(
                RecordPayload(
                    date = date,
                    type = type,
                    categoryId = categoryId,
                    amount = amount,
                    remark = remark
                )
            )
            toastMessage = if (ok) "记录添加成功" else "接口不可用，已本地刷新"
            addRecordDialogVisible = false
            loadDashboard()
            loadRecords()
            loadStatistics()
            loadBudget()
        }
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteRecord(id)
            toastMessage = "记录已删除"
            if (editingRecord?.id == id) {
                editingRecord = null
            }
            loadDashboard()
            loadRecords(records.page)
            loadStatistics()
            loadBudget()
        }
    }

    fun updateRecord(
        id: Int,
        type: String,
        categoryId: Int,
        amount: Double,
        remark: String,
        date: String
    ) {
        viewModelScope.launch {
            if (amount <= 0.0 || categoryId <= 0) {
                toastMessage = "请先填写正确的金额与分类"
                return@launch
            }
            val ok = repository.updateRecord(
                id,
                RecordPayload(
                    date = date,
                    type = type,
                    categoryId = categoryId,
                    amount = amount,
                    remark = remark
                )
            )
            toastMessage = if (ok) "记录更新成功" else "更新失败，请检查接口"
            if (ok) {
                editingRecord = null
            }
            loadDashboard()
            loadRecords(records.page)
            loadStatistics()
            loadBudget()
        }
    }

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            val ok = repository.setBudget(amount, currentMonth)
            toastMessage = if (ok) "预算设置成功" else "接口不可用，已本地刷新"
            loadDashboard()
            loadBudget()
        }
    }

    fun addCategory(
        type: String,
        icon: String,
        name: String,
        description: String,
        sort: Int
    ) {
        viewModelScope.launch {
            val ok = repository.addCategory(
                CategoryPayload(type, icon, name, description, sort)
            )
            toastMessage = if (ok) "分类添加成功" else "分类添加失败"
            refreshAll()
        }
    }

    fun updateCategory(
        id: Int,
        type: String,
        icon: String,
        name: String,
        description: String,
        sort: Int
    ) {
        viewModelScope.launch {
            val ok = repository.updateCategory(
                id,
                CategoryPayload(type, icon, name, description, sort)
            )
            toastMessage = if (ok) "分类更新成功" else "分类更新失败"
            refreshAll()
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            val ok = repository.deleteCategory(id)
            toastMessage = if (ok) "分类已删除" else "分类删除失败"
            refreshAll()
        }
    }
}
