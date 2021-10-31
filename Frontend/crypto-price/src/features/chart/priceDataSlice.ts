import {BufferEncoders, RSocketClient} from "rsocket-core"
import {
    createAsyncThunk,
    createSlice,
    PayloadAction
} from "@reduxjs/toolkit";


import {
    RootState,
 } from "../../app/store";
import { ReactiveSocket } from 'rsocket-types'
import { getExchangeInfoPlaceholder, getExchangeInfoRequest, getRequestStream, oneCoinPriceFrameFromJsonString } from "./dataApi";
import { Dispatch } from "react";
import RSocketWebSocketClient from "rsocket-websocket-client";




export interface IncomingPriceFrame {
    market: string,
    exchangeId: string,
    oneCoinBuyPrice: number,
    oneCoinSellPrice: number
}

export interface PriceFrame {
    oneCoinBuyPrice: number,
    oneCoinSellPrice: number
}

export interface ExchangeInfo {
    exchangeName: string,
    exchangeLogoUrl: string,
    exchangeUrl: string,
    marketUrls: Record<MarketId, string>,
    marketLogoUrls: Record<MarketId, string>,
}

export enum DisplayMode {
    TWO_BY_TWO
}

export type ExchangeId = string
export type MarketId = string



export interface PriceDataState {
    exchangeInfoStore: Record<ExchangeId, ExchangeInfo>
    data: Record<ExchangeId, Record<MarketId, PriceFrame>>,
    history: Record<ExchangeId, Record<MarketId, Array<PriceFrame>>>,
    dataSocketClient?: RSocketClient<any, any>,
    dataSocket?: ReactiveSocket<any, any>,
    displayMode: DisplayMode,
    dataLimit: number
}


const initialState: PriceDataState = {
    exchangeInfoStore: {},
    data: {},
    history: {},
    displayMode: DisplayMode.TWO_BY_TWO,
    dataLimit: 100
}

function createPriceFrame(incomingPriceFrame: IncomingPriceFrame) : PriceFrame {
    return {
        oneCoinBuyPrice: incomingPriceFrame.oneCoinBuyPrice,
        oneCoinSellPrice: incomingPriceFrame.oneCoinSellPrice
    }
}

const isExchangeInfoRecorded = (
    frame: IncomingPriceFrame,
    state: PriceDataState) => {

    return frame.exchangeId in state.exchangeInfoStore;
}


function processExchangeInfoReceived(state: PriceDataState, dispatch: Dispatch<any>) {

}


export const connectToRSocket = createAsyncThunk<
    ReactiveSocket<any, any>,
    {url: string, mimeType?: string}
>(
    "priceData/connectToRSocket",
    async ({url, mimeType = "application/json"}) => {
        const client = new RSocketClient(
            {
                transport: new RSocketWebSocketClient(
                    {
                        url: url,
                        wsCreator: u => new WebSocket(u)
                    }, BufferEncoders
                ),
                setup: {
                    dataMimeType: mimeType,
                    metadataMimeType: "message/x.rsocket.composite-metadata.v0",
                    keepAlive: 20000,
                    lifetime: 60000
                },
                errorHandler: e => {console.log(e)}
            }
        )

        const sock = await client.connect()
        return sock
    }
)

export const fetchDataFrames = createAsyncThunk<
    void,
    {endpoint: string, perMin?: number},
    {
        state: RootState
    }
>(
    "priceData/FetchDataFrames",
    async ({endpoint, perMin = 1000}, {getState, rejectWithValue, dispatch}) => {
        const rsocket = getState().priceData.dataSocket
        if (rsocket !== undefined) {
            getRequestStream(rsocket, endpoint)
                .subscribe({

                    onSubscribe: s => {
                        setInterval(() => s.request(perMin))
                        s.request(perMin)

                    },
                    onNext: e =>  {
                        const priceFrame =oneCoinPriceFrameFromJsonString(e.data)
                        dispatch(processFrameReceived(priceFrame))
                    },
                    onError: e => {
                        console.log(e)
                    }
                })
        }
    }
)

