import { configureStore, ThunkAction, Action } from '@reduxjs/toolkit';
import priceDataReducer from '../features/chart/priceDataSlice';

export const store = configureStore({
  reducer: {
    priceData: priceDataReducer
  },
  middleware: (getDefaultMiddleware) => getDefaultMiddleware({serializableCheck: false})
});

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<
  ReturnType,
  RootState,
  unknown,
  Action<string>
>;
