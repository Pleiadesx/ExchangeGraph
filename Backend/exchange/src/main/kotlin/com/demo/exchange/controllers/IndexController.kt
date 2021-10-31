package com.demo.exchange.controllers

import com.demo.exchange.providers.ExchangeInfo
import com.demo.exchange.service.MarketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["*"])
@RestController
class IndexController {

    @Autowired
    lateinit var marketService: MarketService


    @GetMapping("/price")
    fun getPrice() : String {
        return marketService.marketPairs.first().provider.getBestBidPrice().toString()
    }

    @GetMapping("/exchangeInfo/{exchangeName}")
    fun getExchangeInfo(@PathVariable exchangeName: String) : ExchangeInfo? =
        marketService.getExchangeInfoByName(exchangeName)


}