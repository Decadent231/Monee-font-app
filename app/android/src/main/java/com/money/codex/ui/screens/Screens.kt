package com.money.codex.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.money.codex.BudgetUi
import com.money.codex.DashboardUi
import com.money.codex.RecordFilters
import com.money.codex.RecordsUi
import com.money.codex.StatisticsUi
import com.money.codex.data.Category
import com.money.codex.data.CategoryStat
import com.money.codex.data.RecordItem
import com.money.codex.data.TrendData
import com.money.codex.data.todayString
import com.money.codex.ui.theme.AppThemePreset
import com.money.codex.ui.theme.Brand
import com.money.codex.ui.theme.BrandSurface
import com.money.codex.ui.theme.Expense
import com.money.codex.ui.theme.Income
import com.money.codex.ui.theme.TextMuted
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

private val moneyFmt = DecimalFormat("#,##0.00")
private val expenseIconPresets = listOf("🍽️", "🚗", "🛒", "🎮", "📦", "📱", "🏠")
private val incomeIconPresets = listOf("💰", "🎁", "📈", "💵", "🧧")

fun money(value: Double): String = "¥${moneyFmt.format(value)}"

private fun weekdayZh(date: String): String {
    val day = runCatching { LocalDate.parse(date).dayOfWeek }.getOrNull() ?: return ""
    return when (day.value) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        else -> "周日"
    }
}

private fun defaultCategoryId(categories: List<Category>, type: String): Int {
    val typed = categories.filter { it.type == type }
    val preferred = if (type == "expense") {
        typed.firstOrNull { it.name.contains("餐饮") }
    } else {
        null
    }
    return (preferred ?: typed.firstOrNull())?.id ?: 0
}

