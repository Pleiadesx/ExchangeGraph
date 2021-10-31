package com.demo.exchange.providers

import com.demo.exchange.model.CoinType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.json.JsonParserFactory
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.kotlin.core.publisher.toMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.WebsocketClientSpec
import java.math.BigDecimal
import java.net.URI
import java.time.Duration

@OptIn(DelicateCoroutinesApi::class)
class GeminiBaseProvider(
    val coinType: CoinType
) : L2BookKeeper() {

    private val logger = LoggerFactory.getLogger(GeminiBaseProvider::class.java)
    private val objectMapper = ObjectMapper()

    // Gemini uses the same "type":"l2_updates" for two different JSON
    // layouts but we only need changes for now
    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class L2Update(
        @JsonProperty("type") var type: String,
        @JsonProperty("symbol") var symbol: String,
        @JsonProperty("changes") var changes: Array<Array<String>>,
    )

    private val client = ReactorNettyWebSocketClient(
        HttpClient.create(),
        WebsocketClientSpec
            .builder()
            .maxFramePayloadLength(2621440)
    )

    companion object {

        const val exchangeName = "Gemini"

        val exchangeInfo = ExchangeInfo(
            exchangeName,
            "https://pbs.twimg.com/profile_images/1237023498696081408/IiLEvZJY_400x400.jpg",
            "https://www.gemini.com/exchange",
            mapOf(
                CoinType.BITCOIN to "https://www.gemini.com/prices/bitcoin",
                CoinType.ETHEREUM to "https://www.gemini.com/prices/ether",
            ),
            arrayOf(CoinType.BITCOIN, CoinType.ETHEREUM).associate { it to CoinType.logoUrls[it]!! },
        )


        val websocketUrl = "wss://api.gemini.com/v2/marketdata"

        fun coinTypeToUSDMarket(coinType: CoinType) : String =
            when (coinType) {
                CoinType.BITCOIN -> "BTCUSD"
                CoinType.ETHEREUM -> "ETHUSD"
            }

    }

    init {
        GlobalScope.launch {

            client.execute(
                URI.create(websocketUrl)
            ) { session ->
                session.send(
                    """
                        {
                            "type": "subscribe",
                            "subscriptions": [
                                {
                                    "name": "l2",
                                    "symbols": [
                                        "${coinTypeToUSDMarket(coinType)}"
                                    ]
                                }
                            ]
                        }
                    """.trimIndent().let {session.textMessage(it).toMono()}
                ).thenMany(
                    session
                        .receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(this@GeminiBaseProvider::handleWebSocketMessage)
                ).then()
            }.block()
        }
    }

    private fun handleWebSocketMessage(payload: String) {
        val map = JsonParserFactory.getJsonParser().parseMap(payload)

        if ("type" in map && map["type"] == "l2_updates") {
            when (map["type"]) {
                "l2_updates" -> {
                    val l2UpdateMap = objectMapper.readValue(payload, L2Update::class.java)
                    handleL2Update(l2UpdateMap.changes)
                }
            }
        }

    }

    private fun handleL2Update(payload: String) {
        val l2Frame = objectMapper.readValue(payload, L2Update::class.java)

        l2Frame
            .changes
            .filter { it.size == 3 }
            .mapNotNull {
                val directive = when(it[0]) {
                    "buy" -> L2Directive.BID
                    "sell" -> L2Directive.ASK
                    else -> return@mapNotNull null
                }
                L2UpdateFrame(
                    directive,
                    it[1].toBigDecimal(),
                    it[2].toBigDecimal()
                )
            }.let {
                handleUpdateFrames(it)
            }

    }

    private fun handleL2Update(changesArray: Array<Array<String>>) {

        changesArray
            .filter { it.size == 3 }
            .mapNotNull {
                val directive = when(it[0]) {
                    "buy" -> L2Directive.BID
                    "sell" -> L2Directive.ASK
                    else -> return@mapNotNull null
                }
                L2UpdateFrame(
                    directive,
                    it[1].toBigDecimal(),
                    it[2].toBigDecimal()
                )
            }.let {
                handleUpdateFrames(it)
            }

    }

    override val exchangeName: String
        get() = "Gemini"

    override fun getBestBidPrice(): PriceQuantityPair? =
        bidPricesMap.firstEntry()?.let { PriceQuantityPair(it.key, it.value) }

    override fun getBestAskingPricePair(): PriceQuantityPair? =
        askPricesMap.firstEntry()?.let { PriceQuantityPair(it.key, it.value) }

    override fun oneCoinBuyingCost(): PriceQuantityPair? {

        val pair = accumulateFromMap(BigDecimal.ONE, askPricesMap)

        if (pair == null) {
            logger.error(
                "Coins in exchange not enough to make one, " +
                        "this is probably an error"
            )
        }

        return pair
    }

    override fun oneCoinSellingPrice(): PriceQuantityPair? {

        val pair = accumulateFromMap(BigDecimal.ONE, bidPricesMap)

        if (pair == null) {
            logger.error(
                "Coins in exchange not enough to make one, " +
                        "this is probably an error"
            )
        }

        return pair
    }

    override fun nCoinBuyingCost(n: BigDecimal): PriceQuantityPair? =
        accumulateFromMap(n, askPricesMap)

    override fun nCoinSellingPrice(n: BigDecimal): PriceQuantityPair? =
        accumulateFromMap(n, bidPricesMap)
}