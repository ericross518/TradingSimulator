package com.google.gwt.tradingsimulator.client;

import java.io.Serializable;

public class HistoricalPriceServiceException extends Exception implements Serializable {
	String symbol;
	String message;
	/**
	 * 
	 */
	private static final long serialVersionUID = -6883657543115126158L;

	public HistoricalPriceServiceException() {
	}

	public HistoricalPriceServiceException(String symbol, String message) {
		this.symbol = symbol;
		this.message = message;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public String getMessage() {
		return this.message;
	}

}
