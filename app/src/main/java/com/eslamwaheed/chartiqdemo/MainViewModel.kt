package com.eslamwaheed.chartiqdemo

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chartiq.sdk.ChartIQ
import com.chartiq.sdk.DataSource
import com.chartiq.sdk.DataSourceCallback
import com.chartiq.sdk.model.DataMethod
import com.chartiq.sdk.model.OHLCParams
import com.chartiq.sdk.model.QuoteFeedParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(
    private val networkManager: NetworkManager,
    private val chartIQ: ChartIQ,
    private val connectivityManager: ConnectivityManager
) : ViewModel() {
    val symbol = MutableLiveData<Symbol>()
    val errorLiveData = MutableLiveData<Unit>()
    val networkIsAvailableEvent = MutableLiveData<Event<Boolean>>()

    init {
        chartIQ.apply {
            setDataSource(object : DataSource {
                override fun pullInitialData(
                    params: QuoteFeedParams,
                    callback: DataSourceCallback,
                ) {
                    loadChartData(params, callback)
                }

                override fun pullUpdateData(
                    params: QuoteFeedParams,
                    callback: DataSourceCallback,
                ) {
                    loadChartData(params, callback)
                }

                override fun pullPaginationData(
                    params: QuoteFeedParams,
                    callback: DataSourceCallback,
                ) {
                    loadChartData(params, callback)
                }
            })

            start {
                chartIQ.setDataMethod(DataMethod.PULL, "")
            }
        }

        chartIQ.apply {
            addChartAvailableListener { chartAvailable ->
                if (chartAvailable) {
                    updateSymbol(Symbol("1010-99-S"))
                }
            }
        }
    }

    private val symbolDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private fun loadChartData(params: QuoteFeedParams, callback: DataSourceCallback) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = networkManager.fetchSymbolCharIQData(params)) {
                is NetworkResult.Success -> {
                    withContext(Dispatchers.Main) {
                        val data: ArrayList<OHLCParams> = arrayListOf()
                        result.data.map { item ->
                            kotlin.runCatching {
                                item.date?.let { nonNullDateString ->
                                    symbolDateFormat.parse(nonNullDateString)?.let {
                                        data.add(
                                            OHLCParams(
                                                it,
                                                item.open ?: 0.0,
                                                item.high ?: 0.0,
                                                item.low ?: 0.0,
                                                item.close ?: 0.0,
                                                item.volume ?: 0.0,
                                                item.close ?: 0.0
                                            )
                                        )
                                    }
                                }
                            }

                        }
                        callback.execute(data)
                    }
                }
                is NetworkResult.Failure -> {
                    when (result.exception) {
                        is NetworkException -> withContext(Dispatchers.Main) {
                            checkInternetAvailability()
                        }
                        else -> errorLiveData.postValue(Unit)
                    }
                }

            }
        }
    }

    fun checkInternetAvailability() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            if (connectivityManager.activeNetworkInfo != null) {
                if (networkIsAvailableEvent.value?.peekContent() != true) {
                    reloadData()
                    networkIsAvailableEvent.value = Event(true)
                }
            } else {
                networkIsAvailableEvent.value = Event(false)
            }
        } else {
            val allNetworks: Array<Network> = connectivityManager.allNetworks
            for (network in allNetworks) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    ) {
                        if (networkIsAvailableEvent.value?.peekContent() != true) {
                            reloadData()
                            networkIsAvailableEvent.value = Event(true)
                        }
                        return
                    }
                }
            }
            networkIsAvailableEvent.value = Event(false)
        }
    }

    private fun reloadData() {
        symbol.value?.let {
            updateSymbol(it)
        }
    }

    fun updateSymbol(symbol: Symbol) {
        chartIQ.setSymbol(symbol.value)
    }

    class ViewModelFactory(
        private val argNetworkManager: NetworkManager,
        private val argChartIQHandler: ChartIQ,
        private val argConnectivityManager: ConnectivityManager
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass
                .getConstructor(
                    NetworkManager::class.java,
                    ChartIQ::class.java,
                    ConnectivityManager::class.java
                )
                .newInstance(
                    argNetworkManager,
                    argChartIQHandler,
                    argConnectivityManager
                )
        }
    }
}