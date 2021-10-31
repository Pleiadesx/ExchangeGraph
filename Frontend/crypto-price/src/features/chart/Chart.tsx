import {useEffect} from 'react'
import Container from 'react-bootstrap/Container'
import Row from 'react-bootstrap/Row'
import Col from 'react-bootstrap/Col'


import { useAppSelector, useAppDispatch } from '../../app/hooks'
import { priceFramesEndpoint, rsocketUrl } from '../../config'

import {
    connectToRSocket,
    rsocketSelector,
    fetchDataFrames,
    dataSelector,
    historySelector,
    exchangeInfoStoreSelector,
} from './priceDataSlice'
import ExchangeCell from './cell/ExchangeCell'
import { getAbsoluteReduced } from './util'



export const Chart = () => {
    const rsocket = useAppSelector(rsocketSelector)
    const data = useAppSelector(dataSelector)
    const history = useAppSelector(historySelector)
    const exchangeInfoStore = useAppSelector(exchangeInfoStoreSelector)

    const dispatch = useAppDispatch()

    useEffect(() => {
        if (rsocket === undefined) {
            dispatch(connectToRSocket({url: rsocketUrl}))
        }
    })

    useEffect(() => {
        if (rsocket !== undefined) {
            console.log("rsocket was valid")
            dispatch(fetchDataFrames({endpoint: priceFramesEndpoint}))
        } else {
            console.log("rsocket was not valid")
        }
    }, [rsocket])


    const minBuy = getAbsoluteReduced(
        data,
        (n1, n2) => Math.min(n1, n2),
        (frame) => frame.oneCoinBuyPrice
    )

    const maxSell = getAbsoluteReduced(
        data,
        (n1, n2) => Math.max(n1, n2),
        (frame) => frame.oneCoinSellPrice
    )

    return (
        <div>
            <Container fluid>
                <Row>
                {
                    Object.entries(data).map(
                        ([exchangeId, marketRecord]) =>
                        <Col>
                            <ExchangeCell key={exchangeId}
                                maxSell={maxSell}
                                minBuy={minBuy}
                                exchangeInfo={
                                    exchangeId in exchangeInfoStore ?
                                    exchangeInfoStore[exchangeId] : null
                                }
                                marketRecord={marketRecord}
                                historyRecord={history[exchangeId]}

                            />
                        </Col>
                    )
                }
                </Row>
            </Container>
        </div>
    )

}

