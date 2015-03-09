package com.google.gwt.tradingsimulator.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import com.google.gwt.tradingsimulator.client.HistoricalPriceService;
import com.google.gwt.tradingsimulator.client.HistoricalPriceServiceException;
import com.google.gwt.tradingsimulator.client.QuotePlus;
import com.google.gwt.tradingsimulator.server.ta.Core;
import com.google.gwt.tradingsimulator.server.ta.MInteger;
import com.google.gwt.tradingsimulator.server.ta.RetCode;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
public class HistoricalPriceServiceImpl extends RemoteServiceServlet implements HistoricalPriceService {
	/**
	 * Historical Price Service Implementation gets data from Yahoo Finance.
	 * getName gets the name which for some reason is limited to 20 characters.
	 * getQuotesPlus gets the price and moving averages.  
	 * If other indicators are added, moving averages could be moved to a separate call back
	 * function apart from the price data. 
	 */
	private static final long serialVersionUID = 4219997612922242099L;
	private Core lib = new Core();
	// http://finance.yahoo.com/d/quotes.csv?s='aapl'&f=n
	public String getName(String symbol) throws
		HistoricalPriceServiceException {
		String urlString = "http://finance.yahoo.com/d/quotes.csv?s='" + symbol + "'&f=n";	
		String line = "";
		int LINE_SIZE = 80;
		if (urlString.contains("^"))
			urlString = urlString.replace("^", "%5E");
		System.err.println(urlString);
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(10000);  //10 seconds, default 5 seconds.
			connection.setReadTimeout(10000);  //10 seconds, default 5 seconds.
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()),LINE_SIZE);
			try {
				line = reader.readLine();
			} catch (IOException e) {
				System.err.println(symbol + " " + e.toString());
				throw new HistoricalPriceServiceException(symbol, e.toString());
			} 		
			reader.close();
		} catch (MalformedURLException e) {
			System.err.println(symbol + " " + e.toString());
			throw new HistoricalPriceServiceException(symbol, e.toString());
		} catch (IOException e) {
			System.err.println(symbol + " " + e.toString());
			throw new HistoricalPriceServiceException(symbol, e.toString());
		} catch (Exception e) {
			System.err.println(symbol + " " + e.toString());
			throw new HistoricalPriceServiceException(symbol, e.toString());
		}
		line = line.replace("\"","");
		return line;
	}

	
	public QuotePlus[] getQuotesPlus(String symbol, String yearDate, int maxYears) throws
		HistoricalPriceServiceException {
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		int yearNow = calendar.get(Calendar.YEAR);
		int yearThen = yearNow-maxYears+1;
		int monthNow = calendar.get(Calendar.MONTH);
		int monthThen = 0;
		int dayNow = calendar.get(Calendar.DAY_OF_MONTH);
		int dayThen = 1;
		if (yearDate.length()==8) {
			dayThen = Integer.parseInt(yearDate.substring(6,8));
			dayNow = dayThen;
		}
		if (yearDate.length()>=6) {
			monthThen = Integer.parseInt(yearDate.substring(4,6))-1;
			monthNow = monthThen;
		}
		if (yearDate.length()>4) {
			yearThen = Integer.parseInt(yearDate.substring(0,4));
			yearNow = yearThen+maxYears;
		} else if (yearDate.length()==4) {
			yearThen = Integer.parseInt(yearDate.substring(0,4));
			yearNow = yearThen+maxYears-1;
		}
		// http://finance.yahoo.com/q/hp?s=ORCL&a=00&b=1&c=2000&d=00&e=1&f=2004&g=d
		String urlString = "http://ichart.finance.yahoo.com/table.csv?s=" + symbol + "&a=" + monthThen + "&b=" + dayThen + "&c=" + yearThen +
					"&d=" + monthNow + "&e=" + dayNow + "&f=" + yearNow;	
		if (urlString.contains("^"))
			urlString = urlString.replace("^", "%5E");
		System.err.println(urlString);
		int MAX_LINES = 256*maxYears;
		int LINE_SIZE = 80;
		int count=0;
		StringBuffer sb = new StringBuffer();
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(30000);  //30 seconds, default 5 seconds.
			connection.setReadTimeout(30000);  //30 seconds, default 5 seconds.
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()),MAX_LINES*LINE_SIZE);
			String line = "";
