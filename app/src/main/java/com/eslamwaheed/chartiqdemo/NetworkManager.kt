package com.eslamwaheed.chartiqdemo

import com.chartiq.sdk.model.OHLCParams
import com.chartiq.sdk.model.QuoteFeedParams

interface NetworkManager {
    suspend fun fetchDataFeed(
        params: QuoteFeedParams,
        applicationId: String
    ): NetworkResult<List<OHLCParams>>

    suspend fun fetchSymbolCharIQData(
        params: QuoteFeedParams
    ): NetworkResult<List<SymbolChartModel>>
}