package com.eslamwaheed.chartiqdemo

import com.chartiq.sdk.ChartIQ

interface ServiceLocator {
    val chartIQ: ChartIQ
}