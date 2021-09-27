package com.eslamwaheed.chartiqdemo

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SymbolChartModel(
    @Json(name = "DT")
    val date: String? = null,
    @Json(name = "AdjClose")
    val adjClose: Double? = null,
    @Json(name = "Value")
    val value: Double? = null,
    @Json(name = "Open")
    val open: Double? = null,
    @Json(name = "High")
    val high: Double? = null,
    @Json(name = "Low")
    val low: Double? = null,
    @Json(name = "Close")
    val close: Double? = null,
    @Json(name = "Volume")
    val volume: Double? = null
)