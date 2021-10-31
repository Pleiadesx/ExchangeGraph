import { ExchangeId, MarketId, PriceFrame } from "./priceDataSlice";




export function getAbsoluteReduced(
    exchangeData: Record<ExchangeId, Record<MarketId, PriceFrame>>,
    reducerFunc: (n1: number, n2: number) => number,
    getterFunc: (frame: PriceFrame) => number,
    ) {


    return Object.entries(exchangeData).reduce<Record<MarketId, number>>(

        (acc, [exchangeId, marketRecord]) => {

            const innerReduced = Object.entries(marketRecord).reduce<Record<MarketId, number>>(
                (accIn, [marketId, frame]) => {
                    if (!(marketId in accIn)) {
                        return {
                            ...accIn,
                            [marketId]: getterFunc(frame)
                        }
                    } else {
                        return {
                            ...accIn,
                            [marketId]: reducerFunc(getterFunc(frame), accIn[marketId])
                        }
                    }
                }, {}
            )

            return Object.entries(innerReduced).filter(
                ([marketId, price]) => {
                    return (!(marketId in acc) || reducerFunc(price, acc[marketId]) === price)
                }
            ).reduce(
                (accIn, [marketId, price]) => {
                    return {
                        ...accIn,
                        [marketId]: price
                    }
                }, acc
            )

        }, {}
    )



}


