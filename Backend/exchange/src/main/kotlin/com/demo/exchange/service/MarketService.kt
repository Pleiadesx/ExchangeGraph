package com.demo.exchange.service

import com.demo.exchange.extensions.toCachedProvider
import com.demo.exchange.model.CoinType
import com.demo.exchange.providers.CoinBaseProvider
import com.demo.exchange.providers.ExchangeMarketPriceProvider
import com.demo.exchange.providers.GeminiBaseProvider
import com.demo.exchange.providers.PriceQuantityPair
import com.demo.exchange.repository.PriceFrameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.Duration
import javax.annotation.PostConstruct

data class MarketPair(
    val provider: ExchangeMarketPriceProvider,
    val coinType: CoinType,
    val exchangeId: String
)

data class ExchangePrices(
    val bestBuyPrice: PriceQuantityPair,
    val bestSellPrice: PriceQuantityPair,
    val coinType: CoinType,
    val exchangeId: String,
)

@Component
@Service
class MarketService(val priceFrameRepository: PriceFrameRepository) {

    val marketPairs = listOf(
        MarketPair(
            CoinBaseProvider(CoinType.ETHEREUM).toCachedProvider(),
            CoinType.ETHEREUM,
            CoinBaseProvider.exchangeName
        ),
        MarketPair(
            CoinBaseProvider(CoinType.BITCOIN).toCachedProvider(),
            CoinType.BITCOIN,
            CoinBaseProvider.exchangeName
        ),
        MarketPair(
            GeminiBaseProvider(CoinType.BITCOIN).toCachedProvider(),
            CoinType.BITCOIN,
            GeminiBaseProvider.exchangeName
        ),
        MarketPair(
            GeminiBaseProvider(CoinType.ETHEREUM).toCachedProvider(),
            CoinType.ETHEREUM,
            GeminiBaseProvider.exchangeName
        ),
    )


    val exchangeInfoMap = mapOf(
        CoinBaseProvider.exchangeName to CoinBaseProvider.exchangeInfo,
        GeminiBaseProvider.exchangeName to GeminiBaseProvider.exchangeInfo
    )


    companion object {
        val delayPeriod: Duration = Duration.ofSeconds(1)
    }


    @PostConstruct
    fun init() {
        println("started market service")
        println(marketPairs)
    }


    fun streamBestPrices() : Flow<ExchangePrices> {
        return flow {
            while (true) {
                for (pair in marketPairs) {

                    val buyPair = pair.provider.getBestAskingPricePair() ?: continue
                    val sellPair = pair.provider.getBestBidPrice() ?: continue

                    emit(ExchangePrices(
                        buyPair,
                        sellPair,
                        pair.coinType,
                        pair.exchangeId
                    ))
                }
                delay(delayPeriod.toMillis())
            }
        }
    }

    fun streamOneCoinPrices() : Flow<ExchangePrices> {
        return flow {
            while (true) {
                for (pair in marketPairs) {
                    val buyPair = pair.provider.oneCoinBuyingCost() ?: continue
                    val sellPair = pair.provider.oneCoinSellingPrice() ?: continue
                    emit(ExchangePrices(
                            buyPair,
                            sellPair,
                            pair.coinType,
                            pair.exchangeId
                    ))
                }
                delay(delayPeriod.toMillis())
            }
        }
    }

    fun getExchangeInfoByName(exchangeInfoName: String) =
       exchangeInfoMap[exchangeInfoName]



}