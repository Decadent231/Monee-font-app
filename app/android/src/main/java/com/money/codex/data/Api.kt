package com.money.codex.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "http://101.201.235.13:8080/api/"

interface MoneyApiService {
    @GET("categories")
    suspend fun categories(): ApiResponse<List<Category>>

    @POST("categories")
    suspend fun addCategory(@Body payload: CategoryPayload): ApiResponse<Any>

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Int, @Body payload: CategoryPayload): ApiResponse<Any>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): ApiResponse<Any>

    @GET("records")
    suspend fun records(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("type") type: String? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("keyword") keyword: String? = null
    ): ApiResponse<RecordsPage>

    @POST("records")
    suspend fun addRecord(@Body payload: RecordPayload): ApiResponse<Any>

    @PUT("records/{id}")
    suspend fun updateRecord(@Path("id") id: Int, @Body payload: RecordPayload): ApiResponse<Any>

    @DELETE("records/{id}")
    suspend fun deleteRecord(@Path("id") id: Int): ApiResponse<Any>

    @GET("statistics/monthly")
    suspend fun monthlyStats(@Query("month") month: String): ApiResponse<MonthlyStats>

    @GET("statistics/yearly")
    suspend fun yearlyStats(@Query("year") year: Int): ApiResponse<YearlyStats>

    @GET("statistics/category")
    suspend fun categoryStats(
        @Query("month") month: String,
        @Query("type") type: String
    ): ApiResponse<List<CategoryStat>>

    @GET("statistics/category/yearly")
    suspend fun yearlyCategoryStats(
        @Query("year") year: Int,
        @Query("type") type: String
    ): ApiResponse<List<CategoryStat>>

    @GET("statistics/trend")
    suspend fun trend(@Query("month") month: String): ApiResponse<TrendData>

    @GET("statistics/trend/yearly")
    suspend fun yearlyTrend(@Query("year") year: Int): ApiResponse<TrendData>

    @GET("budget")
    suspend fun budget(@Query("month") month: String): ApiResponse<BudgetData>

    @GET("budget/daily-available")
    suspend fun dailyBudget(@Query("month") month: String): ApiResponse<DailyBudgetData>

    @POST("budget")
    suspend fun setBudget(@Body payload: BudgetPayload): ApiResponse<Any>
}

object ApiFactory {
    val service: MoneyApiService by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoneyApiService::class.java)
    }
}

