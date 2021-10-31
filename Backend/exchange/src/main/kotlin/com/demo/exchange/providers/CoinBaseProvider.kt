package com.demo.exchange.providers

import com.demo.exchange.model.CoinType
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

@OptIn(DelicateCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
class CoinBaseProvider(
    val coinType: CoinType
) : L2BookKeeper() {

    private val logger = LoggerFactory.getLogger(CoinBaseProvider::class.java)
    private val objectMapper = ObjectMapper()

    private data class Snapshot(
        @JsonProperty("type") var type: String,
        @JsonProperty("product_id") var productId: String,
        @JsonProperty("asks") var asks: Array<Array<String>>,
        @JsonProperty("bids") var bids: Array<Array<String>>,
    )

    private data class L2Update(
        @JsonProperty("type") var type: String,
        @JsonProperty("product_id") var productId: String,
        @JsonProperty("changes") var changes: Array<Array<String>>,
        @JsonProperty("time") var timeStamp: String,
    )

    private val client = ReactorNettyWebSocketClient(
        HttpClient.create(),

        WebsocketClientSpec
            .builder()
            .maxFramePayloadLength(2621440) // set max to ~2MB to get first lv2 frame
    )



    companion object {

        const val exchangeName = "CoinBase"

        val exchangeInfo = ExchangeInfo(
            exchangeName,
            "https://pbs.twimg.com/profile_images/1389269073452670978/d5cUIuYo.png",
            "https://www.coinbase.com/price",
            mapOf(
                CoinType.BITCOIN to "https://www.coinbase.com/price/bitcoin",
                CoinType.ETHEREUM to "https://www.coinbase.com/price/ethereum",
            ),
            CoinType.values().associate { it to CoinType.logoUrls[it]!! },
        )

        const val websocketUrl = "wss://ws-feed.exchange.coinbase.com"

        fun coinTypeToUSDMarket(coinType: CoinType): String {
            return when (coinType) {
                CoinType.BITCOIN -> "BTC-USD"
                CoinType.ETHEREUM -> "ETH-USD"
            }
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
                            "product_ids": [
                                "${coinTypeToUSDMarket(coinType)}"
                            ],
                            "channels": [
                                "level2"
                            ]
                        }
                    """.trimIndent().let { session.textMessage(it).toMono() }
                ).thenMany(
                    session
                        .receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(this@CoinBaseProvider::handleAPIMessage)
                ).then()
            }.block()
        }

    }

    private fun handleAPIMessage(payload: String) {
        val map = JsonParserFactory.getJsonParser().parseMap(payload)

        if ("type" in map) {
            when (map["type"] as String) {
                "snapshot" -> handleSnapshotPayload(payload)
                "l2update" -> handleUpdate(payload)
                else -> Unit
            }
        }

    }

    private fun handleSnapshotPayload(payload: String) {
        val snapshot = objectMapper.readValue(payload, Snapshot::class.java)

        logger.info("handling snapshot...")

        snapshot
            .asks
            .map {
                PriceQuantityPair(it[0].toBigDecimal(), it[1].toBigDecimal())
            }.let {
                handleFrameUpdatesToMap(it, askPricesMap)
            }
        snapshot
            .bids
            .map {
                PriceQuantityPair(it[0].toBigDecimal(), it[1].toBigDecimal())
            }.let {
                handleFrameUpdatesToMap(it, bidPricesMap)
            }

        logger.info("cheapest entry: ${askPricesMap.firstEntry()}")

    }

    private fun handleUpdate(payload: String) {
        val update = objectMapper.readValue(payload, L2Update::class.java)

        if (update.changes.isEmpty() || update.changes[0].size != 3) {
            logger.warn("received malformed L2Update package!")
            return
        }

        update.changes
            .filter {it.size == 3}
            .mapNotNull {
                val directive = when(it[0]){
                    "buy" -> L2Directive.BID
                    "sell" -> L2Directive.ASK
                    else -> return@mapNotNull null
                }
                L2UpdateFrame(
                    directive,
                    it[1].toBigDecimal(),
                    it[2].toBigDecimal(),
                )
            }.let {
                handleUpdateFrames(it)
            }

    }

    override val exchangeName: String
        get() = Companion.exchangeName


    override fun getBestBidPrice(): PriceQuantityPair {
        val e = bidPricesMap.firstEntry()
        return PriceQuantityPair(e.key, e.value)
    }

    override fun getBestAskingPricePair(): PriceQuantityPair {
        val e = askPricesMap.firstEntry()
        return PriceQuantityPair(e.key, e.value)
    }

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