@Composable
fun DashboardScreen(ui: DashboardUi, onEditRecord: (RecordItem) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Brand)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("本月概览", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    Text(
                        text = money(ui.stats.balance),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("收入 ${money(ui.stats.income)}", color = Color.White.copy(alpha = 0.88f))
                        Text("支出 ${money(ui.stats.expense)}", color = Color.White.copy(alpha = 0.88f))
                    }
                }
            }
        }

        item {
            SectionTitle("预算节奏")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("今日建议最多支出 ${money(ui.dailyAvailable)}", fontWeight = FontWeight.SemiBold)
                    LinearProgressIndicator(
                        progress = { (ui.budgetPercent / 100.0).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(7.dp)
                    )
                    Text(
                        text = "预算剩余 ${money(ui.budgetRemaining)} · 剩余 ${ui.daysRemaining} 天 · 使用 ${ui.budgetPercent.toInt()}%",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }
        item { SectionTitle("最近记录") }
        val recentRecords = ui.recentRecords.sortedWith(
            compareByDescending<RecordItem> { it.date }.thenByDescending { it.id }
        )
        if (recentRecords.isEmpty()) {
            item { EmptyTip("暂无记录，开始记账吧") }
        } else {
            val grouped = recentRecords.groupBy { it.date }
            grouped.forEach { (date, records) ->
                val dayExpense = records.filter { it.type == "expense" }.sumOf { it.amount }
                val dayIncome = records.filter { it.type == "income" }.sumOf { it.amount }
                val dayNet = dayIncome - dayExpense
                val dayNetText = if (dayNet < 0) {
                    "支出 -${money(kotlin.math.abs(dayNet))}"
                } else {
                    "收入 +${money(dayNet)}"
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$date ${weekdayZh(date)}",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = dayNetText,
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                items(records) { record ->
                    RecordRow(
                        record = record,
                        showDelete = false,
                        showEdit = true,
                        onDelete = {},
                        onEdit = { onEditRecord(record) }
                    )
                }
            }
        }

    }
}

@Composable
fun RecordsScreen(
    ui: RecordsUi,
    filters: RecordFilters,
    categories: List<Category>,
    onFiltersChange: (startDate: String, endDate: String, type: String, categoryId: Int?, keyword: String) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
    onDelete: (Int) -> Unit,
    onEdit: (RecordItem) -> Unit
) {
    val context = LocalContext.current
    var advancedExpanded by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteRecord by remember { mutableStateOf<RecordItem?>(null) }
    val hasAdvancedFilters = filters.type.isNotBlank() || filters.categoryId != null || filters.keyword.isNotBlank()
    val availableCategories = if (filters.type.isBlank()) {
        categories
    } else {
        categories.filter { it.type == filters.type }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionTitle("记账记录")
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (filters.startDate.isBlank()) "开始日期" else filters.startDate,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .background(BrandSurface, RoundedCornerShape(10.dp))
                            .clickable {
                                val current = filters.startDate.ifBlank { todayString() }
                                showDatePicker(context, current) {
                                    onFiltersChange(it, filters.endDate, filters.type, filters.categoryId, filters.keyword)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                    Text("~", color = TextMuted)
                    Text(
                        text = if (filters.endDate.isBlank()) "结束日期" else filters.endDate,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .background(BrandSurface, RoundedCornerShape(10.dp))
                            .clickable {
                                val current = filters.endDate.ifBlank { todayString() }
                                showDatePicker(context, current) {
                                    onFiltersChange(filters.startDate, it, filters.type, filters.categoryId, filters.keyword)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSearch,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("搜索") }
                    Text(
                        text = if (advancedExpanded) "收起筛选" else if (hasAdvancedFilters) "筛选(已选)" else "展开筛选",
                        color = if (hasAdvancedFilters) Brand else TextMuted,
                        modifier = Modifier
                            .background(BrandSurface, RoundedCornerShape(10.dp))
                            .clickable { advancedExpanded = !advancedExpanded }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                    Text(
                        text = "重置",
                        color = TextMuted,
                        modifier = Modifier
                            .background(BrandSurface, RoundedCornerShape(10.dp))
                            .clickable {
                                advancedExpanded = false
                                onReset()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }

                AnimatedVisibility(visible = advancedExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("" to "全部", "expense" to "支出", "income" to "收入").forEach { (value, label) ->
                                Text(
                                    text = label,
                                    color = if (filters.type == value) Color.White else Color(0xFF334155),
                                    modifier = Modifier
                                        .background(
                                            if (filters.type == value) Brand else BrandSurface,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            onFiltersChange(
                                                filters.startDate,
                                                filters.endDate,
                                                value,
                                                null,
                                                filters.keyword
                                            )
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                Text(
                                    text = "全部分类",
                                    color = if (filters.categoryId == null) Color.White else Color(0xFF334155),
                                    modifier = Modifier
                                        .background(
                                            if (filters.categoryId == null) Brand else BrandSurface,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            onFiltersChange(
                                                filters.startDate,
                                                filters.endDate,
                                                filters.type,
                                                null,
                                                filters.keyword
                                            )
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                            items(availableCategories) { category ->
                                Text(
                                    text = "${category.icon} ${category.name}",
                                    color = if (filters.categoryId == category.id) Color.White else Color(0xFF334155),
                                    modifier = Modifier
                                        .background(
                                            if (filters.categoryId == category.id) Brand else BrandSurface,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            onFiltersChange(
                                                filters.startDate,
                                                filters.endDate,
                                                filters.type,
                                                category.id,
                                                filters.keyword
                                            )
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = filters.keyword,
                            onValueChange = {
                                onFiltersChange(
                                    filters.startDate,
                                    filters.endDate,
                                    filters.type,
                                    filters.categoryId,
                                    it
                                )
                            },
                            label = { Text("关键词（备注）") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (ui.list.isEmpty()) {
            EmptyTip("暂无记录")
            return@Column
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val grouped = ui.list
                .sortedWith(compareByDescending<RecordItem> { it.date }.thenByDescending { it.id })
                .groupBy { it.date }
            grouped.forEach { (date, records) ->
                item {
                    Text(
                        text = "$date ${weekdayZh(date)}",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                    )
                }
                items(records) { record ->
                    RecordRow(
                        record = record,
                        showDelete = true,
                        showEdit = true,
                        onDelete = { pendingDeleteRecord = record },
                        onEdit = { onEdit(record) }
                    )
                }
            }
        }
    }

    pendingDeleteRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { pendingDeleteRecord = null },
            title = { Text("确认删除") },
            text = { Text("确定删除这条记录吗？删除后不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(record.id)
                        pendingDeleteRecord = null
                    }
                ) { Text("删除", color = Expense) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteRecord = null }) { Text("取消") }
            }
        )
    }
}

@Composable
fun StatisticsScreen(
    ui: StatisticsUi,
    onPeriodChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onYearChange: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("month" to "按月", "year" to "按年").forEach { (value, label) ->
                    Button(
                        onClick = { onPeriodChange(value) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (ui.period == value) Brand else BrandSurface,
                            contentColor = if (ui.period == value) Color.White else Color(0xFF334155)
                        )
                    ) {
                        Text(label)
                    }
                }
            }
        }

        item {
            if (ui.period == "month") {
                val current = runCatching { YearMonth.parse(ui.month) }.getOrElse { YearMonth.now() }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { onMonthChange(current.minusMonths(1).toString()) }) { Text("上月") }
                    Text(ui.month, fontWeight = FontWeight.SemiBold)
                    OutlinedButton(onClick = { onMonthChange(current.plusMonths(1).toString()) }) { Text("下月") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { onYearChange(ui.year - 1) }) { Text("上一年") }
                    Text("${ui.year}", fontWeight = FontWeight.SemiBold)
                    OutlinedButton(onClick = { onYearChange(ui.year + 1) }) { Text("下一年") }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard("总支出", money(ui.stats.expense), Expense, Modifier.weight(1f))
                SummaryCard("总收入", money(ui.stats.income), Income, Modifier.weight(1f))
            }
        }
        item {
            SummaryCard("结余", money(ui.stats.balance), Brand, Modifier.fillMaxWidth())
        }
        item {
            PieChartCard(
                title = "支出分类饼图",
                centerTitle = "总支出",
                stats = ui.expenseStats,
                emptyTip = "暂无支出分类数据"
            )
        }
        item {
            PieChartCard(
                title = "收入分类饼图",
                centerTitle = "总收入",
                stats = ui.incomeStats,
                emptyTip = "暂无收入分类数据"
            )
        }
        item {
            PieChartCard(
                title = "收支对比饼图",
                centerTitle = "总收支",
                stats = listOf(
                    CategoryStat(-1, "支出", ui.stats.expense, 0),
                    CategoryStat(-2, "收入", ui.stats.income, 0)
                ),
                emptyTip = "暂无收支数据"
            )
        }
        item { TrendBarChartCard(if (ui.period == "month") "日度收支趋势柱状图" else "年度收支趋势柱状图", ui.trend) }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun CategoriesScreen(
    categories: List<Category>,
    onAdd: (type: String, icon: String, name: String, description: String, sort: Int) -> Unit,
    onUpdate: (id: Int, type: String, icon: String, name: String, description: String, sort: Int) -> Unit,
    onDelete: (id: Int) -> Unit
) {
    var editing by remember { mutableStateOf<Category?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        CategoryEditorDialog(
            initial = null,
            onDismiss = { showAdd = false },
            onSubmit = { type, icon, name, description, sort ->
                onAdd(type, icon, name, description, sort)
                showAdd = false
            }
        )
    }
    editing?.let { item ->
        CategoryEditorDialog(
            initial = item,
            onDismiss = { editing = null },
            onSubmit = { type, icon, name, description, sort ->
                onUpdate(item.id, type, icon, name, description, sort)
                editing = null
            }
        )
    }

    val sorted = categories.sortedWith(
        compareBy<Category> { it.type }.thenBy { it.sort ?: Int.MAX_VALUE }.thenBy { it.id }
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("分类管理")
            Button(
                onClick = { showAdd = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand)
            ) { Text("新增") }
        }
        if (sorted.isEmpty()) {
            EmptyTip("暂无分类")
            return@Column
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sorted) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${item.icon}  ${item.name}", fontWeight = FontWeight.SemiBold)
                            Text(
                                "${if (item.type == "expense") "支出" else "收入"} · sort=${item.sort ?: 99}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "编辑",
                                color = Brand,
                                modifier = Modifier
                                    .background(BrandSurface, RoundedCornerShape(8.dp))
                                    .clickable { editing = item }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                            Text(
                                "删除",
                                color = Expense,
                                modifier = Modifier
                                    .background(Expense.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { onDelete(item.id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun CategoryEditorDialog(
    initial: Category?,
    onDismiss: () -> Unit,
    onSubmit: (type: String, icon: String, name: String, description: String, sort: Int) -> Unit
) {
    var type by remember(initial?.id) { mutableStateOf(initial?.type ?: "expense") }
    var icon by remember(initial?.id) { mutableStateOf(initial?.icon ?: "🍽️") }
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var description by remember(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var sort by remember(initial?.id) { mutableStateOf((initial?.sort ?: 99).toString()) }

    val presets = if (type == "expense") expenseIconPresets else incomeIconPresets
    LaunchedEffect(type) {
        if (!presets.contains(icon)) {
            icon = presets.firstOrNull() ?: icon
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (initial == null) "新增分类" else "编辑分类", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("expense" to "支出", "income" to "收入").forEach { (value, label) ->
                        Button(
                            onClick = { type = value },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == value) Brand else BrandSurface,
                                contentColor = if (type == value) Color.White else Color(0xFF334155)
                            )
                        ) { Text(label) }
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 5
                ) {
                    presets.forEach { item ->
                        Text(
                            text = item,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .background(
                                    if (icon == item) Brand.copy(alpha = 0.14f) else BrandSurface,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { icon = item }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sort,
                    onValueChange = { sort = it.filter { c -> c.isDigit() } },
                    label = { Text("排序值 sort") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("排序值越小排在越前面", fontSize = 12.sp, color = TextMuted)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("取消") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSubmit(type, icon, name.trim(), description.trim(), sort.toIntOrNull() ?: 99)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand)
                    ) { Text("保存") }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun SettingsScreen(
    ui: BudgetUi,
    selectedTheme: AppThemePreset,
    onThemeChange: (AppThemePreset) -> Unit,
    onSetBudget: (Double) -> Unit,
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDialog) {
        SetBudgetDialog(
            onDismiss = { showDialog = false },
            onSave = {
                onSetBudget(it)
                showDialog = false
            }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("设置")
        SummaryCard("月度预算", money(ui.budget.budget), Brand, Modifier.fillMaxWidth())
        SummaryCard("已支出", money(ui.budget.spent), Expense, Modifier.fillMaxWidth())
        SummaryCard("预算剩余", money(ui.budget.remaining), Income, Modifier.fillMaxWidth())

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurface)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("每日可用 ${money(ui.dailyAvailable)}", fontWeight = FontWeight.SemiBold)
                Text("剩余 ${ui.daysRemaining} 天", color = TextMuted)
            }
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brand)
        ) {
            Text("设置预算")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("定时提醒", fontWeight = FontWeight.SemiBold)
                        Text("每天提醒记账", color = TextMuted, fontSize = 13.sp)
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { onReminderEnabledChange(it) }
                    )
                }
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> onReminderTimeChange(hour, minute) },
                            reminderHour,
                            reminderMinute,
                            true
                        ).show()
                    },
                    enabled = reminderEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("提醒时间  ${String.format(Locale.getDefault(), "%02d:%02d", reminderHour, reminderMinute)}")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("主题外观", fontWeight = FontWeight.SemiBold)
                Text("选择你喜欢的主题风格", color = TextMuted, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("明亮系", color = TextMuted, fontSize = 12.sp)
                    Text("暗黑系", color = TextMuted, fontSize = 12.sp)
                }
                val lightThemes = listOf(
                    AppThemePreset.Ocean,
                    AppThemePreset.Mint,
                    AppThemePreset.Sunset,
                    AppThemePreset.Graphite
                )
                val darkThemes = listOf(
                    AppThemePreset.Midnight,
                    AppThemePreset.Obsidian,
                    AppThemePreset.Nocturne,
                    AppThemePreset.Carbon
                )
                repeat(4) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val left = lightThemes[index]
                        val leftActive = selectedTheme == left
                        Text(
                            text = left.label,
                            color = if (leftActive) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .background(if (leftActive) Brand else BrandSurface, RoundedCornerShape(12.dp))
                                .clickable { onThemeChange(left) }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            textAlign = TextAlign.Center
                        )

                        val right = darkThemes[index]
                        val rightActive = selectedTheme == right
                        Text(
                            text = right.label,
                            color = if (rightActive) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .background(if (rightActive) Brand else BrandSurface, RoundedCornerShape(12.dp))
                                .clickable { onThemeChange(right) }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = TextMuted, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
        }
    }
}

@Composable
private fun StatRow(title: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Medium)
        Text(value, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PieChartCard(
    title: String,
    centerTitle: String,
    stats: List<CategoryStat>,
    emptyTip: String
) {
    val validStats = stats.filter { it.amount > 0 }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            if (validStats.isEmpty()) {
                EmptyTip(emptyTip)
            } else {
                val total = validStats.sumOf { it.amount }
                val chartColors = listOf(
                    Color(0xFF0F7E76), Color(0xFF19A59A), Color(0xFF4BC7B8), Color(0xFF7ADBCF),
                    Color(0xFFB4EDE3), Color(0xFF4F46E5), Color(0xFFEA580C), Color(0xFFDC2626)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(188.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier.matchParentSize()
                        ) {
                            var start = -90f
                            validStats.forEachIndexed { index, stat ->
                                val sweep = (stat.amount / total * 360f).toFloat()
                                drawArc(
                                    color = chartColors[index % chartColors.size],
                                    startAngle = start,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = 34f)
                                )
                                start += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(centerTitle, color = TextMuted, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(money(total), fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Color(0xFF0F172A))
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for ((index, stat) in validStats.withIndex()) {
                            val percent = stat.amount / total * 100
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(chartColors[index % chartColors.size], RoundedCornerShape(12.dp))
                                    )
                                    Text(stat.categoryName, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                                }
                                Text(
                                    "${money(stat.amount)} · ${"%.1f".format(percent)}%",
                                    color = TextMuted,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }                
            }
        }
    }
}

@Composable
private fun TrendBarChartCard(title: String, trend: TrendData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            val labels = if (trend.labels.isNotEmpty()) trend.labels else trend.dates.map { it.takeLast(5) }
            val expenses = trend.expenses
            val incomes = trend.incomes
            val maxValue = (expenses + incomes).maxOrNull()?.takeIf { it > 0 } ?: 1.0

            if (labels.isEmpty() || expenses.isEmpty() || incomes.isEmpty()) {
                EmptyTip("暂无趋势数据")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(labels.size) { index ->
                        val expense = expenses.getOrElse(index) { 0.0 }
                        val income = incomes.getOrElse(index) { 0.0 }
                        val expenseH = (expense / maxValue * 90).toFloat().coerceAtLeast(2f)
                        val incomeH = (income / maxValue * 90).toFloat().coerceAtLeast(2f)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier.height(100.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 10.dp, height = expenseH.dp)
                                        .background(Expense, RoundedCornerShape(4.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 10.dp, height = incomeH.dp)
                                        .background(Income, RoundedCornerShape(4.dp))
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(labels[index], fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("■ 支出", color = Expense, fontSize = 12.sp)
                    Text("■ 收入", color = Income, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun RecordRow(
    record: RecordItem,
    showDelete: Boolean,
    showEdit: Boolean,
    onDelete: (Int) -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clickable(enabled = showEdit, onClick = onEdit)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text("${record.categoryName} · ${record.date}", fontWeight = FontWeight.SemiBold)
            if (!record.remark.isNullOrBlank()) {
                Text(record.remark, color = TextMuted, fontSize = 13.sp)
            }
        }
        Text(
            text = (if (record.type == "expense") "-" else "+") + money(record.amount),
            color = if (record.type == "expense") Expense else Income,
            fontWeight = FontWeight.Bold
        )
        if (showDelete) {
            Spacer(Modifier.size(8.dp))
            Text(
                text = "删除",
                color = Expense,
                modifier = Modifier
                    .background(Expense.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    .clickable { onDelete(record.id) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun EmptyTip(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandSurface, RoundedCornerShape(14.dp))
            .padding(vertical = 24.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextMuted, textAlign = TextAlign.Center)
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun AddRecordDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSubmit: (type: String, categoryId: Int, amount: Double, remark: String, date: String) -> Unit
) {
    val context = LocalContext.current
    var type by remember { mutableStateOf("expense") }
    var categoryId by remember(categories) { mutableStateOf(defaultCategoryId(categories, "expense")) }
    var date by remember { mutableStateOf(todayString()) }
    var amountExpression by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }

    val currentCategories = categories.filter { it.type == type }
    val amountValue = evaluateAmountExpression(amountExpression)
    val amountDisplay = if (amountValue <= 0.0) "0" else moneyFmt.format(amountValue)

    LaunchedEffect(type, categories) {
        val valid = categories.any { it.id == categoryId && it.type == type }
        if (!valid) {
            categoryId = defaultCategoryId(categories, type)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("记一笔", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "关闭",
                        color = TextMuted,
                        modifier = Modifier
                            .background(BrandSurface, RoundedCornerShape(10.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            type = "expense"
                            categoryId = defaultCategoryId(categories, "expense")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "expense") Expense else BrandSurface,
                            contentColor = if (type == "expense") Color.White else Color(0xFF1E293B)
                        )
                    ) { Text("支出") }
                    Button(
                        onClick = {
                            type = "income"
                            categoryId = defaultCategoryId(categories, "income")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "income") Income else BrandSurface,
                            contentColor = if (type == "income") Color.White else Color(0xFF1E293B)
                        )
                    ) { Text("收入") }
                }

                if (currentCategories.isEmpty()) {
                    Text("暂无可选分类", color = TextMuted, fontSize = 13.sp)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 4
                    ) {
                        currentCategories.take(8).forEach { c ->
                            Text(
                                text = "${c.icon} ${c.name}",
                                color = if (categoryId == c.id) Color.White else Color(0xFF334155),
                                modifier = Modifier
                                    .background(
                                        if (categoryId == c.id) Brand else BrandSurface,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { categoryId = c.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("输入备注") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandSurface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("金额", color = TextMuted)
                    Text("¥$amountDisplay", color = Color(0xFFFF5C89), fontWeight = FontWeight.Bold, fontSize = 26.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = date,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .background(BrandSurface, RoundedCornerShape(12.dp))
                            .clickable {
                                showDatePicker(context, date) {
                                    date = it
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                    Text(
                        text = "今天",
                        color = Color.White,
                        modifier = Modifier
                            .background(Brand, RoundedCornerShape(12.dp))
                            .clickable { date = todayString() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        listOf("1", "2", "3", "删"),
                        listOf("4", "5", "6", "+"),
                        listOf("7", "8", "9", "-"),
                        listOf(".", "0", "00", "完成")
                    ).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { key ->
                                KeyPadButton(
                                    text = key,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        when (key) {
                                            "删" -> {
                                                if (amountExpression.isNotEmpty()) {
                                                    amountExpression = amountExpression.dropLast(1)
                                                }
                                            }
                                            "+" -> amountExpression = appendOperator(amountExpression, '+')
                                            "-" -> amountExpression = appendOperator(amountExpression, '-')
                                            "." -> amountExpression = appendDot(amountExpression)
                                            "完成" -> {
                                                onSubmit(type, categoryId, amountValue, remark, date)
                                            }
                                            "00" -> amountExpression = appendDigit(amountExpression, "00")
                                            else -> amountExpression = appendDigit(amountExpression, key)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditRecordDialog(
    record: RecordItem,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (type: String, categoryId: Int, amount: Double, remark: String, date: String) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var type by remember(record.id) { mutableStateOf(record.type) }
    var categoryId by remember(record.id) { mutableStateOf(record.categoryId) }
    var date by remember(record.id) { mutableStateOf(record.date) }
    var remark by remember(record.id) { mutableStateOf(record.remark.orEmpty()) }
    var amountExpression by remember(record.id) { mutableStateOf(moneyFmt.format(record.amount)) }

    val currentCategories = categories.filter { it.type == type }
    val amountValue = evaluateAmountExpression(amountExpression)
    val amountDisplay = if (amountValue <= 0.0) "0.00" else moneyFmt.format(amountValue)

    LaunchedEffect(type, categories) {
        val valid = categories.any { it.id == categoryId && it.type == type }
        if (!valid) {
            categoryId = defaultCategoryId(categories, type)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("编辑记录", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            type = "expense"
                            categoryId = defaultCategoryId(categories, "expense")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "expense") Expense else BrandSurface,
                            contentColor = if (type == "expense") Color.White else Color(0xFF1E293B)
                        )
                    ) { Text("支出") }
                    Button(
                        onClick = {
                            type = "income"
                            categoryId = defaultCategoryId(categories, "income")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "income") Income else BrandSurface,
                            contentColor = if (type == "income") Color.White else Color(0xFF1E293B)
                        )
                    ) { Text("收入") }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentCategories) { c ->
                        Text(
                            text = "${c.icon} ${c.name}",
                            color = if (categoryId == c.id) Color.White else Color(0xFF334155),
                            modifier = Modifier
                                .background(
                                    if (categoryId == c.id) Brand else BrandSurface,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { categoryId = c.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandSurface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("金额", color = TextMuted)
                    Text("¥$amountDisplay", color = Brand, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = date,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .background(BrandSurface, RoundedCornerShape(12.dp))
                            .clickable { showDatePicker(context, date) { date = it } }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                    Text(
                        text = "今天",
                        color = Color.White,
                        modifier = Modifier
                            .background(Brand, RoundedCornerShape(12.dp))
                            .clickable { date = todayString() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        listOf("1", "2", "3", "删"),
                        listOf("4", "5", "6", "+"),
                        listOf("7", "8", "9", "-"),
                        listOf(".", "0", "00", "完成")
                    ).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { key ->
                                KeyPadButton(
                                    text = key,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        when (key) {
                                            "删" -> if (amountExpression.isNotEmpty()) {
                                                amountExpression = amountExpression.dropLast(1)
                                            }
                                            "+" -> amountExpression = appendOperator(amountExpression, '+')
                                            "-" -> amountExpression = appendOperator(amountExpression, '-')
                                            "." -> amountExpression = appendDot(amountExpression)
                                            "完成" -> onUpdate(type, categoryId, amountValue, remark, date)
                                            "00" -> amountExpression = appendDigit(amountExpression, "00")
                                            else -> amountExpression = appendDigit(amountExpression, key)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Expense)
                    ) { Text("删除", color = Color.White) }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSurface, contentColor = Color(0xFF1E293B))
                    ) { Text("取消") }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定删除这条记录吗？删除后不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) { Text("删除", color = Expense) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun KeyPadButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val (background, contentColor) = when (text) {
        "完成" -> Brand to Color.White
        "删", "+", "-", "00" -> Color(0xFFDCEAF7) to Color(0xFF1E293B)
        else -> Color(0xFFF4F7FB) to Color(0xFF1E293B)
    }
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = contentColor, fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
    }
}

private fun appendDigit(expression: String, key: String): String {
    if (expression.length >= 18) return expression
    return expression + key
}

private fun appendOperator(expression: String, operator: Char): String {
    if (expression.isEmpty()) return expression
    val last = expression.last()
    return if (last == '+' || last == '-') {
        expression.dropLast(1) + operator
    } else {
        expression + operator
    }
}

private fun appendDot(expression: String): String {
    if (expression.length >= 18) return expression
    if (expression.isEmpty() || expression.last() == '+' || expression.last() == '-') return expression + "0."
    val segment = expression.takeLastWhile { it != '+' && it != '-' }
    if (segment.contains('.')) return expression
    return expression + "."
}

private fun evaluateAmountExpression(expression: String): Double {
    val clean = expression.trim().trimEnd('+', '-')
    if (clean.isEmpty()) return 0.0

    var total = 0.0
    var current = StringBuilder()
    var sign = 1.0

    clean.forEachIndexed { index, c ->
        when {
            c.isDigit() || c == '.' -> current.append(c)
            c == '+' || c == '-' -> {
                val number = current.toString().toDoubleOrNull() ?: 0.0
                total += sign * number
                current = StringBuilder()
                sign = if (c == '+') 1.0 else -1.0
            }
            index == clean.lastIndex -> {
                // no-op, only to keep exhaustive when with index available
            }
        }
    }

    if (current.isNotEmpty()) {
        val number = current.toString().toDoubleOrNull() ?: 0.0
        total += sign * number
    }
    return total
}

private fun showDatePicker(context: Context, currentDate: String, onSelect: (String) -> Unit) {
    val parts = currentDate.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = parts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: 0
    val day = parts.getOrNull(2)?.toIntOrNull() ?: 1

    DatePickerDialog(
        context,
        { _, y, m, d ->
            onSelect(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d))
        },
        year,
        month,
        day
    ).show()
}

@Composable
fun SetBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置预算") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("月度预算金额") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(amount.toDoubleOrNull() ?: 0.0) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
