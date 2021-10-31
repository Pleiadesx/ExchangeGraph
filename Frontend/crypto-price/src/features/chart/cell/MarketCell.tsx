
import { MarketId, PriceFrame } from '../priceDataSlice'
import Card from 'react-bootstrap/Card'
import 'bootstrap/dist/css/bootstrap.min.css'

import {Col, Row, Image} from 'react-bootstrap'
import {
    HorizontalGridLines,
    VerticalGridLines,
    XAxis,
    XYPlot,
    YAxis,
    LineSeries,
    DiscreteColorLegend
} from 'react-vis'
import { currencyFormatter } from '../../../config'

const buyPricesColor = "#51ff97"
const sellPricesColor = "#ff2424"
const highlightedColor = "#545D69"


const formatMarketId = (marketId: MarketId) => {

    return marketId.charAt(0).toUpperCase() + marketId.toLowerCase().slice(1)
}


const LineChartFromHistory = (history: PriceFrame[]) => {
    if (history.length > 1) {



        const ITEMS = [
            {title: "Buy Price", color: buyPricesColor},
            {title: "Sell Price", color: sellPricesColor}
        ]
        const dataBuy = history.map((v, idx) =>  {
            return {x: idx, y: v.oneCoinBuyPrice}
        })
        const dataSell = history.map((v, idx) => {
            return {x: idx, y: v.oneCoinSellPrice}
        })
        return (
            <div>

            <Row>
            <Col xs={10}>
            <XYPlot margin={{left: 100}} width={700} height={500}>
                    <HorizontalGridLines/>
                    <VerticalGridLines/>
                    <XAxis/>
                    <YAxis />
                    <LineSeries
                        curve={'curveMonotoneX'}
                        data={dataBuy}
                        color={buyPricesColor}
                    />
                    <LineSeries
                        curve={'curveMonotoneX'}
                        data={dataSell}
                        color={sellPricesColor}
                    />

            </XYPlot>
            </Col>
            <Col>
            <DiscreteColorLegend  items={ITEMS}/>
            </Col>
            </Row>

            </div>
        )

    }
}
const MarketHeader = (marketId: MarketId, marketLogoUrl?: string, marketUrl?: string) => {

    if (marketLogoUrl === undefined) {
        if (marketUrl === undefined || marketUrl.length === 0) {
            return formatMarketId(marketId)
        } else {
            return (
                <div>
                    <a href={marketUrl}>{formatMarketId(marketId)}</a>
                </div>
            )
        }
    }

    return (
        <div>
            <Image style={{width: 30, height: 'auto'}} src={marketLogoUrl} roundedCircle/>
            {' '} <a href={marketUrl} className='market-cell-name'>{formatMarketId(marketId)}</a>
        </div>
    )


}

interface MarketCellProps {
    marketId: MarketId,
    price: PriceFrame,
    history: PriceFrame[],
    highlighted?: {
        buy?: boolean,
        sell?: boolean
    },
    logoUrl?: string,
    marketUrl?:string,
}

export default function MarketCell(
    {marketId,
        price,
        history,
        highlighted = {buy: false, sell: false},
        logoUrl,
        marketUrl,
    }: MarketCellProps
) {


    return (
        <div>
        <Card className='market-card' text='white'>
            <Card.Header>
                {MarketHeader(marketId, logoUrl, marketUrl)}
            </Card.Header>
            <Card.Body>
                <Row>
                <Col>
                <div className="chart-buy-price" style={

                    highlighted?.buy ? {color: buyPricesColor, background: highlightedColor} :
                    {color: buyPricesColor}
                }>

                Buy: {currencyFormatter.format(price.oneCoinBuyPrice)}

                </div>
                </Col>
                <Col>
                <div className="chart-sell-price" style={
                    highlighted?.sell ? {color: sellPricesColor, background: highlightedColor} :
                    {color: sellPricesColor}

                    }>
                    Sell: {currencyFormatter.format(price.oneCoinSellPrice)}
                </div>
                </Col>
                </Row>
                {LineChartFromHistory(history)}
            </Card.Body>
        </Card>
        </div>
    )

}
