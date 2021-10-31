import { ExchangeInfo, MarketId, PriceFrame } from "../priceDataSlice";
import MarketCell from './MarketCell'
import Row from 'react-bootstrap/Row'
import { Card } from "react-bootstrap";
import Image from 'react-bootstrap/Image'




interface ExchangeCellProps {

    exchangeInfo: ExchangeInfo | null,
    marketRecord: Record<MarketId, PriceFrame>,
    historyRecord: Record<MarketId, PriceFrame[]>
    maxSell: Record<MarketId, number>,
    minBuy: Record<MarketId, number>,

}

const ExchangeHeader = (exchangeInfo: ExchangeInfo | null) => {

    if (exchangeInfo != null) {
        return (
            <div>
                <Image style={{width: 30, height: 'auto'}} src={exchangeInfo.exchangeLogoUrl} roundedCircle/>
                {' '}
                <a href={exchangeInfo?.exchangeUrl} className='exchange-cell-name'>
                    {exchangeInfo?.exchangeName}
                </a>
            </div>
        )
    } else {
        return ' '
    }

}


export default function ExchangeCell(
    {exchangeInfo, marketRecord, historyRecord, minBuy, maxSell} : ExchangeCellProps
) {



    const highlighted = (marketId : MarketId, priceFrame: PriceFrame) => {
        return {
            buy: minBuy[marketId] ? minBuy[marketId] === priceFrame.oneCoinBuyPrice  : false,
            sell: maxSell[marketId] ? maxSell[marketId] === priceFrame.oneCoinSellPrice : false
        }
    }


    return (
        <div>
            <Card className="exchange-card" text='white'>
                <Card.Header>

                    {ExchangeHeader(exchangeInfo)}

                </Card.Header>
                <Card.Body>
            {Object.entries(marketRecord).sort((e1, e2) => e1[0].localeCompare(e2[0])).map(
                ([marketId, priceFrame]) =>
                    <Row style={{padding: 10}} key={marketId}>
                    <MarketCell
                        highlighted={highlighted(marketId, priceFrame)}
                        marketId={marketId}
                        price={priceFrame}
                        history={historyRecord[marketId]}
                        logoUrl={exchangeInfo?.marketLogoUrls?.[marketId]}
                        marketUrl={exchangeInfo?.marketUrls?.[marketId]}
                        />
                    </Row>
                )

            }
            </Card.Body>
            </Card>
        </div>
    )

}

