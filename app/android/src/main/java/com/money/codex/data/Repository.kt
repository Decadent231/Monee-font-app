package com.money.codex.data

import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

class MoneyRepository(private val api: MoneyApiService = ApiFactory.service) {

    suspend fun loadCategories(): List<Category> = runCatching {
        api.categories().data.orEmpty()
    }.getOrDefault(defaultCategories)

    suspend fun addCategory(payload: CategoryPayload): Boolean = runCatching {
        api.addCategory(payload)
        true
    }.getOrDefault(false)

    suspend fun updateCategory(id: Int, payload: CategoryPayload): Boolean = runCatching {
        api.updateCategory(id, payload)
        true
    }.getOrDefault(false)

    suspend fun deleteCategory(id: Int): Boolean = runCatching {
        api.deleteCategory(id)
        true
    }.getOrDefault(false)

    suspend fun loadDashboard(month: String): DashboardBundle {
        val monthlyStats = runCatching { api.monthlyStats(month).data ?: MonthlyStats() }.getOrDefault(MonthlyStats())
        val budget = runCatching { api.budget(month).data ?: BudgetData() }.getOrDefault(BudgetData())
        val dailyBudget = runCatching { api.dailyBudget(month).data ?: DailyBudgetData() }.getOrDefault(DailyBudgetData())
        val records = runCatching { api.records(page = 1, size = 100).data?.records.orEmpty() }.getOrDefault(mockRecords)
        val expenseStats = runCatching { api.categoryStats(month, "expense").data.orEmpty() }
            .getOrDefault(mockCategoryStats)

        return DashboardBundle(monthlyStats, budget, dailyBudget, records, expenseStats)
    }

    suspend fun loadRecords(
        page: Int = 1,
        size: Int = 20,
        startDate: String? = null,
        endDate: String? = null,
        type: String? = null,
        categoryId: Int? = null,
        keyword: String? = null
    ): RecordsPage = runCatching {
        api.records(page, size, startDate, endDate, type, categoryId, keyword).data ?: RecordsPage()
    }.getOrDefault(RecordsPage(mockRecords, mockRecords.size))

    suspend fun addRecord(payload: RecordPayload): Boolean = runCatching {
        api.addRecord(payload)
        true
    }.getOrDefault(false)

    suspend fun updateRecord(id: Int, payload: RecordPayload): Boolean = runCatching {
        api.updateRecord(id, payload)
        true
    }.getOrDefault(false)

    suspend fun deleteRecord(id: Int): Boolean = runCatching {
        api.deleteRecord(id)
        true
    }.getOrDefault(false)

    suspend fun loadStatistics(
        period: String,
        month: String,
        year: Int
    ): StatisticsBundle {
        return if (period == "year") {
            val yearly = runCatching { api.yearlyStats(year).data ?: YearlyStats() }.getOrDefault(YearlyStats())
            val stats = MonthlyStats(
                expense = yearly.expense,
                income = yearly.income,
                balance = yearly.balance
            )
            val expense = runCatching { api.yearlyCategoryStats(year, "expense").data.orEmpty() }
                .getOrDefault(mockCategoryStats)
            val income = runCatching { api.yearlyCategoryStats(year, "income").data.orEmpty() }
                .getOrDefault(mockIncomeStats)
            val trend = runCatching { api.yearlyTrend(year).data ?: TrendData() }
                .getOrDefault(mockTrendData)
            StatisticsBundle(stats, expense, income, trend)
        } else {
            val stats = runCatching { api.monthlyStats(month).data ?: MonthlyStats() }.getOrDefault(MonthlyStats())
            val expense = runCatching { api.categoryStats(month, "expense").data.orEmpty() }
                .getOrDefault(mockCategoryStats)
            val income = runCatching { api.categoryStats(month, "income").data.orEmpty() }
                .getOrDefault(mockIncomeStats)
            val trend = runCatching { api.trend(month).data ?: TrendData() }.getOrDefault(mockTrendData)
            StatisticsBundle(stats, expense, income, trend)
        }
    }

    suspend fun loadBudget(month: String): Pair<BudgetData, DailyBudgetData> {
        val budget = runCatching { api.budget(month).data ?: BudgetData() }
            .getOrDefault(BudgetData(5000.0, 3500.0, 1500.0))
        val daily = runCatching { api.dailyBudget(month).data ?: DailyBudgetData() }
            .getOrDefault(DailyBudgetData(75.0, 20))
        return budget to daily
    }

    suspend fun setBudget(amount: Double, month: String): Boolean = runCatching {
        api.setBudget(BudgetPayload(amount, month))
        true
    }.getOrDefault(false)

    companion object {
        val defaultCategories = listOf(
            Category(1, "expense", "🍽️", "餐饮", "餐饮消费", 1),
            Category(2, "expense", "🚇", "交通", "交通出行", 2),
            Category(3, "expense", "🛍️", "购物", "日常购物", 3),
            Category(4, "expense", "🎮", "娱乐", "休闲娱乐", 4),
            Category(7, "expense", "🏠", "住房", "房租水电", 7),
            Category(9, "income", "💼", "工资", "工资收入", 1),
            Category(10, "income", "🎁", "奖金", "奖金收入", 2)
        )

        val mockRecords = listOf(
            RecordItem(1, "2026-03-25", "expense", 1, "餐饮", 38.5, "午餐"),
            RecordItem(2, "2026-03-25", "expense", 2, "交通", 12.0, "地铁"),
            RecordItem(3, "2026-03-24", "income", 9, "工资", 8200.0, "3月工资"),
            RecordItem(4, "2026-03-24", "expense", 3, "购物", 129.0, "生活用品")
        )

        val mockCategoryStats = listOf(
            CategoryStat(1, "餐饮", 1200.0, 35),
            CategoryStat(2, "交通", 500.0, 20),
            CategoryStat(3, "购物", 800.0, 10),
            CategoryStat(4, "娱乐", 600.0, 8)
        )

        val mockIncomeStats = listOf(
            CategoryStat(9, "工资", 8200.0, 1),
            CategoryStat(10, "奖金", 1200.0, 1)
        )

        val mockTrendData = TrendData(
            dates = listOf("2026-03-21", "2026-03-22", "2026-03-23", "2026-03-24", "2026-03-25"),
            expenses = listOf(120.0, 80.0, 140.0, 65.0, 150.0),
            incomes = listOf(0.0, 0.0, 0.0, 8200.0, 0.0)
        )
    }
}

data class DashboardBundle(
    val stats: MonthlyStats,
    val budget: BudgetData,
    val dailyBudget: DailyBudgetData,
    val records: List<RecordItem>,
    val categoryStats: List<CategoryStat>
)

data class StatisticsBundle(
    val stats: MonthlyStats,
    val expenseStats: List<CategoryStat>,
    val incomeStats: List<CategoryStat>,
    val trend: TrendData
)

fun currentMonthString(): String = YearMonth.now().toString()
fun currentYearValue(): Int = Year.now().value
fun todayString(): String = LocalDate.now().toString()