//			System.err.println("Timeout: " + connection.getReadTimeout());
			while (line != null && count<MAX_LINES) {
				try {
					line = reader.readLine();
					if (line != null && (line.charAt(0) == '1' || line.charAt(0) == '2')) {			
						sb.append(line + "\n");
						count++;
					}
				} catch (IOException e) {
					System.err.println(symbol + " " + yearDate + " " + e.toString());
					throw new HistoricalPriceServiceException(symbol, e.toString());
				} 		
			}
			reader.close();
		} catch (MalformedURLException e) {
			System.err.println(symbol + " " + yearDate + " " + e.toString());
			throw new HistoricalPriceServiceException(symbol, e.toString());
		} catch (IOException e) {
			System.err.println(symbol + " " + yearDate + " " + e.toString());
			throw new HistoricalPriceServiceException(symbol, e.toString());
		} catch (Exception e) {
			System.err.println(symbol + " " + yearDate + " " + e.toString());
			throw new HistoricalPriceServiceException(symbol, e.toString());
		}
		if (count==0) {
			System.err.println(symbol + " " + yearDate + " " + "DataException");
			throw new HistoricalPriceServiceException (symbol, "DataException");
		}
		QuotePlus[] quotePlus = new QuotePlus[count];

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = "";
		int newLine = sb.lastIndexOf("\n");
		for (int i=0; i<count && newLine > -1; i++) {		
			newLine = sb.lastIndexOf("\n",newLine-1);
			int from=newLine;
			if (sb.charAt(from+1) == 'D') continue;
			int to = sb.indexOf(",", from+1);
	        try {
				date = sdf.parse(sb.substring(from+1,to));
				dateString = sb.substring(from+1,to);
			} catch (ParseException e) {
				System.err.println(symbol + " " + yearDate + " " + e.toString());
				throw new HistoricalPriceServiceException(symbol, e.toString());
			}
			from = to; to = sb.indexOf(",", from+1);
            double open = Double.parseDouble(sb.substring(from+1,to));
            from = to; to = sb.indexOf(",", from+1);
            double high = Double.parseDouble(sb.substring(from+1,to));
            from = to; to = sb.indexOf(",", from+1);
            double low = Double.parseDouble(sb.substring(from+1,to));
            from = to; to = sb.indexOf(",", from+1);
            double close = Double.parseDouble(sb.substring(from+1,to));
            //double volume = Double.parseDouble(splitLine[5]);
            from = to; to = sb.indexOf(",", from+1);
            from = to; to = sb.indexOf("\n", from+1);
            
            double adjustedClose = Double.parseDouble(sb.substring(from+1,to));
			quotePlus[i] = new QuotePlus(symbol, yearDate, date, 
					open * adjustedClose/close, 
					high * adjustedClose/close,
					low * adjustedClose/close,
					close * adjustedClose/close,
					dateString);
			//System.err.println ("quote " + i + " " + close + " " + dateString);

		}

		double adjustedClose[] = QuotePlus.getCloses(quotePlus);
		double ma20[] = new double[quotePlus.length];
		double ma50[] = new double[quotePlus.length];
		double ma200[] = new double[quotePlus.length];
		MInteger outBegIdx = new MInteger();
	    MInteger outNbElement = new MInteger();
	    
	    RetCode retCode = lib.sma(0, adjustedClose.length-1, adjustedClose, 200, outBegIdx, outNbElement, ma200);
		for (int i=outBegIdx.value; retCode==RetCode.Success && i<outBegIdx.value+outNbElement.value; i++) {			
			quotePlus[i].setMA200(ma200[i-outBegIdx.value]);
		}	    
		retCode = lib.sma(0, adjustedClose.length-1, adjustedClose, 50, outBegIdx, outNbElement, ma50);
		for (int i=outBegIdx.value; retCode==RetCode.Success && i<outBegIdx.value+outNbElement.value; i++) {			
			quotePlus[i].setMA50(ma50[i-outBegIdx.value]);
		}
		retCode = lib.sma(0, adjustedClose.length-1, adjustedClose, 20, outBegIdx, outNbElement, ma20);
		for (int i=outBegIdx.value; retCode==RetCode.Success && i<outBegIdx.value+outNbElement.value; i++) {			
			quotePlus[i].setMA20(ma20[i-outBegIdx.value]);
		}
		if (quotePlus.length==0) {
			System.err.println(symbol + " " + yearDate + " " + "DataException");
			throw new HistoricalPriceServiceException (symbol, "DataException");
		}

		return quotePlus;
	}
}

