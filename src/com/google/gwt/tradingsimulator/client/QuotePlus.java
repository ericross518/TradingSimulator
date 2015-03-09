package com.google.gwt.tradingsimulator.client;

import java.util.Date;

public class QuotePlus extends Quote {
	
	/**
	 * Quote Plus contains the pixel value of the O,H,L,C for drawing candle sticks.
	 * It also contains the price value for the moving averages.
	 * The static function pixY returns the proper pixel value for a given price.
	 */
	private static final long serialVersionUID = 1360384756403375523L;
	private double openY;
	private double highY;
	private double lowY;
	private double closeY;
	private double ma20;
	private double ma50;
	private double ma200;
	//public Rectangle candle = new Rectangle();
	//public Rectangle stick = new Rectangle();
	//public Rectangle close = new Rectangle();
	//public Rectangle ma1 = new Rectangle();
	//public Rectangle ma2 = new Rectangle();
	//public Rectangle ma3 = new Rectangle();

	public QuotePlus() {
	}

	public QuotePlus(String symbol, String year, Date date, double open,
			double high, double low, double close, String dateString) {
		super(symbol, year, date, open, high, low, close, dateString);
	}
	// This are for the log values between max and min
	public double getOpenY() {
		return openY;
	}
	public double getHighY() {
		return highY;
	}
	public double getLowY() {
	    return lowY;
	}
	public double getCloseY() {
	    return closeY;
	}
	public double getCandleY() {
		return closeY<openY?closeY:openY;
	}
	public double getCandleHeight() {
		return closeY<openY?openY-closeY:closeY-openY;
	}
	public double getStickY() {
		return lowY<highY?lowY:highY;
	}
	public double getStickHeight() {
		return lowY<highY?highY-lowY:lowY-highY;
	}
	public double getMA200() {
		return ma200;
	}
	public double getMA50() {
		return ma50;
	}
	public double getMA20() {
		return ma20;
	}
	
	public void setQuotePlus (double max, double min, double height) {
		openY = pixY(getOpen(), max, min, height);
		highY = pixY(getHigh(), max, min, height);
		lowY = pixY(getLow(), max, min, height);
		closeY = pixY(getClose(), max, min, height);
	}

	public void setMA20 (double value) {
		ma20 = value;
	}
	public void setMA50 (double value) {
		ma50 = value;
	}
	public void setMA200 (double value) {
		ma200 = value;
	}
	public static double pixY (double value, double max, double min, double height) {
		return Math.rint(height-(value - min)/(max - min)*height/2);
	}
	
}
