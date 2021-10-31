package com.demo.exchange.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("prices")
data class PriceFrame(
    val priceCents : Long,
    val epoch : Long,
    val coinType: CoinType,
    val exchangeId : ExchangeId,
    @Id var id: Long = 0
)

enum class CoinType {
    BITCOIN, ETHEREUM;

    companion object {
        val logoUrls = mapOf(
            BITCOIN to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9a/BTC_Logo.svg/1200px-BTC_Logo.svg.png",
            ETHEREUM to "https://download.logo.wine/logo/Ethereum/Ethereum-Logo.wine.png"
        )
    }

}

enum class ExchangeId {
    Nil,
    CoinBase,
    Gemini,
    // TODO
}


