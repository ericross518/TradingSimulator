package com.google.gwt.tradingsimulator.client;

//import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Date;

public class Quote implements Serializable {

	/**
	 * Quote contains the Yahoo finance quote info.  Note all price values are based on the
	 * adjusted close.  Prices are retroactively modified so all stock splits and price adjustments 
	 * are based on the final, current price.
	 */
	private static final long serialVersionUID = -1130636923953980312L;
	private String symbol;
	private String year;
	private Date date;
	private double open;
	private double high;
	private double low;
	private double close;
	private String dateString;
	
	public Quote() {
	}
	
	public Quote(String symbol, String year, Date date, double open, double high, double low, double close, String dateString) {
		this.symbol = symbol;
		this.year = year;
	    this.date = date;
	    this.open = open;
	    this.high = high;
	    this.low = low;
	    this.close = close;
	    this.dateString = dateString;
	}
	
	public String getSymbol() {
	    return this.symbol;
	}
	
	public Date getDate() {
	    return this.date;
	}
	
	public String getYear() {
	    return this.year;
	}
	
	public double getOpen() {
	    return this.open;
	}
	
	public double getHigh() {
	    return this.high;
	}
	
	public double getLow() {
	    return this.low;
	}
	
	public double getClose() {
	    return this.close;
	}
	
	public String getDateString() {
	    return this.dateString;
	}

	  
	public static	double[] getCloses(Quote[] array) {
		double[] close = new double[array.length];
		int i=0;
		for (Quote q: array) {
			close[i++] = q.close;
		}
		return close;
	}
		
	public static Date[] getDates(Quote[] array) {
		Date date[] = new Date[array.length];
		int i=0;
		for (Quote q: array) {
			date[i++] = q.date;
		}
		return date;
	}
	  
	public static double getMax(Quote[] array) {
		double max=0;
		for (Quote x: array) {
			if (x.high > max) max = x.high;
		}
		return max;
	}
	
	public static double getMin(Quote[] array) {
		double min=0;
		for (Quote x: array) {
			if (x.low < min || min == 0) min = x.low;
		}
		return min;
	}
	
	public boolean up () {
		return (close>open);
    }
}

