package com.demo.exchange.repository

import com.demo.exchange.model.CoinType
import com.demo.exchange.model.PriceFrame
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param

interface PriceFrameRepository : CoroutineCrudRepository<PriceFrame, Long> {

    // language=SQL
    @Query("""
            SELECT id, price_cents, epoch, coin_type, exchange_id
            FROM prices
            WHERE epoch = (SELECT MAX(epoch) FROM prices WHERE coin_type = :coinType) 
            AND coin_type = :coinType
            LIMIT :frameLimit
        """
    )
    fun findLatestByCoinType(@Param("coinType") coinType: CoinType,
                                     @Param("frameLimit") limit: Int = 1): Flow<PriceFrame>

}