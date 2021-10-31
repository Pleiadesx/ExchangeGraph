import { ExchangeId, ExchangeInfo, IncomingPriceFrame } from "./priceDataSlice";
import {encodeAndAddWellKnownMetadata, MESSAGE_RSOCKET_ROUTING} from 'rsocket-core'
import {ReactiveSocket } from 'rsocket-types'
import { apiExchangeInfoEndPoint } from "../../config";

export function getExchangeInfoPlaceholder(exchangeId: ExchangeId) : ExchangeInfo {

    return {
        exchangeLogoUrl: "",
        exchangeName: exchangeId,
        exchangeUrl: "",
        marketUrls: {},
        marketLogoUrls: {}
    }

}

export const getRequestStream = (rsocket: ReactiveSocket<any, any>, endpoint: String) => {

    return rsocket.requestStream(
        {
            metadata:
                encodeAndAddWellKnownMetadata(
                    Buffer.alloc(0),
                    MESSAGE_RSOCKET_ROUTING,
                    Buffer.from(String.fromCharCode(endpoint.length) + endpoint)
                )
        }
    )

}


export const oneCoinPriceFrameFromJsonString = (s: string) : IncomingPriceFrame => {

    const json = JSON.parse(s)
    return {
        market: json.coinType,
        exchangeId: json.exchangeId,
        oneCoinBuyPrice: json.bestBuyPrice.price,
        oneCoinSellPrice: json.bestSellPrice.price
    }


}

export async function getExchangeInfoRequest(exchangeId: ExchangeId) : Promise<ExchangeInfo> {

    try {
        const res = await fetch(apiExchangeInfoEndPoint + exchangeId)
        const json = await res.json()
        return {
            exchangeName: json.exchangeName,
            exchangeLogoUrl: json.exchangeLogoUrl,
            exchangeUrl: json.exchangeUrl,
            marketUrls: json.marketUrls,
            marketLogoUrls: json.marketLogoUrls,
        }

    } catch (e) {
        console.log("error: ")
        console.log(e)
        return getExchangeInfoPlaceholder(exchangeId)
    }

}

