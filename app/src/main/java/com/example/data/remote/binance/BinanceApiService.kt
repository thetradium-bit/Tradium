package com.example.data.remote.binance

import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceSpotApi {

    @GET("api/v3/ticker/24hr")
    suspend fun get24HrTicker(
        @Query("symbol") symbol: String
    ): BinanceTicker24Hr

    @GET("api/v3/ticker/24hr")
    suspend fun getAll24HrTickers(): List<BinanceTicker24Hr>

    /**
     * Kline/candlestick bars for a symbol.
     * Klines are uniquely identified by their open time.
     * Response is List<List<Any>>:
     * [
     *   [
     *     1499040000000,      // 0: Kline open time
     *     "0.01634790",       // 1: Open price
     *     "0.80000000",       // 2: High price
     *     "0.01575800",       // 3: Low price
     *     "0.01577100",       // 4: Close price
     *     "148976.11427815",  // 5: Volume
     *     1499644799999,      // 6: Kline Close time
     *     "2434.19055334",    // 7: Quote asset volume
     *     308,                // 8: Number of trades
     *     "1756.87402397",    // 9: Taker buy base asset volume
     *     "28.46694368",      // 10: Taker buy quote asset volume
     *     "0"                 // 11: Unused field, ignore.
     *   ]
     * ]
     */
    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100
    ): List<List<Any>>
}

interface BinanceFuturesApi {

    @GET("fapi/v1/ticker/24hr")
    suspend fun getFutures24HrTicker(
        @Query("symbol") symbol: String
    ): BinanceTicker24Hr

    @GET("fapi/v1/klines")
    suspend fun getFuturesKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100
    ): List<List<Any>>

    @GET("fapi/v1/premiumIndex")
    suspend fun getPremiumIndex(
        @Query("symbol") symbol: String
    ): BinanceFuturesFundingRate

    @GET("fapi/v1/openInterest")
    suspend fun getOpenInterest(
        @Query("symbol") symbol: String
    ): BinanceFuturesOpenInterest
}
