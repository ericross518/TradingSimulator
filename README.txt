The Trading Simulator allows you to replay the Price Chart of a stock one bar at a time and simulate buys and sells.  This permits learning about the price action of a stock dynamically, versus looking at a traditional static chart.

The Trading Simulator is located at https://thetradingsimulator.appspot.com.

The GitHub source is at: https://github.com/ericross518/TradingSimulator/blob/master/

The main source file for the file is: src/com/google/gwt/tradingsimulator/client/TradingSimulator.java.

The source file that pulls data from Yahoo Finance is: src/com/google/gwt/tradingsimulator/server/HistoricalPriceServerImpl.java.

The application was developed using Google Web Toolkit.  Technical indicators are based on code in http://sourceforge.net/p/ta-lib, modified to work with GWT.

One user of the simulator said, "There is so much more you can do with it".  

My intent in 2011 was to develop a simple application to demonstrate programming skills.  I have not worked on it since then, other that to update it to keep it compatible with more recent versions of GWT. Among ideas for improvement that come to mind are:

1.  Put the help in HTML pages instead of a message box.

2.  Add other Technical indicators, besides the 20/50/200 day moving average.

3.  Provide automated buy and sell signals.  A starting point might be using the rules in http://stockcharts.com/public "Above the Greenline".

4.  Add automated trend lines like the ones shown on symbols in http://finviz.com.