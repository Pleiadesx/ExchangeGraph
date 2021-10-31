package com.demo.exchange.providers

import java.math.BigDecimal
import java.util.concurrent.ConcurrentNavigableMap
import java.util.concurrent.ConcurrentSkipListMap

abstract class TreeMapAskBidProvider : ExchangeMarketPriceProvider {


    protected val bidPricesMap: ConcurrentNavigableMap<BigDecimal, BigDecimal> = ConcurrentSkipListMap<BigDecimal, BigDecimal>().descendingMap()
    protected val askPricesMap = ConcurrentSkipListMap<BigDecimal, BigDecimal>()

    protected fun accumulateFromMap(qty: BigDecimal,
                                    map: Map<BigDecimal, BigDecimal>) : PriceQuantityPair? {

        var cost = BigDecimal.ZERO
        var coinAmount = BigDecimal.ZERO

        for ((askingPrice, quantity) in map) {
            if ((coinAmount + quantity).compareTo(qty) >= 0) {
                cost += (BigDecimal.ONE - coinAmount) * askingPrice
                coinAmount += (BigDecimal.ONE - coinAmount) // obviously equals one, just need to check
                break
            } else {
                cost += askingPrice * quantity
                coinAmount += quantity
            }
        }

        if (coinAmount.compareTo(qty) != 0) {
            return null
        }

        return PriceQuantityPair(cost, BigDecimal.ONE)
    }


}