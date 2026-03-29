const API_BASE_URL = 'http://localhost:8080/api';

function formatYearMonthLocal(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
}

function getCurrentYearMonthLocal() {
    return formatYearMonthLocal(new Date());
}

function shiftYearMonth(monthStr, delta) {
    const [year, month] = monthStr.split('-').map(Number);
    const next = new Date(year, month - 1 + delta, 1);
    return formatYearMonthLocal(next);
}

function getTodayLocalDateString() {
    const date = new Date();
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

const state = {
    currentPage: 'dashboard',
    categories: [],
    records: [],
    currentMonth: getCurrentYearMonthLocal(),
    budgetMonth: getCurrentYearMonthLocal(),
    statisticsYear: new Date().getFullYear(),
    statisticsMode: 'month',
    pagination: {
        page: 1,
        size: 10,
        total: 0
    },
    filters: {
        startDate: '',
        endDate: '',
        type: '',
        categoryId: '',
        keyword: ''
    }
};

const dom = {
    navItems: document.querySelectorAll('.nav-item'),
    pageTitle: document.getElementById('pageTitle'),
    currentDate: document.getElementById('currentDate'),
    addRecordBtn: document.getElementById('addRecordBtn'),
    pages: document.querySelectorAll('.page'),
    monthIncome: document.getElementById('monthIncome'),
    monthExpense: document.getElementById('monthExpense'),
    monthBalance: document.getElementById('monthBalance'),
    budgetRemaining: document.getElementById('budgetRemaining'),
    budgetProgress: document.getElementById('budgetProgress'),
    dashboardDailyAvailable: document.getElementById('dashboardDailyAvailable'),
    dashboardDailyMeta: document.getElementById('dashboardDailyMeta'),
    dashboardBudgetUsage: document.getElementById('dashboardBudgetUsage'),
    dashboardBudgetMeta: document.getElementById('dashboardBudgetMeta'),
    recentRecordsList: document.getElementById('recentRecordsList'),
    categoryChart: document.getElementById('categoryChart'),
    recordsTableBody: document.getElementById('recordsTableBody'),
    pagination: document.getElementById('pagination'),
    filterStartDate: document.getElementById('filterStartDate'),
    filterEndDate: document.getElementById('filterEndDate'),
    filterType: document.getElementById('filterType'),
    filterCategory: document.getElementById('filterCategory'),
    filterKeyword: document.getElementById('filterKeyword'),
    filterBtn: document.getElementById('filterBtn'),
    resetFilterBtn: document.getElementById('resetFilterBtn'),
    recordModal: document.getElementById('recordModal'),
    recordForm: document.getElementById('recordForm'),
    recordId: document.getElementById('recordId'),
    recordModalTitle: document.getElementById('recordModalTitle'),
    categorySelect: document.getElementById('categorySelect'),
    recordDate: document.getElementById('recordDate'),
    amount: document.getElementById('amount'),
    remark: document.getElementById('remark'),
    closeRecordModal: document.getElementById('closeRecordModal'),
    cancelRecordBtn: document.getElementById('cancelRecordBtn'),
    categoryModal: document.getElementById('categoryModal'),
    categoryForm: document.getElementById('categoryForm'),
    categoryId: document.getElementById('categoryId'),
    categoryType: document.getElementById('categoryType'),
    categoryModalTitle: document.getElementById('categoryModalTitle'),
    categoryName: document.getElementById('categoryName'),
    categoryDescription: document.getElementById('categoryDescription'),
    selectedIcon: document.getElementById('selectedIcon'),
    iconSelector: document.getElementById('iconSelector'),
    closeCategoryModal: document.getElementById('closeCategoryModal'),
    cancelCategoryBtn: document.getElementById('cancelCategoryBtn'),
    addCategoryBtn: document.getElementById('addCategoryBtn'),
    categoriesGrid: document.getElementById('categoriesGrid'),
    categoryTabs: document.querySelectorAll('.tab-btn'),
    currentMonth: document.getElementById('currentMonth'),
    prevMonth: document.getElementById('prevMonth'),
    nextMonth: document.getElementById('nextMonth'),
    statsMonthModeBtn: document.getElementById('statsMonthModeBtn'),
    statsYearModeBtn: document.getElementById('statsYearModeBtn'),
    statsMonthSelector: document.getElementById('statsMonthSelector'),
    statsYearSelector: document.getElementById('statsYearSelector'),
    currentYear: document.getElementById('currentYear'),
    prevYear: document.getElementById('prevYear'),
    nextYear: document.getElementById('nextYear'),
    statExpense: document.getElementById('statExpense'),
    statIncome: document.getElementById('statIncome'),
    statBalance: document.getElementById('statBalance'),
    statAuxiliary: document.getElementById('statAuxiliary'),
    statAuxiliaryLabel: document.getElementById('statAuxiliaryLabel'),
    expenseCategoryStats: document.getElementById('expenseCategoryStats'),
    incomeCategoryStats: document.getElementById('incomeCategoryStats'),
    expenseCategoryPieChart: document.getElementById('expenseCategoryPieChart'),
    incomeCategoryPieChart: document.getElementById('incomeCategoryPieChart'),
    trendChart: document.getElementById('trendChart'),
    trendChartTitle: document.getElementById('trendChartTitle'),
    budgetCurrentMonth: document.getElementById('budgetCurrentMonth'),
    budgetPrevMonth: document.getElementById('budgetPrevMonth'),
    budgetNextMonth: document.getElementById('budgetNextMonth'),
    budgetAmount: document.getElementById('budgetAmount'),
    budgetSpent: document.getElementById('budgetSpent'),
    budgetRemainingDetail: document.getElementById('budgetRemainingDetail'),
    dailyAvailable: document.getElementById('dailyAvailable'),
    daysRemaining: document.getElementById('daysRemaining'),
    budgetPercent: document.getElementById('budgetPercent'),
    budgetProgressFill: document.getElementById('budgetProgressFill'),
    editBudgetBtn: document.getElementById('editBudgetBtn'),
    budgetModal: document.getElementById('budgetModal'),
    budgetForm: document.getElementById('budgetForm'),
    budgetAmountInput: document.getElementById('budgetAmountInput'),
    closeBudgetModal: document.getElementById('closeBudgetModal'),
    cancelBudgetBtn: document.getElementById('cancelBudgetBtn'),
    toastContainer: document.getElementById('toastContainer')
};

function init() {
    updateCurrentDate();
    setupEventListeners();
    loadCategories();
    navigateTo('dashboard');
}

function updateCurrentDate() {
    const now = new Date();
    const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
    dom.currentDate.textContent = now.toLocaleDateString('zh-CN', options);
}

function setupEventListeners() {
    dom.navItems.forEach(item => {
        item.addEventListener('click', () => {
            const page = item.dataset.page;
            navigateTo(page);
        });
    });

    document.querySelectorAll('.view-all').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const page = link.dataset.page;
            navigateTo(page);
        });
    });

    dom.addRecordBtn.addEventListener('click', () => openRecordModal());

    dom.closeRecordModal.addEventListener('click', () => closeModal('record'));
    dom.cancelRecordBtn.addEventListener('click', () => closeModal('record'));
    dom.recordModal.querySelector('.modal-overlay').addEventListener('click', () => closeModal('record'));

    dom.recordForm.addEventListener('submit', handleRecordSubmit);

    document.querySelectorAll('input[name="type"]').forEach(radio => {
        radio.addEventListener('change', () => updateCategoryOptions());
    });

    dom.closeCategoryModal.addEventListener('click', () => closeModal('category'));
    dom.cancelCategoryBtn.addEventListener('click', () => closeModal('category'));
    dom.categoryModal.querySelector('.modal-overlay').addEventListener('click', () => closeModal('category'));

    dom.categoryForm.addEventListener('submit', handleCategorySubmit);

    dom.iconSelector.addEventListener('click', (e) => {
        const iconOption = e.target.closest('.icon-option');
        if (iconOption) {
            document.querySelectorAll('.icon-option').forEach(opt => opt.classList.remove('selected'));
            iconOption.classList.add('selected');
            dom.selectedIcon.value = iconOption.dataset.icon;
        }
    });

    dom.addCategoryBtn.addEventListener('click', () => openCategoryModal());

    dom.categoryTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            dom.categoryTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            loadCategoriesGrid(tab.dataset.type);
        });
    });

    dom.filterBtn.addEventListener('click', () => {
        state.pagination.page = 1;
        loadRecords();
    });

    dom.resetFilterBtn.addEventListener('click', () => {
        dom.filterStartDate.value = '';
        dom.filterEndDate.value = '';
        dom.filterType.value = '';
        dom.filterCategory.value = '';
        dom.filterKeyword.value = '';
        state.filters = { startDate: '', endDate: '', type: '', categoryId: '', keyword: '' };
        state.pagination.page = 1;
        loadRecords();
    });

    dom.prevMonth.addEventListener('click', () => changeMonth(-1, 'stats'));
    dom.nextMonth.addEventListener('click', () => changeMonth(1, 'stats'));
    dom.statsMonthModeBtn.addEventListener('click', () => changeStatisticsMode('month'));
    dom.statsYearModeBtn.addEventListener('click', () => changeStatisticsMode('year'));
    dom.prevYear.addEventListener('click', () => changeYear(-1));
    dom.nextYear.addEventListener('click', () => changeYear(1));

    dom.budgetPrevMonth.addEventListener('click', () => changeMonth(-1, 'budget'));
    dom.budgetNextMonth.addEventListener('click', () => changeMonth(1, 'budget'));

    dom.editBudgetBtn.addEventListener('click', openBudgetModal);
    dom.closeBudgetModal.addEventListener('click', () => closeModal('budget'));
    dom.cancelBudgetBtn.addEventListener('click', () => closeModal('budget'));
    dom.budgetModal.querySelector('.modal-overlay').addEventListener('click', () => closeModal('budget'));
    dom.budgetForm.addEventListener('submit', handleBudgetSubmit);
}

