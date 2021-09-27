package com.eslamwaheed.chartiqdemo

import android.annotation.SuppressLint
import com.chartiq.sdk.model.OHLCParams
import com.chartiq.sdk.model.QuoteFeedParams
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.moshi.MoshiConverterFactory


class ChartIQNetworkManager : NetworkManager {
    private val chartRetrofit by lazy {
        getRetrofit(HOST_SIMULATOR, ChartAPI::class.java)
    }

    private fun getMoshiConverterFactory(): MoshiConverterFactory {
        return MoshiConverterFactory.create(moshi)
    }

    private fun getMoshiInstance(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val moshi by lazy { getMoshiInstance() }
    private val moshiConverter by lazy { getMoshiConverterFactory() }

    @SuppressLint("HardwareIds")
    override suspend fun fetchDataFeed(
        params: QuoteFeedParams,
        applicationId: String
    ): NetworkResult<List<OHLCParams>> {
        return try {
            chartRetrofit
                .fetchDataFeedAsync(
                    params.symbol,
                    params.start,
                    params.end,
                    params.interval,
                    params.period?.toString(),
                    DEFAULT_VALUE_EXTENDED,
                    applicationId
                )
                .safeExtractNetworkResult()
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkException(null, 3000))
        }
    }

    override suspend fun fetchSymbolCharIQData(
        params: QuoteFeedParams
    ): NetworkResult<List<SymbolChartModel>> {
        return try {
            chartRetrofit
                .getSymbolCharIQData(
                    "${baseChartUrl}home/fetch",
                    params.symbol,
                    params.start,
                    params.end,
                    params.interval,
                    params.period
                )
                .safeExtractNetworkResult()
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkException(null, 3000))
        }
    }

    private fun <T> getRetrofit(baseUrl: String, api: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(moshiConverter)
            .client(OkHttpClient().newBuilder().addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            ).build())
            .build()
            .create(api)
    }

    companion object {
        private const val HOST_SIMULATOR = "https://mobile-simulator.chartiq.com"
        private const val DEFAULT_VALUE_EXTENDED = "1"
        private val baseChartUrl = "https://chart.derayah.com/"
        val chartUrl = "${baseChartUrl}dist/template-native-sdk.html"
    }
}