export const processFrameReceived = createAsyncThunk<
    IncomingPriceFrame,
    IncomingPriceFrame,
    {
        state: RootState
    }
>(
    "priceData/frameReceived",
    async (frame: IncomingPriceFrame, {getState, dispatch}) => {

        const actions = priceDataSlice.actions
        const state = getState()

        if (!isExchangeInfoRecorded(frame, state.priceData)) {
            // need to get exchange info and add it
            dispatch(
                actions
                .createExchangeInfoPlaceholder(frame.exchangeId))

            // get the info and dispatch
            const exchangeInfo = await getExchangeInfoRequest(frame.exchangeId)
            dispatch(actions.updateExchangeInfo({id: frame.exchangeId, info: exchangeInfo}))

            // proccess the info returned
            processExchangeInfoReceived(state.priceData, dispatch)

        }


        return frame;
    }
)

export const priceDataSlice = createSlice({
    name: 'priceData',
    initialState,
    reducers: {
        createExchangeInfoPlaceholder: (state, action: PayloadAction<ExchangeId>) => {
            if (action.payload !in state.exchangeInfoStore){
                state.exchangeInfoStore[action.payload] = getExchangeInfoPlaceholder(action.payload)
            }
        },
        updateExchangeInfo: (state, action: PayloadAction<{id: ExchangeId, info: ExchangeInfo}>) => {
            state.exchangeInfoStore[action.payload.id] = action.payload.info
        },
        updateDataLimit: (state, action: PayloadAction<number>) => {
            state.dataLimit = action.payload
        }
    },
    extraReducers: (builder) => {
        builder.addCase(processFrameReceived.fulfilled,  (state, action) => {

            if (!(action.payload.exchangeId in state.data)) {
                // if the data frame has not been created add it
                const dataFrame = createPriceFrame(action.payload)
                const historyFrame = createPriceFrame(action.payload)

                state.data[action.payload.exchangeId] = {
                    [action.payload.market]: dataFrame
                }

                state.history[action.payload.exchangeId] = {
                    [action.payload.market]: Array(historyFrame)
                }


            } else {
                const priceFrame = createPriceFrame(action.payload)
                const historyFrame = createPriceFrame(action.payload)
                state.data[action.payload.exchangeId][action.payload.market] = priceFrame
                if (action.payload.market in state.history[action.payload.exchangeId]) {
                    state.history[action.payload.exchangeId][action.payload.market].push(historyFrame)
                } else {
                    state.history[action.payload.exchangeId][action.payload.market] = [historyFrame]
                }

                if (state.dataLimit > -1 &&
                    state.history[action.payload.exchangeId][action.payload.market].length > state.dataLimit) {

                        state.history[action.payload.exchangeId][action.payload.market].shift()
                }

            }

        })
        builder.addCase(connectToRSocket.fulfilled, (state, action) => {
            console.log("connected to rsocket fulfilled")
            state.dataSocket = action.payload
        })
        builder.addCase(fetchDataFrames.fulfilled, (state, action) => {

        })
    }
});


export const appSelectorValid = (state: RootState) => state.priceData.dataSocket !== undefined
export const rsocketSelector = (state: RootState) => state.priceData.dataSocket
export const dataSelector = (state: RootState) => state.priceData.data
export const historySelector = (state: RootState) => state.priceData.history

export const dataSelectorThunk = (exchangeId: ExchangeId, marketId: MarketId) => (state: RootState) =>
    state.priceData.data[exchangeId]?.[marketId]

export const historySelectorThunk = (exchangeId: ExchangeId, marketId: MarketId) => (state: RootState) =>
    state.priceData.history[exchangeId]?.[marketId]

export const exchangeInfoStoreSelector = (state: RootState) => state.priceData.exchangeInfoStore


export default priceDataSlice.reducer
