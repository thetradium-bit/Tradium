package com.example.data.repository

import android.util.Log
import com.example.data.remote.binance.BinanceFuturesApi
import com.example.data.remote.binance.BinanceSpotApi
import com.example.data.remote.binance.BinanceWebSocketManager
import com.example.domain.model.CandleData
import com.example.domain.model.MarketTicker
import com.example.domain.model.MarketType
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private const val TAG = "BinanceMarketRepo"

interface MarketDataRepository {
    suspend fun getCandleData(
        pair: String,
        timeframe: String,
        marketType: MarketType = MarketType.SPOT,
        limit: Int = 60
    ): List<CandleData>

    suspend fun getMarketTicker(
        pair: String,
        marketType: MarketType = MarketType.SPOT
    ): MarketTicker

    suspend fun getTopTickers(marketType: MarketType = MarketType.SPOT): List<MarketTicker>

    fun subscribeLiveStream(pair: String, marketType: MarketType, timeframe: String)
    fun disconnectStream()
    val liveCandles: Flow<com.example.data.remote.binance.LiveCandleUpdate>
    val livePrices: Flow<com.example.data.remote.binance.LivePriceUpdate>
}

class MarketDataRepositoryImpl(
    private val webSocketManager: BinanceWebSocketManager = BinanceWebSocketManager()
) : MarketDataRepository {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Host order: data-api.binance.vision (Public global unblocked mirror) -> api.binance.us -> api.binance.com
    private val publicVisionRetrofit = Retrofit.Builder()
        .baseUrl("https://data-api.binance.vision/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val binanceUsRetrofit = Retrofit.Builder()
        .baseUrl("https://api.binance.us/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val binanceGlobalRetrofit = Retrofit.Builder()
        .baseUrl("https://api.binance.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val futuresRetrofit = Retrofit.Builder()
        .baseUrl("https://fapi.binance.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val visionSpotApi: BinanceSpotApi = publicVisionRetrofit.create(BinanceSpotApi::class.java)
    private val usSpotApi: BinanceSpotApi = binanceUsRetrofit.create(BinanceSpotApi::class.java)
    private val globalSpotApi: BinanceSpotApi = binanceGlobalRetrofit.create(BinanceSpotApi::class.java)
    private val futuresApi: BinanceFuturesApi = futuresRetrofit.create(BinanceFuturesApi::class.java)

    private val candleCache = ConcurrentHashMap<String, List<CandleData>>()

    override val liveCandles = webSocketManager.liveCandles
    override val livePrices = webSocketManager.livePrices

    override suspend fun getCandleData(
        pair: String,
        timeframe: String,
        marketType: MarketType,
        limit: Int
    ): List<CandleData> {
        val symbol = pair.replace("/", "").uppercase()
        val interval = mapTimeframe(timeframe)
        val cacheKey = "$symbol-$interval-$marketType"

        // 1. Try public unblocked Binance Vision mirror first
        try {
            val rawKlines = visionSpotApi.getKlines(symbol, interval, limit)
            val candles = parseKlines(rawKlines)
            if (candles.isNotEmpty()) {
                candleCache[cacheKey] = candles
                return candles
            }
        } catch (e: Exception) {
            // failover quietly
        }

        // 2. Try Binance.US REST API
        try {
            val rawKlines = usSpotApi.getKlines(symbol, interval, limit)
            val candles = parseKlines(rawKlines)
            if (candles.isNotEmpty()) {
                candleCache[cacheKey] = candles
                return candles
            }
        } catch (e: Exception) {
            // failover quietly
        }

        // 3. Try Global / Futures API if not geo-blocked
        try {
            val rawKlines = if (marketType == MarketType.FUTURES) {
                futuresApi.getFuturesKlines(symbol, interval, limit)
            } else {
                globalSpotApi.getKlines(symbol, interval, limit)
            }
            val candles = parseKlines(rawKlines)
            if (candles.isNotEmpty()) {
                candleCache[cacheKey] = candles
                return candles
            }
        } catch (e: Exception) {
            Log.w(TAG, "Binance direct endpoint: ${e.message}")
        }

        return candleCache[cacheKey] ?: emptyList()
    }

    private fun parseKlines(rawKlines: List<List<Any>>): List<CandleData> {
        return rawKlines.mapNotNull { row ->
            try {
                val openTime = (row[0] as? Number)?.toLong() ?: 0L
                val open = (row[1] as? String)?.toFloatOrNull() ?: 0f
                val high = (row[2] as? String)?.toFloatOrNull() ?: 0f
                val low = (row[3] as? String)?.toFloatOrNull() ?: 0f
                val close = (row[4] as? String)?.toFloatOrNull() ?: 0f
                val volume = (row[5] as? String)?.toFloatOrNull() ?: 0f

                CandleData(
                    timestamp = openTime,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getMarketTicker(pair: String, marketType: MarketType): MarketTicker {
        val symbol = pair.replace("/", "").uppercase()

        // 1. Try Binance Vision mirror first
        try {
            val tickerDto = visionSpotApi.get24HrTicker(symbol)
            return MarketTicker(
                symbol = symbol,
                pairFormatted = pair,
                marketType = marketType,
                lastPrice = tickerDto.lastPrice.toDoubleOrNull() ?: 0.0,
                priceChange24h = tickerDto.priceChange.toDoubleOrNull() ?: 0.0,
                priceChangePercent24h = tickerDto.priceChangePercent.toDoubleOrNull() ?: 0.0,
                highPrice24h = tickerDto.highPrice.toDoubleOrNull() ?: 0.0,
                lowPrice24h = tickerDto.lowPrice.toDoubleOrNull() ?: 0.0,
                volume24h = tickerDto.volume.toDoubleOrNull() ?: 0.0,
                quoteVolume24h = tickerDto.quoteVolume.toDoubleOrNull() ?: 0.0
            )
        } catch (e: Exception) {
            // try next
        }

        // 2. Try Binance.US
        try {
            val tickerDto = usSpotApi.get24HrTicker(symbol)
            return MarketTicker(
                symbol = symbol,
                pairFormatted = pair,
                marketType = marketType,
                lastPrice = tickerDto.lastPrice.toDoubleOrNull() ?: 0.0,
                priceChange24h = tickerDto.priceChange.toDoubleOrNull() ?: 0.0,
                priceChangePercent24h = tickerDto.priceChangePercent.toDoubleOrNull() ?: 0.0,
                highPrice24h = tickerDto.highPrice.toDoubleOrNull() ?: 0.0,
                lowPrice24h = tickerDto.lowPrice.toDoubleOrNull() ?: 0.0,
                volume24h = tickerDto.volume.toDoubleOrNull() ?: 0.0,
                quoteVolume24h = tickerDto.quoteVolume.toDoubleOrNull() ?: 0.0
            )
        } catch (e: Exception) {
            // try next
        }

        // 3. Fallback to direct Futures or Global
        try {
            val tickerDto = if (marketType == MarketType.FUTURES) {
                futuresApi.getFutures24HrTicker(symbol)
            } else {
                globalSpotApi.get24HrTicker(symbol)
            }
            return MarketTicker(
                symbol = symbol,
                pairFormatted = pair,
                marketType = marketType,
                lastPrice = tickerDto.lastPrice.toDoubleOrNull() ?: 0.0,
                priceChange24h = tickerDto.priceChange.toDoubleOrNull() ?: 0.0,
                priceChangePercent24h = tickerDto.priceChangePercent.toDoubleOrNull() ?: 0.0,
                highPrice24h = tickerDto.highPrice.toDoubleOrNull() ?: 0.0,
                lowPrice24h = tickerDto.lowPrice.toDoubleOrNull() ?: 0.0,
                volume24h = tickerDto.volume.toDoubleOrNull() ?: 0.0,
                quoteVolume24h = tickerDto.quoteVolume.toDoubleOrNull() ?: 0.0
            )
        } catch (e: Exception) {
            Log.w(TAG, "Ticker fallback exhausted for $symbol: ${e.message}")
        }

        return MarketTicker(
            symbol = symbol,
            pairFormatted = pair,
            marketType = marketType,
            lastPrice = 0.0,
            priceChange24h = 0.0,
            priceChangePercent24h = 0.0,
            highPrice24h = 0.0,
            lowPrice24h = 0.0,
            volume24h = 0.0,
            quoteVolume24h = 0.0
        )
    }

    override suspend fun getTopTickers(marketType: MarketType): List<MarketTicker> {
        val pairs = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "NEAR/USDT", "AVAX/USDT", "LINK/USDT", "XRP/USDT")
        return pairs.map { getMarketTicker(it, marketType) }
    }

    override fun subscribeLiveStream(pair: String, marketType: MarketType, timeframe: String) {
        webSocketManager.subscribeMarketStream(pair, marketType, timeframe)
    }

    override fun disconnectStream() {
        webSocketManager.disconnect()
    }

    private fun mapTimeframe(tf: String): String {
        return when (tf.lowercase()) {
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
    }
}
