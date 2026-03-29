# 记账项目 API 接口文档

## 基础信息

- Base URL: `http://localhost:8080/api`
- 请求格式: `application/json`
- 响应格式: JSON
- 当前项目默认使用固定用户 `user_id = 1`

## 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 1. 记录接口

### 1.1 获取记录列表

接口: `GET /records`

请求参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 `1` |
| size | int | 否 | 每页数量，默认 `10` |
| startDate | string | 否 | 开始日期，格式 `yyyy-MM-dd` |
| endDate | string | 否 | 结束日期，格式 `yyyy-MM-dd` |
| type | string | 否 | `expense` 或 `income` |
| categoryId | long | 否 | 分类 ID |
| keyword | string | 否 | 备注关键字搜索，模糊匹配 |

响应示例:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "date": "2026-03-29",
        "type": "expense",
        "categoryId": 1,
        "categoryName": "餐饮",
        "amount": 35.50,
        "remark": "午饭"
      }
    ],
    "total": 18
  }
}
```

### 1.2 获取记录详情

接口: `GET /records/{id}`

### 1.3 新增记录

接口: `POST /records`

请求体:

```json
{
  "date": "2026-03-29",
  "type": "expense",
  "categoryId": 1,
  "amount": 35.50,
  "remark": "午饭"
}
```

### 1.4 更新记录

接口: `PUT /records/{id}`

### 1.5 删除记录

接口: `DELETE /records/{id}`

## 2. 分类接口

### 2.1 获取分类列表

接口: `GET /categories`

### 2.2 新增分类

接口: `POST /categories`

请求体:

```json
{
  "type": "expense",
  "icon": "🍜",
  "name": "餐饮",
  "description": "日常用餐",
  "sort": 1
}
```

### 2.3 更新分类

接口: `PUT /categories/{id}`

### 2.4 删除分类

接口: `DELETE /categories/{id}`

## 3. 预算接口

### 3.1 获取月度预算信息

接口: `GET /budget`

请求参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | string | 否 | 月份，格式 `yyyy-MM`，默认当前月 |

响应示例:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "budget": 5000.00,
    "spent": 1500.00,
    "remaining": 3500.00
  }
}
```

### 3.2 设置月度预算

接口: `POST /budget`

请求体:

```json
{
  "amount": 5000.00,
  "month": "2026-03"
}
```

### 3.3 获取每日可用预算

接口: `GET /budget/daily-available`

请求参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | string | 否 | 月份，格式 `yyyy-MM`，默认当前月 |

响应示例:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dailyAvailable": 175.00,
    "daysRemaining": 20
  }
}
```

## 4. 统计接口

### 4.1 月度统计

接口: `GET /statistics/monthly`

请求参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | string | 否 | 月份，格式 `yyyy-MM`，默认当前月 |

### 4.2 分类统计

接口: `GET /statistics/category`

请求参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | string | 否 | 月份，格式 `yyyy-MM`，默认当前月 |
| type | string | 否 | `expense` 或 `income` |

### 4.3 月度趋势

接口: `GET /statistics/trend`

请求参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | string | 否 | 月份，格式 `yyyy-MM`，默认当前月 |

### 4.4 年度统计

接口: `GET /statistics/yearly`

新增功能说明:

- 用于年度财务复盘
- 前端可以直接展示年度收入、支出、结余、活跃记账天数、月度趋势、年度支出最高分类

请求参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | int | 否 | 年份，默认当前年 |

响应示例:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "year": 2026,
    "income": 28000.00,
    "expense": 12600.00,
    "balance": 15400.00,
    "activeDays": 47,
    "monthlyIncome": [8000, 8000, 12000, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    "monthlyExpense": [3500, 4200, 4900, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    "topExpenseCategory": {
      "categoryId": 1,
      "categoryName": "餐饮",
      "amount": 3200.00
    }
  }
}
```

### 4.5 年度分类统计

接口: `GET /statistics/category/yearly`

请求参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | int | 否 | 年份，默认当前年 |
| type | string | 否 | `expense` 或 `income` |

响应说明:

- 返回指定年份内按分类聚合后的金额与笔数
- 适用于年度支出占比饼图、年度收入来源饼图

响应示例:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "categoryId": 1,
      "categoryName": "餐饮",
      "amount": 3200.00,
      "count": 42
    },
    {
      "categoryId": 7,
      "categoryName": "住房",
      "amount": 4800.00,
      "count": 6
    }
  ]
}
```

### 4.6 年度趋势统计

接口: `GET /statistics/trend/yearly`

请求参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | int | 否 | 年份，默认当前年 |

响应说明:

- 返回 1 到 12 月的收支汇总
- 适用于年度柱状趋势图

响应示例:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "unit": "month",
    "labels": ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],
    "expenses": [3500, 4200, 4900, 0, 0, 0, 0, 0, 0, 0, 0, 0],
    "incomes": [8000, 8000, 12000, 0, 0, 0, 0, 0, 0, 0, 0, 0]
  }
}
```

## 5. 错误码

| 错误码 | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 6. 当前新增的实用功能

### 6.1 记录备注关键字搜索

- 前端位置: 记录页筛选栏
- 后端支持: `GET /records?keyword=...`
- 匹配方式: `remark LIKE '%keyword%'`

### 6.2 年度财务统计

- 前端位置: 统计页年度区域
- 后端支持: `GET /statistics/yearly`
- 展示内容:
  - 年度收入
  - 年度支出
  - 年度结余
  - 活跃记账天数
  - 12 个月收支趋势
  - 年度支出最高分类
