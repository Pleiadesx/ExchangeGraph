package com.demo.exchange.providers

import java.math.BigDecimal
import java.util.concurrent.ConcurrentNavigableMap


abstract class L2BookKeeper : TreeMapAskBidProvider() {

    enum class L2Directive {
        BID,
        ASK,
    }

    data class L2UpdateFrame(
        val directive: L2Directive,
        val price: BigDecimal,
        val qty: BigDecimal
    )

    protected fun handleUpdateFrames(frames: Iterable<L2UpdateFrame> ) {
        for (frame in frames) {
            val map = when (frame.directive) {
                L2Directive.BID -> bidPricesMap
                L2Directive.ASK -> askPricesMap
            }

            if (frame.qty.compareTo(BigDecimal.ZERO) == 0) {
                map.remove(frame.price)
            } else {
                map[frame.price] = frame.qty
            }

        }
    }

    protected fun handleFrameUpdatesToMap(frames: Iterable<PriceQuantityPair>,
                                          map: ConcurrentNavigableMap<BigDecimal, BigDecimal>) {

        for (frame in frames) {
            if (frame.quantity.compareTo(BigDecimal.ZERO) == 0){
                map.remove(frame.price)
            } else {
                map[frame.price] = frame.quantity
            }
        }
    }


}