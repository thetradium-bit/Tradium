package com.example.data.remote.binance

import android.util.Log
import com.example.domain.model.CandleData
import com.example.domain.model.MarketType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "BinanceWebSocket"

data class LiveCandleUpdate(
    val symbol: String,
    val marketType: MarketType,
    val timeframe: String,
    val candle: CandleData,
    val isFinal: Boolean
)

data class LivePriceUpdate(
    val symbol: String,
    val marketType: MarketType,
    val price: Double,
    val timestamp: Long
)

class BinanceWebSocketManager(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _liveCandles = MutableSharedFlow<LiveCandleUpdate>(extraBufferCapacity = 64)
    val liveCandles: Flow<LiveCandleUpdate> = _liveCandles.asSharedFlow()

    private val _livePrices = MutableSharedFlow<LivePriceUpdate>(extraBufferCapacity = 64)
    val livePrices: Flow<LivePriceUpdate> = _livePrices.asSharedFlow()

    private var activeWebSocket: WebSocket? = null
    private var currentStreamUrl: String? = null
    private var isConnected = false
    private var reconnectAttempts = 0
    private var primaryHostIndex = 0

    // Host list with global unblocked endpoints first
    private val hostCandidates = listOf(
        "wss://data-stream.binance.vision/ws",
        "wss://stream.binance.us:9443/ws",
        "wss://stream.binance.com:9443/ws"
    )

    fun subscribeMarketStream(
        symbol: String,
        marketType: MarketType,
        timeframe: String
    ) {
        val cleanSymbol = symbol.replace("/", "").lowercase()
        val interval = when (timeframe.lowercase()) {
            "1m" -> "1m"
            "5m" -> "5m"
            "15m" -> "15m"
            "30m" -> "30m"
            "1h" -> "1h"
            "4h" -> "4h"
            "1d" -> "1d"
            "1w" -> "1w"
            else -> "15m"
        }

        val baseUrl = hostCandidates[primaryHostIndex % hostCandidates.size]
        val streamName = "${cleanSymbol}@kline_${interval}/${cleanSymbol}@ticker"
        val fullUrl = "$baseUrl/$streamName"

        if (fullUrl == currentStreamUrl && isConnected) {
            return
        }

        disconnect()
        currentStreamUrl = fullUrl
        connect(fullUrl, cleanSymbol.uppercase(), marketType, timeframe)
    }

    private fun connect(
        url: String,
        symbol: String,
        marketType: MarketType,
        timeframe: String
    ) {
        val request = Request.Builder().url(url).build()

        activeWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Binance WebSocket connected: $url")
                isConnected = true
                reconnectAttempts = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                parseMessage(text, symbol, marketType, timeframe)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "Binance WebSocket failure on $url: ${t.message}. Failover to alternate host...")
                isConnected = false
                primaryHostIndex++
                handleReconnect(symbol, marketType, timeframe)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Binance WebSocket closed: $reason ($code)")
                isConnected = false
            }
        })
    }

    private fun parseMessage(
        jsonString: String,
        targetSymbol: String,
        marketType: MarketType,
        timeframe: String
    ) {
        try {
            val json = JSONObject(jsonString)
            val eventType = json.optString("e")

            if (eventType == "kline") {
                val kline = json.getJSONObject("k")
                val open = kline.getDouble("o").toFloat()
                val high = kline.getDouble("h").toFloat()
                val low = kline.getDouble("l").toFloat()
                val close = kline.getDouble("c").toFloat()
                val volume = kline.getDouble("v").toFloat()
                val openTime = kline.getLong("t")
                val isFinal = kline.getBoolean("x")

                val candle = CandleData(
                    timestamp = openTime,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume
                )

                _liveCandles.tryEmit(
                    LiveCandleUpdate(
                        symbol = targetSymbol,
                        marketType = marketType,
                        timeframe = timeframe,
                        candle = candle,
                        isFinal = isFinal
                    )
                )

                _livePrices.tryEmit(
                    LivePriceUpdate(
                        symbol = targetSymbol,
                        marketType = marketType,
                        price = close.toDouble(),
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else if (eventType == "24hrTicker") {
                val lastPrice = json.optDouble("c", 0.0)
                if (lastPrice > 0.0) {
                    _livePrices.tryEmit(
                        LivePriceUpdate(
                            symbol = targetSymbol,
                            marketType = marketType,
                            price = lastPrice,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // ignore malformed frame
        }
    }

    private fun handleReconnect(
        symbol: String,
        marketType: MarketType,
        timeframe: String
    ) {
        scope.launch {
            reconnectAttempts++
            val backoffMs = (1000L * (1 shl reconnectAttempts.coerceAtMost(4))).coerceAtMost(16000L)
            delay(backoffMs)
            subscribeMarketStream(symbol, marketType, timeframe)
        }
    }

    fun disconnect() {
        activeWebSocket?.close(1000, "Client switching stream or screen paused")
        activeWebSocket = null
        isConnected = false
        currentStreamUrl = null
    }
}
