package com.demo.exchange.providers

import com.demo.exchange.service.PriceCache
import java.math.BigDecimal
import java.time.Duration
import java.util.*

class CachedProvider(
    private val internalProvider: ExchangeMarketPriceProvider,
    cachedSet: Set<BigDecimal>,
    private val cacheUpdatePeriod: Duration = Duration.ofSeconds(1),
    private val cacheStalePeriod: Duration = Duration.ofSeconds(1),
    var allowCache: Boolean // make public in order to control when to allow caching from outside
    ) : ExchangeMarketPriceProvider by internalProvider {

    private val cachedPairs =
        cachedSet
            .asSequence()
            .plus(BigDecimal.ONE) // Include the one coin case by default
            .associateByTo(
                TreeMap(),
                {it},
                {PriceCache(internalProvider, it, cacheUpdatePeriod, cacheStalePeriod)}
            ).toMap()


    override fun nCoinBuyingCost(n: BigDecimal): PriceQuantityPair? =
        if (allowCache) (cachedPairs[n]?.buyPriceIfFresh() ?: internalProvider.nCoinBuyingCost(n))
        else internalProvider.nCoinBuyingCost(n)

    override fun nCoinSellingPrice(n: BigDecimal): PriceQuantityPair? =
        if (allowCache) (cachedPairs[n]?.sellPriceIfFresh() ?: internalProvider.nCoinSellingPrice(n))
        else internalProvider.nCoinSellingPrice(n)

    override fun oneCoinBuyingCost(): PriceQuantityPair? =
        if (allowCache) cachedPairs[BigDecimal.ONE]?.buyPriceIfFresh() ?:
        internalProvider.oneCoinBuyingCost()
        else internalProvider.oneCoinBuyingCost()

    override fun oneCoinSellingPrice(): PriceQuantityPair? =
        if (allowCache) (cachedPairs[BigDecimal.ONE]?.sellPriceIfFresh() ?:
        internalProvider.oneCoinSellingPrice())
        else internalProvider.oneCoinSellingPrice()



}