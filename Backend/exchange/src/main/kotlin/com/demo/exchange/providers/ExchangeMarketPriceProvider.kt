package com.demo.exchange.providers

import com.demo.exchange.model.CoinType
import java.math.BigDecimal

data class PriceQuantityPair(
    val price: BigDecimal,
    val quantity: BigDecimal
)

data class ExchangeInfo(
    val exchangeName: String,
    val exchangeLogoUrl: String,
    val exchangeUrl: String,
    val marketUrls: Map<CoinType, String>,
    val marketLogoUrls: Map<CoinType, String>,
)

interface ExchangeMarketPriceProvider {


    val exchangeName : String

    fun getBestBidPrice() : PriceQuantityPair?

    fun getBestAskingPricePair() : PriceQuantityPair?

    fun oneCoinBuyingCost() : PriceQuantityPair?

    fun oneCoinSellingPrice() : PriceQuantityPair?

    fun nCoinBuyingCost(n: BigDecimal) : PriceQuantityPair?

    fun nCoinSellingPrice(n: BigDecimal) : PriceQuantityPair?

}