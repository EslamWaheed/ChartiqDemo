package com.eslamwaheed.chartiqdemo

import com.chartiq.sdk.model.OHLCParams
import retrofit2.Response
import retrofit2.http.*

interface ChartAPI {
    @GET("datafeed")
    suspend fun fetchDataFeedAsync(
        @Query("identifier") identifier: String?,
        @Query("startdate") startDate: String?,
        @Query("enddate") endDate: String?,
        @Query("interval") interval: String?,
        @Query("period") period: String?,
        @Query("extended") extended: String?,
        @Query("session") session: String?,
    ): Response<List<OHLCParams>>

    @GET
    suspend fun getSymbolCharIQData(
        @Url url: String,
        @Query("symbol") symbol: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("interval") interval: String?,
        @Query("period") period: Int?
    ): Response<List<SymbolChartModel>>
}
