package com.demo.exchange.controllers

import com.demo.exchange.service.ExchangePrices
import com.demo.exchange.service.MarketService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller


@Controller
@MessageMapping("api.prices")
class PriceSocketController(val marketService: MarketService) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(PriceSocketController::class.java)
    }

    @MessageMapping("stream")
    suspend fun receive(@Payload inMessage: String) {
        logger.info("received message: $inMessage")
    }

    @MessageMapping("stream")
    suspend fun sendBestPrices(): Flow<String> {
        logger.info("sending stream...")
        return marketService.streamBestPrices().map { it.toString() }
    }

    @MessageMapping("one_coin_stream")
    suspend fun sendOneCoinPrices() : Flow<ExchangePrices?> =
        marketService.streamOneCoinPrices()




}