package com.google.gwt.tradingsimulator.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HistoricalPriceServiceAsync {
	void getName(String symbol, AsyncCallback<String> callbackString);
	void getQuotesPlus(String symbol, String year, int maxYears, AsyncCallback<QuotePlus[]> callback);

}
