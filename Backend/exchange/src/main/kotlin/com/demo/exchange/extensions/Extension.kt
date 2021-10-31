package com.demo.exchange.extensions

import com.demo.exchange.providers.CachedProvider
import com.demo.exchange.providers.ExchangeMarketPriceProvider
import java.math.BigDecimal
import java.time.Duration


fun ExchangeMarketPriceProvider.toCachedProvider(
    cachedSet: Set<BigDecimal> = setOf(BigDecimal.ONE),
    cachePeriod: Duration = Duration.ofSeconds(1),
    cacheStale: Duration = Duration.ofSeconds(1),
    allowCache: Boolean = true
    ) : CachedProvider =
    CachedProvider(this, cachedSet, cachePeriod, cacheStale, allowCache)