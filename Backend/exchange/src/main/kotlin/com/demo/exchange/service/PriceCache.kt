package com.demo.exchange.service

import com.demo.exchange.providers.ExchangeMarketPriceProvider
import com.demo.exchange.providers.PriceQuantityPair
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

@OptIn(DelicateCoroutinesApi::class)
class PriceCache(
    private val provider: ExchangeMarketPriceProvider,
    private val coinAmount: BigDecimal,
    private val updatePeriod: Duration = Duration.ofSeconds(1),
    private val freshPeriod: Duration = Duration.ofSeconds(1),
) {
    private val buyPricePair: AtomicReference<PriceQuantityPair?> = AtomicReference(null)
    private val sellPricePair: AtomicReference<PriceQuantityPair?> = AtomicReference(null)
    private val expire: AtomicReference<Instant> = AtomicReference(Instant.now())

    init {
        GlobalScope.launch {
            while (true) {
                delay(updatePeriod.toMillis())
                updatePrices(
                    provider.nCoinBuyingCost(coinAmount),
                    provider.nCoinSellingPrice(coinAmount),
                    freshPeriod
                )
            }
        }
    }

    private fun updatePrices(buyPrice: PriceQuantityPair?,
                             sellPrice: PriceQuantityPair?,
                             staleIn: Duration = Duration.ofSeconds(1)) {

        buyPricePair.set(buyPrice)
        sellPricePair.set(sellPrice)
        expire.set(Instant.now() + staleIn)
    }

    fun buyPriceIfFresh() : PriceQuantityPair? {
        return if (expire.get() > Instant.now()) buyPricePair.get() else null
    }

    fun sellPriceIfFresh() : PriceQuantityPair? {
        return if (expire.get() > Instant.now()) sellPricePair.get() else null
    }

}