function navigateTo(page) {
    state.currentPage = page;

    dom.navItems.forEach(item => {
        item.classList.toggle('active', item.dataset.page === page);
    });

    dom.pages.forEach(p => {
        p.classList.toggle('active', p.id === `${page}Page`);
    });

    const titles = {
        dashboard: '概览',
        records: '记账记录',
        statistics: '统计分析',
        categories: '分类管理',
        budget: '预算管理'
    };
    dom.pageTitle.textContent = titles[page] || page;

    switch (page) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'records':
            loadRecords();
            break;
        case 'statistics':
            loadStatistics();
            break;
        case 'categories':
            loadCategoriesGrid('expense');
            break;
        case 'budget':
            loadBudget();
            break;
    }
}

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json'
        }
    };

    const mergedOptions = { ...defaultOptions, ...options };
    if (options.body && typeof options.body === 'object') {
        mergedOptions.body = JSON.stringify(options.body);
    }

    try {
        const response = await fetch(url, mergedOptions);
        const data = await response.json();

        if (data.code !== 200) {
            throw new Error(data.message || '请求失败');
        }

        return data.data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

async function loadCategories() {
    try {
        state.categories = await apiRequest('/categories');
        updateCategoryFilter();
    } catch (error) {
        state.categories = getDefaultCategories();
        updateCategoryFilter();
    }
}

function getDefaultCategories() {
    return [
        { id: 1, type: 'expense', icon: '🍽️', name: '餐饮', description: '餐饮消费', sort: 1 },
        { id: 2, type: 'expense', icon: '🚇', name: '交通', description: '交通出行', sort: 2 },
        { id: 3, type: 'expense', icon: '🛍️', name: '购物', description: '日常购物', sort: 3 },
        { id: 4, type: 'expense', icon: '🎮', name: '娱乐', description: '休闲娱乐', sort: 4 },
        { id: 5, type: 'expense', icon: '🏥', name: '医疗', description: '医疗健康', sort: 5 },
        { id: 6, type: 'expense', icon: '📚', name: '教育', description: '教育培训', sort: 6 },
        { id: 7, type: 'expense', icon: '🏠', name: '住房', description: '房租水电', sort: 7 },
        { id: 8, type: 'expense', icon: '📱', name: '通讯', description: '手机网络', sort: 8 },
        { id: 9, type: 'income', icon: '💼', name: '工资', description: '工资收入', sort: 1 },
        { id: 10, type: 'income', icon: '🎁', name: '奖金', description: '奖金收入', sort: 2 },
        { id: 11, type: 'income', icon: '📈', name: '投资', description: '投资收益', sort: 3 },
        { id: 12, type: 'income', icon: '🧑‍💻', name: '兼职', description: '兼职收入', sort: 4 }
    ];
}

function updateCategoryFilter() {
    dom.filterCategory.innerHTML = '<option value="">全部</option>';
    state.categories.forEach(cat => {
        const option = document.createElement('option');
        option.value = cat.id;
        option.textContent = `${cat.icon} ${cat.name}`;
        dom.filterCategory.appendChild(option);
    });
}

function updateCategoryOptions() {
    const type = document.querySelector('input[name="type"]:checked').value;
    dom.categorySelect.innerHTML = '<option value="">请选择分类</option>';

    const filteredCategories = state.categories.filter(cat => cat.type === type);
    filteredCategories.forEach(cat => {
        const option = document.createElement('option');
        option.value = cat.id;
        option.textContent = `${cat.icon} ${cat.name}`;
        dom.categorySelect.appendChild(option);
    });
}

async function loadDashboard() {
    try {
        const [monthlyStats, budgetData, dailyBudgetData, recordsData, categoryStats] = await Promise.all([
            apiRequest(`/statistics/monthly?month=${state.currentMonth}`).catch(() => ({ expense: 0, income: 0, balance: 0 })),
            apiRequest(`/budget?month=${state.currentMonth}`).catch(() => ({ budget: 0, spent: 0, remaining: 0 })),
            apiRequest(`/budget/daily-available?month=${state.currentMonth}`).catch(() => ({ dailyAvailable: 0, daysRemaining: 0 })),
            apiRequest('/records?page=1&size=5').catch(() => ({ records: [], total: 0 })),
            apiRequest(`/statistics/category?month=${state.currentMonth}&type=expense`).catch(() => [])
        ]);

        updateDashboardUI(monthlyStats, budgetData, dailyBudgetData, recordsData.records, categoryStats);
    } catch (error) {
        showToast('加载概览数据失败，使用模拟数据', 'warning');
        const mockData = getMockDashboardData();
        updateDashboardUI(mockData.stats, mockData.budget, mockData.dailyBudget, mockData.records, mockData.categoryStats);
    }
}

function getMockDashboardData() {
    return {
        stats: { expense: 3500, income: 8000, balance: 4500 },
        budget: { budget: 5000, spent: 3500, remaining: 1500 },
        dailyBudget: { dailyAvailable: 75, daysRemaining: 20 },
        records: [
            { id: 1, date: '2024-01-15', type: 'expense', categoryId: 1, categoryName: '餐饮', amount: 35.50, remark: '午餐' },
            { id: 2, date: '2024-01-15', type: 'expense', categoryId: 2, categoryName: '交通', amount: 15.00, remark: '地铁' },
            { id: 3, date: '2024-01-14', type: 'income', categoryId: 9, categoryName: '工资', amount: 8000, remark: '1月工资' }
        ],
        categoryStats: [
            { categoryId: 1, categoryName: '餐饮', amount: 1200, count: 35 },
            { categoryId: 2, categoryName: '交通', amount: 500, count: 20 },
            { categoryId: 3, categoryName: '购物', amount: 800, count: 10 }
        ]
    };
}

function updateDashboardUI(stats, budget, dailyBudget, records, categoryStats) {
    dom.monthIncome.textContent = formatCurrency(stats.income);
    dom.monthExpense.textContent = formatCurrency(stats.expense);
    dom.monthBalance.textContent = formatCurrency(stats.balance);
    dom.budgetRemaining.textContent = formatCurrency(budget.remaining);

    const progressPercent = budget.budget > 0 ? Math.min((budget.spent / budget.budget) * 100, 100) : 0;
    dom.budgetProgress.style.width = `${progressPercent}%`;
    dom.dashboardDailyAvailable.textContent = formatCurrency(dailyBudget.dailyAvailable || 0);
    dom.dashboardDailyMeta.textContent = `剩余 ${dailyBudget.daysRemaining || 0} 天，按预算节奏消费`;
    dom.dashboardBudgetUsage.textContent = `${progressPercent.toFixed(1)}%`;
    dom.dashboardBudgetMeta.textContent = budget.budget > 0
        ? `预算 ${formatCurrency(budget.budget)}，已支出 ${formatCurrency(budget.spent)}`
        : '本月预算尚未设置';

    if (records && records.length > 0) {
        dom.recentRecordsList.innerHTML = records.map(record => `
            <div class="overview-record-row">
                <div class="overview-record-main">
                    <div class="record-icon">${getCategoryIcon(record.categoryId)}</div>
                    <div class="record-info">
                        <div class="record-category">${record.categoryName}</div>
                        <div class="record-date">${record.date}${record.remark ? ` · ${record.remark}` : ''}</div>
                    </div>
                </div>
                <div class="record-amount ${record.type}">${record.type === 'expense' ? '-' : '+'}${formatCurrency(record.amount)}</div>
            </div>
        `).join('');
    } else {
        dom.recentRecordsList.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon">📝</span>
                <p>暂无记录，开始记账吧？/p>
            </div>
        `;
    }

    renderCategoryChart(categoryStats);
}

function getCategoryIcon(categoryId) {
    const category = state.categories.find(c => c.id === categoryId);
    return category ? category.icon : '🧾';
}

function renderCategoryChart(categoryStats) {
    if (!categoryStats || categoryStats.length === 0) {
        dom.categoryChart.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon">📊</span>
                <p>暂无数据</p>
            </div>
        `;
        return;
    }

    const total = categoryStats.reduce((sum, item) => sum + Number(item.amount || 0), 0);
    const colors = ['#0f766e', '#14b8a6', '#f59e0b', '#ef4444', '#3b82f6', '#8b5cf6', '#ec4899', '#84cc16'];
    const radius = 76;
    const circumference = 2 * Math.PI * radius;
    let offset = 0;

    const segments = categoryStats.map((item, index) => {
        const amount = Number(item.amount || 0);
        const percent = total > 0 ? amount / total : 0;
        const length = circumference * percent;
        const dasharray = `${length} ${circumference - length}`;
        const dashoffset = -offset;
        offset += length;
        return {
            ...item,
            amount,
            percent,
            color: colors[index % colors.length],
            dasharray,
            dashoffset
        };
    });

    dom.categoryChart.innerHTML = `
        <div class="donut-chart-layout">
            <div class="chart-pie">
                <svg class="donut-chart" viewBox="0 0 200 200" aria-label="支出分类饼图">
                    <circle class="donut-track" cx="100" cy="100" r="${radius}"></circle>
                    ${segments.map(segment => `
                        <circle
                            class="donut-segment"
                            cx="100"
                            cy="100"
                            r="${radius}"
                            stroke="${segment.color}"
                            stroke-dasharray="${segment.dasharray}"
                            stroke-dashoffset="${segment.dashoffset}"
                        ></circle>
                    `).join('')}
                </svg>
                <div class="pie-center">
                    <span class="pie-center-label">总支出</span>
                    <span class="pie-center-value">${formatCurrency(total)}</span>
                </div>
            </div>
            <div class="chart-legend chart-legend-column">
                ${segments.map(segment => `
                    <div class="legend-item">
                        <span class="legend-color" style="background: ${segment.color}"></span>
                        <div class="legend-content">
                            <span class="legend-name">${segment.categoryName}</span>
                            <span class="legend-meta">${formatCurrency(segment.amount)} · ${(segment.percent * 100).toFixed(1)}%</span>
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

async function loadRecords() {
    const params = new URLSearchParams({
        page: state.pagination.page,
        size: state.pagination.size
    });

    if (dom.filterStartDate.value) params.append('startDate', dom.filterStartDate.value);
    if (dom.filterEndDate.value) params.append('endDate', dom.filterEndDate.value);
    if (dom.filterType.value) params.append('type', dom.filterType.value);
    if (dom.filterCategory.value) params.append('categoryId', dom.filterCategory.value);
    if (dom.filterKeyword.value.trim()) params.append('keyword', dom.filterKeyword.value.trim());

    try {
        const data = await apiRequest(`/records?${params}`);
        state.records = data.records;
        state.pagination.total = data.total;
        renderRecordsTable();
        renderPagination();
    } catch (error) {
        showToast('加载记录失败，使用模拟数据', 'warning');
        const mockRecords = getMockRecords();
        state.records = mockRecords.records;
        state.pagination.total = mockRecords.total;
        renderRecordsTable();
        renderPagination();
    }
}

function getMockRecords() {
    return {
        records: [
            { id: 1, date: '2024-01-15', type: 'expense', categoryId: 1, categoryName: '餐饮', amount: 35.50, remark: '午餐' },
            { id: 2, date: '2024-01-15', type: 'expense', categoryId: 2, categoryName: '交通', amount: 15.00, remark: '地铁' },
            { id: 3, date: '2024-01-14', type: 'income', categoryId: 9, categoryName: '工资', amount: 8000, remark: '1月工资' },
            { id: 4, date: '2024-01-13', type: 'expense', categoryId: 3, categoryName: '购物', amount: 299.00, remark: '衣服' },
            { id: 5, date: '2024-01-12', type: 'expense', categoryId: 4, categoryName: '娱乐', amount: 88.00, remark: '电影' }
        ],
        total: 5
    };
}

function renderRecordsTable() {
    if (state.records.length === 0) {
        dom.recordsTableBody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 40px; color: var(--text-muted);">
                    暂无记录
                </td>
            </tr>
        `;
        return;
    }

    dom.recordsTableBody.innerHTML = state.records.map(record => `
        <tr>
            <td>${record.date}</td>
            <td><span class="type-badge ${record.type}">${record.type === 'expense' ? '支出' : '收入'}</span></td>
            <td>${getCategoryIcon(record.categoryId)} ${record.categoryName}</td>
            <td class="amount-cell ${record.type}">${record.type === 'expense' ? '-' : '+'}${formatCurrency(record.amount)}</td>
            <td>${record.remark || '-'}</td>
            <td>
                <div class="action-btns">
                    <button class="action-btn edit" onclick="editRecord(${record.id})" title="编辑">✎</button>
                    <button class="action-btn delete" onclick="deleteRecord(${record.id})" title="删除">🗑</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderPagination() {
    const totalPages = Math.ceil(state.pagination.total / state.pagination.size);
    const currentPage = state.pagination.page;

    if (totalPages <= 1) {
        dom.pagination.innerHTML = '';
        return;
    }

    let html = `<button ${currentPage === 1 ? 'disabled' : ''} onclick="goToPage(${currentPage - 1})">上一页</button>`;

    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPage - 1 && i <= currentPage + 1)) {
            html += `<button class="${i === currentPage ? 'active' : ''}" onclick="goToPage(${i})">${i}</button>`;
        } else if (i === currentPage - 2 || i === currentPage + 2) {
            html += '<button disabled>...</button>';
        }
    }

    html += `<button ${currentPage === totalPages ? 'disabled' : ''} onclick="goToPage(${currentPage + 1})">下一页</button>`;

    dom.pagination.innerHTML = html;
}

function goToPage(page) {
    state.pagination.page = page;
    loadRecords();
}

function openRecordModal(record = null) {
    dom.recordModal.classList.add('active');

    if (record) {
        dom.recordModalTitle.textContent = '编辑记录';
        dom.recordId.value = record.id;
        document.querySelector(`input[name="type"][value="${record.type}"]`).checked = true;
        dom.amount.value = record.amount;
        dom.recordDate.value = record.date;
        dom.remark.value = record.remark || '';
        updateCategoryOptions();
        setTimeout(() => {
            dom.categorySelect.value = record.categoryId;
        }, 0);
    } else {
        dom.recordModalTitle.textContent = '记一笔';
        dom.recordForm.reset();
        dom.recordId.value = '';
        dom.recordDate.value = getTodayLocalDateString();
        document.querySelector('input[name="type"][value="expense"]').checked = true;
        updateCategoryOptions();
    }
}

function closeModal(type) {
    document.getElementById(`${type}Modal`).classList.remove('active');
}

async function handleRecordSubmit(e) {
    e.preventDefault();

    const recordData = {
        date: dom.recordDate.value,
        type: document.querySelector('input[name="type"]:checked').value,
        categoryId: parseInt(dom.categorySelect.value),
        amount: parseFloat(dom.amount.value),
        remark: dom.remark.value
    };

    const recordId = dom.recordId.value;

    try {
        if (recordId) {
            await apiRequest(`/records/${recordId}`, {
                method: 'PUT',
                body: recordData
            });
            showToast('记录更新成功', 'success');
        } else {
            await apiRequest('/records', {
                method: 'POST',
                body: recordData
            });
            showToast('记录添加成功', 'success');
        }

        closeModal('record');
        loadRecords();
        loadDashboard();
    } catch (error) {
        showToast('操作失败，已模拟保存', 'warning');
        closeModal('record');
        loadRecords();
    }
}

async function editRecord(id) {
    try {
        const record = await apiRequest(`/records/${id}`);
        openRecordModal(record);
    } catch (error) {
        const record = state.records.find(r => r.id === id);
        if (record) {
            openRecordModal(record);
        }
    }
}

async function deleteRecord(id) {
    if (!confirm('确定要删除这条记录吗？')) return;

    try {
        await apiRequest(`/records/${id}`, { method: 'DELETE' });
        showToast('记录删除成功', 'success');
        loadRecords();
        loadDashboard();
    } catch (error) {
        showToast('删除失败，已模拟删除', 'warning');
        state.records = state.records.filter(r => r.id !== id);
        renderRecordsTable();
    }
}

async function loadCategoriesGrid(type) {
    dom.categoryType.value = type;

    try {
        const categories = await apiRequest('/categories');
        state.categories = categories;
        const filteredCategories = categories.filter(cat => cat.type === type);
        renderCategoriesGrid(filteredCategories);
    } catch (error) {
        const filteredCategories = state.categories.filter(cat => cat.type === type);
        renderCategoriesGrid(filteredCategories);
    }
}

function renderCategoriesGrid(categories) {
    if (categories.length === 0) {
        dom.categoriesGrid.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <span class="empty-icon">🗂️</span>
                <p>暂无分类</p>
            </div>
        `;
        return;
    }

    dom.categoriesGrid.innerHTML = categories.map(cat => `
        <div class="category-card">
            <div class="category-card-icon">${cat.icon}</div>
            <div class="category-card-name">${cat.name}</div>
            <div class="category-card-desc">${cat.description || ''}</div>
            <div class="category-card-actions">
                <button class="action-btn edit" onclick="editCategory(${cat.id})" title="编辑">✎</button>
                <button class="action-btn delete" onclick="deleteCategory(${cat.id})" title="删除">🗑</button>
            </div>
        </div>
    `).join('');
}

function openCategoryModal(category = null) {
    dom.categoryModal.classList.add('active');

    if (category) {
        dom.categoryModalTitle.textContent = '编辑分类';
        dom.categoryId.value = category.id;
        dom.categoryName.value = category.name;
        dom.categoryDescription.value = category.description || '';
        dom.selectedIcon.value = category.icon;

        document.querySelectorAll('.icon-option').forEach(opt => {
            opt.classList.toggle('selected', opt.dataset.icon === category.icon);
        });
    } else {
        dom.categoryModalTitle.textContent = '添加分类';
        dom.categoryForm.reset();
        dom.categoryId.value = '';
        dom.selectedIcon.value = '🍽️';

        document.querySelectorAll('.icon-option').forEach(opt => {
            opt.classList.toggle('selected', opt.dataset.icon === '🍽️');
        });
    }
}

async function handleCategorySubmit(e) {
    e.preventDefault();

    const categoryData = {
        type: dom.categoryType.value,
        icon: dom.selectedIcon.value,
        name: dom.categoryName.value,
        description: dom.categoryDescription.value
    };

    const categoryId = dom.categoryId.value;

    try {
        if (categoryId) {
            await apiRequest(`/categories/${categoryId}`, {
                method: 'PUT',
                body: categoryData
            });
            showToast('分类更新成功', 'success');
        } else {
            await apiRequest('/categories', {
                method: 'POST',
                body: categoryData
            });
            showToast('分类添加成功', 'success');
        }

        closeModal('category');
        loadCategories();
        loadCategoriesGrid(dom.categoryType.value);
    } catch (error) {
        showToast('操作失败，已模拟保存', 'warning');
        closeModal('category');

        if (categoryId) {
            const index = state.categories.findIndex(c => c.id === parseInt(categoryId));
            if (index !== -1) {
                state.categories[index] = { ...state.categories[index], ...categoryData };
            }
        } else {
            const newCategory = {
                id: Date.now(),
                ...categoryData
            };
            state.categories.push(newCategory);
        }

        loadCategoriesGrid(dom.categoryType.value);
    }
}

async function editCategory(id) {
    const category = state.categories.find(c => c.id === id);
    if (category) {
        openCategoryModal(category);
    }
}

async function deleteCategory(id) {
    if (!confirm('确定要删除这个分类吗？')) return;

    try {
        await apiRequest(`/categories/${id}`, { method: 'DELETE' });
        showToast('分类删除成功', 'success');
        loadCategories();
        loadCategoriesGrid(dom.categoryType.value);
    } catch (error) {
        showToast('删除失败，已模拟删除', 'warning');
        state.categories = state.categories.filter(c => c.id !== id);
        loadCategoriesGrid(dom.categoryType.value);
    }
}

function changeMonth(delta, type) {
    const currentMonthStr = type === 'stats' ? state.currentMonth : state.budgetMonth;
    const newMonth = shiftYearMonth(currentMonthStr, delta);

    if (type === 'stats') {
        state.currentMonth = newMonth;
        loadStatistics();
    } else {
        state.budgetMonth = newMonth;
        loadBudget();
    }
}

function changeStatisticsMode(mode) {
    state.statisticsMode = mode;
    updateStatisticsModeUI();
    loadStatistics();
}

function changeYear(delta) {
    state.statisticsYear += delta;
    if (state.statisticsMode === 'year') {
        loadStatistics();
    } else {
        updateStatisticsModeUI();
    }
}

function updateStatisticsModeUI() {
    const isMonthMode = state.statisticsMode === 'month';
    dom.statsMonthModeBtn.classList.toggle('active', isMonthMode);
    dom.statsYearModeBtn.classList.toggle('active', !isMonthMode);
    dom.statsMonthSelector.style.display = isMonthMode ? 'inline-flex' : 'none';
    dom.statsYearSelector.style.display = 'inline-flex';
}

async function loadStatistics() {
    dom.currentMonth.textContent = formatMonth(state.currentMonth);
    dom.currentYear.textContent = `${state.statisticsYear}年`;
    updateStatisticsModeUI();

    try {
        if (state.statisticsMode === 'month') {
            const [monthlyStats, expenseStats, incomeStats, trendData] = await Promise.all([
                apiRequest(`/statistics/monthly?month=${state.currentMonth}`),
                apiRequest(`/statistics/category?month=${state.currentMonth}&type=expense`),
                apiRequest(`/statistics/category?month=${state.currentMonth}&type=income`),
                apiRequest(`/statistics/trend?month=${state.currentMonth}`)
            ]);

            updateStatisticsUI({
                stats: monthlyStats,
                expenseStats,
                incomeStats,
                trendData,
                auxiliaryLabel: '活跃记账天数',
                auxiliaryValue: `${(trendData.dates || []).length} 天`,
                trendTitle: '日度收支趋势'
            });
            return;
        }

        const [yearlyStats, expenseStats, incomeStats, trendData] = await Promise.all([
            apiRequest(`/statistics/yearly?year=${state.statisticsYear}`),
            apiRequest(`/statistics/category/yearly?year=${state.statisticsYear}&type=expense`),
            apiRequest(`/statistics/category/yearly?year=${state.statisticsYear}&type=income`),
            apiRequest(`/statistics/trend/yearly?year=${state.statisticsYear}`)
        ]);

        updateStatisticsUI({
            stats: yearlyStats,
            expenseStats,
            incomeStats,
            trendData,
            auxiliaryLabel: '最高支出分类',
            auxiliaryValue: yearlyStats.topExpenseCategory && yearlyStats.topExpenseCategory.categoryName
                ? `${yearlyStats.topExpenseCategory.categoryName} ${formatCurrency(yearlyStats.topExpenseCategory.amount)}`
                : '暂无数据',
            trendTitle: '月度收支趋势'
        });
    } catch (error) {
        showToast('加载统计数据失败，使用模拟数据', 'warning');
        const mockData = getMockStatisticsData();
        updateStatisticsUI({
            stats: mockData.stats,
            expenseStats: mockData.expenseStats,
            incomeStats: mockData.incomeStats,
            trendData: mockData.trendData,
            auxiliaryLabel: state.statisticsMode === 'month' ? '活跃记账天数' : '最高支出分类',
            auxiliaryValue: state.statisticsMode === 'month' ? `${(mockData.trendData.dates || []).length} 天` : '暂无数据',
            trendTitle: state.statisticsMode === 'month' ? '日度收支趋势' : '月度收支趋势'
        });
    }
}

function getMockStatisticsData() {
    return {
        stats: { expense: 3500, income: 8000, balance: 4500 },
        expenseStats: [
            { categoryId: 1, categoryName: '餐饮', amount: 1200, count: 35 },
            { categoryId: 2, categoryName: '交通', amount: 500, count: 20 },
            { categoryId: 3, categoryName: '购物', amount: 800, count: 10 },
            { categoryId: 4, categoryName: '娱乐', amount: 600, count: 8 },
            { categoryId: 7, categoryName: '住房', amount: 400, count: 2 }
        ],
        incomeStats: [
            { categoryId: 9, categoryName: '工资', amount: 8000, count: 1 }
        ],
        trendData: {
            dates: ['2024-01-01', '2024-01-02', '2024-01-03', '2024-01-04', '2024-01-05'],
            expenses: [150, 200, 180, 120, 250],
            incomes: [0, 0, 8000, 0, 0]
        }
    };
}

function updateStatisticsUI({ stats, expenseStats, incomeStats, trendData, auxiliaryLabel, auxiliaryValue, trendTitle }) {
    dom.statExpense.textContent = formatCurrency(stats.expense);
    dom.statIncome.textContent = formatCurrency(stats.income);
    dom.statBalance.textContent = formatCurrency(stats.balance);
    dom.statAuxiliaryLabel.textContent = auxiliaryLabel;
    dom.statAuxiliary.textContent = auxiliaryValue;
    dom.trendChartTitle.textContent = trendTitle;

    renderCategoryStats(dom.expenseCategoryStats, expenseStats, stats.expense);
    renderCategoryStats(dom.incomeCategoryStats, incomeStats, stats.income);
    renderStatisticsPieChart(dom.expenseCategoryPieChart, expenseStats, '支出');
    renderStatisticsPieChart(dom.incomeCategoryPieChart, incomeStats, '收入');
    renderTrendChart(trendData);
}

function renderCategoryStats(container, stats, total) {
    if (!stats || stats.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon">📊</span>
                <p>暂无数据</p>
            </div>
        `;
        return;
    }

    container.innerHTML = stats.map(item => {
        const percent = total > 0 ? (item.amount / total) * 100 : 0;
        return `
            <div class="category-stat-item">
                <div class="category-stat-icon">${getCategoryIcon(item.categoryId)}</div>
                <div class="category-stat-info">
                    <div class="category-stat-name">${item.categoryName} (${item.count}笔)</div>
                    <div class="category-stat-bar">
                        <div class="category-stat-fill" style="width: ${percent}%"></div>
                    </div>
                </div>
                <div class="category-stat-amount">${formatCurrency(item.amount)}</div>
            </div>
        `;
    }).join('');
}

function renderStatisticsPieChart(container, stats, label) {
    if (!stats || stats.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon">📊</span>
                <p>暂无数据</p>
            </div>
        `;
        return;
    }

    const palette = ['#12b886', '#f97316', '#2563eb', '#eab308', '#8b5cf6', '#ef4444', '#14b8a6', '#64748b'];
    const total = stats.reduce((sum, item) => sum + Number(item.amount || 0), 0);
    const radius = 72;
    const circumference = 2 * Math.PI * radius;
    let offset = 0;

    const segments = stats.map((item, index) => {
        const amount = Number(item.amount || 0);
        const ratio = total > 0 ? amount / total : 0;
        const length = circumference * ratio;
        const color = palette[index % palette.length];
        const segment = `
            <circle
                class="donut-segment"
                cx="100"
                cy="100"
                r="${radius}"
                stroke="${color}"
                stroke-dasharray="${length} ${circumference - length}"
                stroke-dashoffset="${-offset}"
            ></circle>
        `;
        offset += length;
        return segment;
    }).join('');

    const legend = stats.map((item, index) => {
        const amount = Number(item.amount || 0);
        const ratio = total > 0 ? ((amount / total) * 100).toFixed(1) : '0.0';
        const color = palette[index % palette.length];
        return `
            <div class="legend-item">
                <span class="legend-color" style="background:${color}"></span>
                <div class="legend-content">
                    <span class="legend-name">${item.categoryName}</span>
                    <span class="legend-meta">${formatCurrency(amount)} · ${ratio}% · ${item.count} 笔</span>
                </div>
            </div>
        `;
    }).join('');

    container.innerHTML = `
        <div class="donut-chart-layout">
            <div class="chart-pie">
                <svg class="donut-chart" viewBox="0 0 200 200" aria-hidden="true">
                    <circle class="donut-track" cx="100" cy="100" r="${radius}"></circle>
                    ${segments}
                </svg>
                <div class="pie-center">
                    <div class="pie-center-label">${label}总额</div>
                    <div class="pie-center-value">${formatCurrency(total)}</div>
                </div>
            </div>
            <div class="chart-legend chart-legend-column">
                ${legend}
            </div>
        </div>
    `;
}

function renderTrendChart(trendData) {
    const labels = trendData ? (trendData.dates || trendData.labels || []) : [];
    if (!trendData || labels.length === 0) {
        dom.trendChart.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon">📈</span>
                <p>暂无数据</p>
            </div>
        `;
        return;
    }

    const maxExpense = Math.max(...trendData.expenses, 1);
    const maxIncome = Math.max(...trendData.incomes, 1);
    const maxValue = Math.max(maxExpense, maxIncome);

    dom.trendChart.innerHTML = labels.map((label, index) => {
        const expenseHeight = (trendData.expenses[index] / maxValue) * 150;
        const incomeHeight = (trendData.incomes[index] / maxValue) * 150;
        const xLabel = trendData.unit === 'month'
            ? label
            : (String(label).includes('-') ? String(label).split('-')[2] : label);

        return `
            <div class="trend-bar-group">
                <div class="trend-bars">
                    <div class="trend-bar expense" style="height: ${expenseHeight}px" title="支出: ${formatCurrency(trendData.expenses[index])}"></div>
                    <div class="trend-bar income" style="height: ${incomeHeight}px" title="收入: ${formatCurrency(trendData.incomes[index])}"></div>
                </div>
                <div class="trend-label">${xLabel}</div>
            </div>
        `;
    }).join('');
}

async function loadBudget() {
    dom.budgetCurrentMonth.textContent = formatMonth(state.budgetMonth);

    try {
        const [budgetData, dailyData] = await Promise.all([
            apiRequest(`/budget?month=${state.budgetMonth}`),
            apiRequest(`/budget/daily-available?month=${state.budgetMonth}`)
        ]);

        updateBudgetUI(budgetData, dailyData);
    } catch (error) {
        showToast('加载预算数据失败，使用模拟数据', 'warning');
        const mockData = {
            budget: 5000,
            spent: 3500,
            remaining: 1500,
            dailyAvailable: 175,
            daysRemaining: 20
        };
        updateBudgetUI(mockData, mockData);
    }
}

function updateBudgetUI(budgetData, dailyData) {
    dom.budgetAmount.textContent = formatCurrency(budgetData.budget);
    dom.budgetSpent.textContent = formatCurrency(budgetData.spent);
    dom.budgetRemainingDetail.textContent = formatCurrency(budgetData.remaining);
    dom.dailyAvailable.textContent = formatCurrency(dailyData.dailyAvailable || budgetData.dailyAvailable || 0);
    dom.daysRemaining.textContent = `${dailyData.daysRemaining || 0} 天`;

    const percent = budgetData.budget > 0 ? Math.min((budgetData.spent / budgetData.budget) * 100, 100) : 0;
    dom.budgetPercent.textContent = `${percent.toFixed(1)}%`;
    dom.budgetProgressFill.style.width = `${percent}%`;

    if (percent > 80) {
        dom.budgetProgressFill.style.background = 'linear-gradient(90deg, #ef4444, #f87171)';
    } else if (percent > 60) {
        dom.budgetProgressFill.style.background = 'linear-gradient(90deg, #f59e0b, #fbbf24)';
    } else {
        dom.budgetProgressFill.style.background = 'linear-gradient(90deg, #6366f1, #818cf8)';
    }
}

function openBudgetModal() {
    dom.budgetModal.classList.add('active');
    dom.budgetAmountInput.value = '';
}

async function handleBudgetSubmit(e) {
    e.preventDefault();

    const amount = parseFloat(dom.budgetAmountInput.value);

    try {
        await apiRequest('/budget', {
            method: 'POST',
            body: {
                amount: amount,
                month: state.budgetMonth
            }
        });
        showToast('预算设置成功', 'success');
        closeModal('budget');
        loadBudget();
    } catch (error) {
        showToast('设置失败，已模拟保存', 'warning');
        closeModal('budget');
        updateBudgetUI({
            budget: amount,
            spent: 0,
            remaining: amount
        }, {
            dailyAvailable: amount / 30,
            daysRemaining: 30
        });
    }
}

function formatCurrency(amount) {
    return '¥' + (amount || 0).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

function formatMonth(monthStr) {
    const [year, month] = monthStr.split('-');
    return `${year}年${parseInt(month, 10)}月`;
}

function showToast(message, type = 'success') {
    const icons = {
        success: '✓',
        error: '✕',
        warning: '⚠️'
    };

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span class="toast-icon">${icons[type]}</span>
        <span class="toast-message">${message}</span>
    `;

    dom.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'toastIn 0.3s ease reverse';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 3000);
}

window.editRecord = editRecord;
window.deleteRecord = deleteRecord;
window.editCategory = editCategory;
window.deleteCategory = deleteCategory;
window.goToPage = goToPage;

document.addEventListener('DOMContentLoaded', init);




