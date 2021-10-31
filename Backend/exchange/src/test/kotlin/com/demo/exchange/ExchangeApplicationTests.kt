package com.demo.exchange

import com.demo.exchange.model.CoinType
import com.demo.exchange.model.ExchangeId
import com.demo.exchange.model.PriceFrame
import com.demo.exchange.repository.PriceFrameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = [
		"spring.r2dbc.url=r2dbc:h2:mem:///testdb;USER=sa;PASSWORD=password"
	]
)
class ExchangeApplicationTests(
	@Autowired val priceFrameRepository: PriceFrameRepository
) {

	var lastFrameId: Long = 0

	@BeforeEach
	fun setup() {
		runBlocking {
			val priceFrames =
				listOf(
					PriceFrame(
						10005,
						1000,
						CoinType.BITCOIN,
						ExchangeId.Nil
					),
					PriceFrame(
						20005,
						2000,
						CoinType.BITCOIN,
						ExchangeId.Nil
					),
					PriceFrame(
						20005,
						3000,
						CoinType.ETHEREUM,
						ExchangeId.Nil
					)
				)

			val savedFrames = priceFrameRepository.saveAll(priceFrames).toList()
			lastFrameId = savedFrames.first().id ?: 0
		}
	}

	@AfterEach
	fun freeResources() {
		runBlocking {
			priceFrameRepository.deleteAll()
		}
	}

	@Test
	fun `test that most recent by coin type works`(){
		runBlocking {
			val bitcoinFrame = priceFrameRepository.findLatestByCoinType(CoinType.BITCOIN)
			val ethereumFrame = priceFrameRepository.findLatestByCoinType(CoinType.ETHEREUM)

//			val a = priceFrameRepository.findAll().toList()
//			println(a)
			assert(bitcoinFrame.first().coinType == CoinType.BITCOIN)
			assert(ethereumFrame.first().coinType == CoinType.ETHEREUM)

		}
	}


}
