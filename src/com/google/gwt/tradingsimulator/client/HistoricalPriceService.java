package com.google.gwt.tradingsimulator.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("historicalPrices")
public interface HistoricalPriceService extends RemoteService {
  String getName(String Symbol) throws HistoricalPriceServiceException;
  QuotePlus[] getQuotesPlus(String symbol, String year, int maxYears) throws HistoricalPriceServiceException;
}
