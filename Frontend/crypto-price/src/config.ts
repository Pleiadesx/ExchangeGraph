
// hardcoding this for now
export const priceFramesEndpoint = "api.prices.one_coin_stream"
export const rsocketUrl = `ws://localhost:${process.env.PORT || 8080}/rsocket`
export const apiExchangeInfoEndPoint = "/exchangeInfo/"
export const currencyFormatter = new Intl.NumberFormat('en-us',
    {
        style:'currency',
        currency: 'USD'
    }
);