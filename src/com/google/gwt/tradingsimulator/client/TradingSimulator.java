
package com.google.gwt.tradingsimulator.client;

import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.canvas.client.*;
import com.google.gwt.canvas.dom.client.*;


public class TradingSimulator implements EntryPoint {
	/**
	 * Trading Simulator is the main class for the project.
	 * It handles all the price display formatting and animation.
	 * canvas1 contains the full plot of all the prices for a range and is hidden. 
	 * canvas2 displays the price animation that scrolls by the user at a speed determined by
	 * the refresh interval.
	 * Help screen for now are Window alerts.
	 */
	
	static final String canvasHolderId = "stockList";
	static final String unsupportedBrowser = "Your browser does not support the HTML5 Canvas";
	
	final CssColor colorBlack = CssColor.make("Black");
	final CssColor colorWhite = CssColor.make("White");
	final CssColor colorRed = CssColor.make("Red");
	final CssColor colorDarkRed = CssColor.make("Maroon");
	final CssColor colorGreen = CssColor.make("GreenYellow");
	final CssColor colorDarkGreen = CssColor.make("Green");
	final CssColor colorBlue = CssColor.make("Blue");
	final CssColor colorGray = CssColor.make("Gray");
	//final CssColor colorDarkGray = CssColor.make(51,51,51);
	final CssColor colorLightGray = CssColor.make(201,201,201);
	final CssColor colorPlum = CssColor.make("Plum");
	final CssColor colorLightSalmon = CssColor.make("LightSalmon");
	final CssColor colorTan = CssColor.make("Tan");
	
	Canvas canvas2;
	Context2d context2;
	Canvas canvas1;
	Context2d context1;
	
	private static int REFRESH_INTERVAL = 500; // milliseconds
	private FlowPanel mainPanel = new FlowPanel();
	private VerticalPanel displayPanel;
	private VerticalPanel chartPanel;
	private Grid centralGrid = new Grid(1,15);
	private Grid southGrid = new Grid(1,15);
	private TextBox symbolTextBox = new TextBox();
	private TextBox yearTextBox = new TextBox();
	private Label tradeProfitLossLabel = new Label("0.00");
	private Label totalProfitLossLabel = new Label("0.00");
	private Button buyButton = new Button("Buy");
	private Button sellButton = new Button("Short");
	private Button helpButton = new Button("Help");
	private Label barLabel = new Label("0");
	private Label dateLabel = new Label("2011-01-01");
	
	private CheckBox candleLineCheckBox = new CheckBox("Candle/Line", true);
	private CheckBox movingAverageCheckBox = new CheckBox ("20/50/200 MA", true);
	private CheckBox gridCheckBox = new CheckBox("Grid", true);
	private CheckBox blackWhiteCheckBox = new CheckBox("Black BG", true);
	private RadioButton lightingRadioButton = new RadioButton("speedGroup", "Fastest", true);
	private RadioButton fastRadioButton = new RadioButton("speedGroup", "Fast", true);
	private RadioButton mediumRadioButton = new RadioButton("speedGroup", "Medium", true);
	private RadioButton verySlowRadioButton = new RadioButton("speedGroup", "Slow", true);
	private Button stopButton = new Button("Stop");
	private Button resumeButton = new Button("Resume");
	private Button restartButton = new Button("Restart");
	private Button homeButton = new Button("Home");

	private Label priceLabel = new Label("0.00");
	
	private HistoricalPriceServiceAsync historicalPriceSvc = GWT.create(HistoricalPriceService.class);
	private QuotePlus[] quote;

	private int displayIndex=-1, buyIndex=-1, sellIndex=-1, maxYears=5;
	private int displayHeight=575, displayWidth=1000, chartWidth=0, chartHeight=0;
	private boolean longTrade=false, shortTrade=false;
	private double tradeProfitLoss=0, totalProfitLoss=0, minPrice=0, maxPrice=0;
	private String symbolName = "";
	private Integer year;

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
			
		int windowWidth = Window.getClientWidth(); //-Constant.WINDOW_BUFFER_WIDTH;
		int windowHeight = Window.getClientHeight(); //-Constant.WINDOW_BUFFER_HEIGHT;
		float centralGridHeight=0, southGridHeight=0;

		buyButton.setWidth("7EM");
		sellButton.setWidth("7EM");
		
		displayWidth=windowWidth;//-Constant.WINDOW_BUFFER_WIDTH; //Math.max(Constant.SCROLL_WIDTH,windowWidth);
		displayHeight=windowHeight-Constant.WINDOW_BUFFER_HEIGHT; //Math.max(Constant.SCROLL_HEIGHT,windowHeight);	
		centralGridHeight = Constant.GRID_HEIGHT;
		southGridHeight = Constant.GRID_HEIGHT;
		chartHeight=displayHeight*2;
		chartWidth=displayWidth;

		canvas1 = Canvas.createIfSupported();
		if (canvas1 == null) {
			RootPanel.get("Your browser does not support the HTML5 Canvas");
			return;
		}
		canvas2 = Canvas.createIfSupported();
		if (canvas2 == null) {
			RootPanel.get("Your browser does not support the HTML5 Canvas");
			return;
		}
		
		canvas1.setSize(chartWidth + "px", chartHeight + "px");
		canvas1.setCoordinateSpaceWidth(chartWidth);
		canvas1.setCoordinateSpaceHeight(chartHeight);
		context1 = canvas1.getContext2d();
		canvas2.setWidth((int)displayWidth + "px");
		canvas2.setHeight((int)displayHeight + "px");
		canvas2.setCoordinateSpaceWidth(displayWidth);
		canvas2.setCoordinateSpaceHeight(displayHeight);
		context2 = canvas2.getContext2d();

		displayPanel = new VerticalPanel();
		displayPanel.setHeight((int)displayHeight + "px");
		displayPanel.setWidth((int)displayWidth + "px");
		chartPanel =  new VerticalPanel();
		chartPanel.setWidth((int)displayWidth + "px");
		chartPanel.setHeight((int)displayHeight + "px");
		centralGrid.setHeight((int)centralGridHeight + "px");
		centralGrid.setWidth((int)displayWidth + "px");
		southGrid.setHeight((int)southGridHeight + "px");
		southGrid.setWidth((int)displayWidth + "px");
		
		centralGrid.setHTML(0,0,"Symbol:");
		symbolTextBox.setMaxLength(10);
		centralGrid.setWidget(0,1,symbolTextBox);
		centralGrid.setHTML(0,2,"Year:");
		yearTextBox.setMaxLength(8);
		Date date = new Date();
		year = new Integer(DateTimeFormat.getFormat("yyyy").format(date));
		year=year-1;
		yearTextBox.setText(year.toString());
		centralGrid.setWidget(0,3,yearTextBox);
		centralGrid.setHTML(0,4,"Trade Profit:");
		tradeProfitLossLabel.addStyleName("profitLossLabelBlack");
		tradeProfitLossLabel.addStyleName("profitLossLabelRed");
		tradeProfitLossLabel.addStyleName("profitLossLabelGreen");		//tradeProfitLossLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		tradeProfitLossLabel.setStyleName("profitLossLabelBlack");
		centralGrid.setWidget(0,5,tradeProfitLossLabel);
		centralGrid.setHTML(0,6,"Total Profit:");
		totalProfitLossLabel.addStyleName("profitLossLabelBlack");
		totalProfitLossLabel.addStyleName("profitLossLabelRed");
		totalProfitLossLabel.addStyleName("profitLossLabelGreen");
		totalProfitLossLabel.setStyleName("profitLossLabelBlack");
		centralGrid.setWidget(0,7,totalProfitLossLabel);
		centralGrid.setWidget(0,8,buyButton);
		centralGrid.setWidget(0,9,sellButton);
		centralGrid.setWidget(0,10,helpButton);
		centralGrid.setHTML(0,11,"Bars:");
		barLabel.addStyleName("barLabel");
		centralGrid.setWidget(0,12,barLabel);
		centralGrid.setHTML(0,13,"Date:");
		dateLabel.addStyleName("dateLabel");
		String dateString = DateTimeFormat.getFormat("yyyy-MM-dd").format(date);
		dateLabel.setText(dateString);
		centralGrid.setWidget(0,14,dateLabel);
			
		candleLineCheckBox.setValue(true);
		southGrid.setWidget(0,0,candleLineCheckBox);
		movingAverageCheckBox.setValue(true);
		southGrid.setWidget(0,1,movingAverageCheckBox);
		gridCheckBox.setValue(true);
		southGrid.setWidget(0,3,gridCheckBox);
		southGrid.setWidget(0,4,blackWhiteCheckBox);
		blackWhiteCheckBox.setValue(false);
		southGrid.setWidget(0,5,lightingRadioButton);
		fastRadioButton.setValue(true);
		southGrid.setWidget(0,6,fastRadioButton);
		southGrid.setWidget(0,7,mediumRadioButton);
		southGrid.setWidget(0,8,verySlowRadioButton);
		southGrid.setWidget(0,9,stopButton);
		southGrid.setWidget(0,10,resumeButton);
		southGrid.setWidget(0,11,restartButton);
		southGrid.setWidget(0,12,homeButton);
		southGrid.setHTML(0,13,"Price:");
		priceLabel.addStyleName("priceLabel");
		southGrid.setWidget(0,14,priceLabel);
		
		mainPanel.add(displayPanel);
		mainPanel.add(centralGrid);
		mainPanel.add(southGrid);
		mainPanel.add(canvas1);
		canvas1.setVisible(false);
	
		RootPanel.get(canvasHolderId).add(mainPanel);
		
		paintLogo(Constant.TRADING_SIMULATOR);
	
		symbolTextBox.setFocus(true);
		symbolTextBox.addKeyDownHandler(new KeyDownHandler(){@Override
		public void onKeyDown(KeyDownEvent event) {
			//System.err.println(event.getNativeKeyCode());
			switch(event.getNativeKeyCode()){ 
				case KeyCodes.KEY_ENTER:
				if(checkEntries()) {
					cancelTimer();
				    paintLogo(Constant.TRADING_SIMULATOR_LOADING);
					getName();
					getQuotes();
				}
			}		
		}});
		
		yearTextBox.addKeyDownHandler(new KeyDownHandler(){@Override
		public void onKeyDown(KeyDownEvent event) {
			switch(event.getNativeKeyCode()){ 
				case KeyCodes.KEY_ENTER:
				if(checkEntries()) { 
					cancelTimer();
				    paintLogo(Constant.TRADING_SIMULATOR_LOADING);
					getName();
					getQuotes();
				}

			}
		}});	
		
	    buyButton.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event) {
	        	if(checkEntries()) {
	        		cancelTimer();
	        		buy(displayIndex);
	        		paintBuySellLine(displayIndex);
	        		initializeTimer();
	        	}
	    }});
	    
	    sellButton.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event) {
	        	if(checkEntries()) {
	        		cancelTimer();
	        		sell(displayIndex);
	        		paintBuySellLine(displayIndex);
	        		initializeTimer();
	        }
	    }});
	    
	    helpButton.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event) {
   		
	        	if (Window.confirm(
"                The Trading Simulator, (c) 2011, by Eric Ross\n\n" +
"To start the Trading Simulator, enter a stock ticker in the Symbol text box and press <ENTER>.  " + 
"The simulation only starts when <ENTER> is pressed in either the Symbol or Year text box.  " +
"Here are tickers for the 25 most active stocks as of April 1, 2011:\n\n" +
"C - Citigroup, Inc.\nBAC - Bank of America Corporation\nSPY - SPDR S&P 500\nF - Ford Motor Co.\nXLF - Financial Select Sector SPDR\nCSCO - Cisco Systems, Inc.\nEEM - iShares MSCI Emerging Markets Index\nQQQQ - PowerShares QQQ Trust, Series 1\nS - Sprint Nextel Corp.\nGE - General Electric Co.\nMSFT - Microsoft Corporation\nINTC - Intel Corporation\nIWM - iShares Russell 2000 Index\nSIRI - SIRIUS XM Radio Inc.\nPFE - Pfizer Inc. - Healthcare\nEWJ - iShares MSCI Japan Index\nMU - Micron Technology Inc.\nALU - Alcatel-Lucent\nJPM - JPMorgan Chase & Co.\nNVDA - NVIDIA Corporation\nWFC - Wells Fargo & Company\nT - AT&T, Inc. - Technology\nAA - Alcoa, Inc.\nNOK - Nokia Corporation\nSLV - iShares Silver Trust\n\n" + //\nSDS - ProShares UltraShort S&P500\nORCL - Oracle Corp.\nAMD - Advanced Micro Devices, Inc.\nLVS - Las Vegas Sands Corp.\nFAS - Direxion Daily Financial Bull 3X Shares\nMGM - MGM Resorts International\nEMC - EMC Corporation\nYHOO - Yahoo! Inc.\nVWO - Vanguard MSCI Emerging Markets ETF\nDELL - Dell Inc. - Technology\nXOM - Exxon Mobil Corp.\nLVLT - Level 3 Communications Inc.\nVALE - Vale S.A.\nVXX - iPath S&P 500 VIX Short-Term Futures ETN\nFCX - Freeport-McMoRan Copper & Gold Inc.\nVZ - Verizon Communications Inc.\nHPQ - Hewlett-Packard Company\nPBR - Petroleo Brasileiro\nMRK - Merck & Co. Inc.\nHBAN - Huntington Bancshares Inc.\nAMAT - Applied Materials, Inc.\nEFA - iShares MSCI EAFE Index\nAAPL - Apple Inc.\nDAL - Delta Air Lines Inc.\nRF - Regions Financial Corp.\n\n" +
"Click 'OK' for more help, or 'Cancel' to quit."
	        			))  	
	        	if (Window.confirm(
"                The Trading Simulator, (c) 2011, by Eric Ross\n\n" +
"The Trading Simulator replays historical stock prices one day at a time.  " + 
"The default chart is a candlestick chart.  " +
"A green candlestick indicates the close is higher than the open, a red the close is lower than the open.  " +
"If you prefer a line chart (blue) based on closing price, uncheck the Candle check box.\n\n" +

"Stock symbols, names and data are taken from Yahoo Finance.  " +
"Past prices are based on the Yahoo Finance historical adjusted close.  " +
"The system by default pulls data from January 1, " + year.toString() + " to today's date.  " +
"Yahoo Finance names are limited to 17 characters.\n\n" +

"Run from dates can be entered in the year box. Valid formats are 'yyyy', 'yyyymm', or 'yyyymmdd'.  " +
"When just the year is entered, up to 4+ years of data are pulled.  " +
"The date range will be from January 1, of the year entered to the current day 4 years later.\n\n" +
"If year, month and day are entered, exactly 5 years of data will be pulled starting from the date entered and ending on the same date 5 years later.\n\n" +

"The current date and price are given in bold type in the lower right corner.  " +
"This price is the one on which your trades will be based.\n\n" +

"Click the Buy or Sell buttons to simulate trades.  " + 
"Trade and total gains or losses are recorded.  " +
"Your last trade is shown with on the chart with a buy point (dark green line) and sell point (dark red).  " +
"Open trades are closed when the end of data is reached.\n\n" +

"Click 'OK' for more help, or 'Cancel' to quit."
))	        	
Window.alert(
"                The Trading Simulator, (c) 2011, by Eric Ross\n\n" +

"Moving averages are the simple 20 day (pink), 50 day (orange), and 200 day (peach) MA's.\n\n" +
"Various replay speeds can be selected.  " +
"Fastest runs at  1/50th of a second intervals, fast runs at half second intervals, medium at one second intervals, and slow at five second intervals.  " +
"Actual speeds are browser dependent.\n\n" +
		
"You can stop and resume the replay action.  " +
"You can also restart the replay action from the beginning without reloading the price data.  " +
"You may choose either a black or white background.\n\n" +


"The price grid is plotted in multiples of $5 or $10, depending on the price range. " +
"The date grid is drawn at monthly intervals.  The grid is labeled with the price and date at the start of each month.\n\n" +

"Caution: If the end of the data range is before the date the stock was issued, you'll receive a 'no data back that far' error.\n\n" +
"For more on stock charts in general, see www.stockcharts.com.  "
); 
	        }}); 
	    
	    blackWhiteCheckBox.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event) {
	        }
	    });
	    
	    gridCheckBox.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event) { 
	        }
	    });
	    
	    stopButton.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event) {
	        	cancelTimer();      }
	    });
	    
	    resumeButton.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event) {
	        	if(checkEntries() && quote != null) {
	        		cancelTimer();
	        		initializeTimer();
	        	}
	        }
	    });
	    
	    restartButton.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event) {
	        	if(checkEntries()) {
					cancelTimer();
					paintLogo(Constant.TRADING_SIMULATOR_LOADING);
					if (symbolName.equals(""))
						getName();
					if (quote == null)
						getQuotes();
	        		else { 
	        			initializeRun();
	        			paintGrid();
	        			paintPrice();
	        			initializeTimer();	
	        		}
				}

	        }
	    });
	    
	    homeButton.addClickHandler(new ClickHandler() {
	        public void onClick(ClickEvent event) {
	        	symbolTextBox.setText("");
	        	paintLogo(Constant.TRADING_SIMULATOR);
	        }
	    });

	    ClickHandler radioButtonsHandler = new ClickHandler() {
	    	@Override
	    	public void onClick(ClickEvent event) {
	    		cancelTimer();
	    		initializeTimer();
	    	}
	    };	    
	    lightingRadioButton.addClickHandler(radioButtonsHandler);
	    fastRadioButton.addClickHandler(radioButtonsHandler);
	    mediumRadioButton.addClickHandler(radioButtonsHandler);
	    verySlowRadioButton.addClickHandler(radioButtonsHandler);
	}
	
	// Edit check for valid symbol and date.
	
	private boolean checkEntries() {
		
		if (symbolTextBox.getText().equals("")) {
		      Window.alert("Enter a symbol and press <ENTER>.");
		      return false;
		}
		else 
		if (!symbolTextBox.getText().matches("^[0-9a-zA-Z\\^\\.]{1,10}$")) {
		      Window.alert("'" + symbolTextBox.getText() + "' is not a valid symbol.");
		      return false;
		}
	    if (!(yearTextBox.getText().matches("^[0-9]{8}$") ||
	    	yearTextBox.getText().matches("^[0-9]{6}$") ||
	    	yearTextBox.getText().matches("^[0-9]{4}$"))) {
		      Window.alert("'" + yearTextBox.getText() + "' is not a valid date.  Must be in YYYYMMDD format.");
		      return false;
		}
	    
		return true;		
	}
	
	// Initialize run after the quotes are returned.
	
	public void initializeRun() {

		if (quote == null) return;

		maxPrice = Quote.getMax(quote);
	    minPrice = Quote.getMin(quote);

		displayIndex=-1;
		buyIndex=-1;
		sellIndex=-1;
		tradeProfitLoss=0;
		totalProfitLoss=0;
		tradeProfitLossLabel.setStyleName("profitLossLabelBlack");
		totalProfitLossLabel.setStyleName("profitLossLabelBlack");
		tradeProfitLossLabel.setText("0.00");
		totalProfitLossLabel.setText("0.00");
		priceLabel.setText("0.00");
		longTrade=false;
		shortTrade=false;
		buyButton.setText("Buy");
		sellButton.setText("Short");

		chartHeight=displayHeight*2;
		chartWidth=quote.length*Constant.CANDLE_SPACING;
		canvas1.setSize(chartWidth + "px", chartHeight + "px");
		canvas1.setCoordinateSpaceWidth(chartWidth);
		canvas1.setCoordinateSpaceHeight(chartHeight);
		context1.setFillStyle(setBackgroundColor());
		context1.fillRect(0, 0, chartWidth, chartHeight);
		
		displayPanel.clear();
		displayPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		displayPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		context2.setFillStyle(colorWhite);
		context2.fillRect(0, 0, displayWidth, displayHeight);
		displayPanel.add(canvas2);
	}		

	// Updates the labels for price and date, profit or loss in the grid panels outside of the
	// canvases.
	
	public void updateLabels(int index) {

		assert(quote != null);
		assert(index>-1);
		assert(index<quote.length);

		String barString = NumberFormat.getFormat("0").format(index+1) + "/"+ NumberFormat.getFormat("0").format(quote.length);
		barLabel.setText(barString);
		
		String dateString = quote[index].getDateString();
		dateLabel.setText(dateString);
		
		String priceString = NumberFormat.getFormat("##,##0.00").format(quote[index].getClose());
		priceLabel.setText(priceString);

    	if (shortTrade && sellIndex > -1) {
    		tradeProfitLoss = quote[sellIndex].getClose() - quote[index].getClose();
    		//System.err.println("shortT " + tradeProfitLoss + " " + quote[sellIndex].getClose() + " " + quote[index].getClose() + " " + index + " " + buyIndex + " " + sellIndex);
    	}
    	else
    	if (longTrade && buyIndex > -1) {
    		tradeProfitLoss = (quote[index].getClose() - quote[buyIndex].getClose());
    		//System.err.println("longT " + tradeProfitLoss + " " + quote[buyIndex].getClose() + " " + quote[index].getClose() + " " + index + " " + buyIndex + " " + sellIndex);
    	}
    	else
    		tradeProfitLoss = 0;
		if (tradeProfitLoss==0)
			tradeProfitLossLabel.setStyleName("profitLossLabelBlack");
		else if (tradeProfitLoss<0)
			tradeProfitLossLabel.setStyleName("profitLossLabelRed");
		else
			tradeProfitLossLabel.setStyleName("profitLossLabelGreen");
		String tradeProfitLossString = NumberFormat.getFormat("##,##0.00").format(tradeProfitLoss);
		tradeProfitLossLabel.setText(tradeProfitLossString);
		
	}
	
	// paint functions follow
	
	public void paintCandleStick(int i) {
		
		assert(quote != null);
		assert(i>-1);
		assert(i<quote.length);

		quote[i].setQuotePlus(maxPrice, minPrice, chartHeight);
		
		Rectangle candle = new Rectangle();
		candle.x = i*Constant.CANDLE_SPACING;
		candle.width=Constant.CANDLE_WIDTH;
		candle.y = quote[i].getCandleY()-chartHeight/4;
		candle.height=quote[i].getCandleHeight();

		Rectangle stick = new Rectangle();
		stick.x =i*Constant.CANDLE_SPACING+Constant.STICK_OFFSET;
		stick.width=Constant.STICK_WIDTH;
		stick.y=quote[i].getStickY()-chartHeight/4;
		stick.height=quote[i].getStickHeight();
		
		context1.moveTo(stick.x, stick.y);
		context1.beginPath();
		if (quote[i].up()) {
			context1.setStrokeStyle(colorDarkGreen); // dark green
			context1.setFillStyle(colorGreen);
		} else {
			context1.setStrokeStyle(colorDarkRed); // dark red
			context1.setFillStyle(colorRed);
		}
		context1.strokeRect(stick.x,stick.y, stick.width,stick.height);
		context1.fillRect(candle.x,candle.y, candle.width,candle.height);
		context1.strokeRect(candle.x,candle.y, candle.width,candle.height);
		context1.closePath();

	}
	
	public void paintLine(int i) {
		
		assert(quote != null);
		assert(i>-1);
		assert(i<quote.length);
		

		if (i==0) return;
		int h = i-1;

		Rectangle candle = new Rectangle();
		candle.x = h*Constant.CANDLE_SPACING;
		candle.y = QuotePlus.pixY(quote[h].getClose(), maxPrice, minPrice, chartHeight)-chartHeight/4;
		candle.width=i*Constant.CANDLE_SPACING;
		candle.height=QuotePlus.pixY(quote[i].getClose(), maxPrice, minPrice, chartHeight)-chartHeight/4;
		context1.beginPath();
		context1.setLineWidth(2);
		context1.setStrokeStyle(colorBlue);
		context1.moveTo(candle.x, candle.y);
		context1.lineTo(candle.width, candle.height);
		context1.stroke();
		context1.closePath();
	}
	
	public void paintPrice() {
		
		if (movingAverageCheckBox.getValue())
			for (int i = 0; i<quote.length; i++) {
				for (int pass=0; pass<3; pass++) {
					paintMovingAverages(pass, i);
				}
			}
		for (int i = 0; i<quote.length; i++) {
			if (candleLineCheckBox.getValue())
				paintCandleStick(i);
			else
				paintLine(i);
		}
	}
	
	public void paintMovingAverages(int pass, int i) {

		assert(quote != null);
		assert(i>-1);
		assert(i<quote.length);

		if (i==0) return;
		int h = i-1;
		CssColor color;
		//Color PINK = new Color(255,182,193);
		double ma0=0, ma1=0;
		if (pass==0) {
			if (quote[i].getMA200() == 0 || quote[h].getMA200() == 0) return;
			ma0 = QuotePlus.pixY(quote[h].getMA200(), maxPrice, minPrice, chartHeight)-chartHeight/4;
			ma1 = QuotePlus.pixY(quote[i].getMA200(), maxPrice, minPrice, chartHeight)-chartHeight/4;
			color = colorTan;
		} else if (pass==1) {
			if (quote[i].getMA50() == 0 || quote[h].getMA50() == 0) return;
			ma0 = QuotePlus.pixY(quote[h].getMA50(), maxPrice, minPrice, chartHeight)-chartHeight/4;
			ma1 = QuotePlus.pixY(quote[i].getMA50(), maxPrice, minPrice, chartHeight)-chartHeight/4;
			color = colorLightSalmon;
		} else if (pass==2) {
			if (quote[i].getMA20() == 0 || quote[h].getMA20() == 0) return;
			ma0 = QuotePlus.pixY(quote[h].getMA20(), maxPrice, minPrice, chartHeight)-chartHeight/4;
			ma1 = QuotePlus.pixY(quote[i].getMA20(), maxPrice, minPrice, chartHeight)-chartHeight/4;
			color = colorPlum;
		} else
			color = colorGray;

		Rectangle candle = new Rectangle();
		candle.x = h*Constant.CANDLE_SPACING;
		candle.y = ma0;
		candle.width=i*Constant.CANDLE_SPACING;
		candle.height=ma1;
		context1.beginPath();
		context1.setLineWidth(2);
		context1.setStrokeStyle(color);
		context1.moveTo(candle.x, candle.y);
		context1.lineTo(candle.width, candle.height);
		context1.stroke();
		context1.closePath();
		
	}
	
	public CssColor setGridColor() { 
		if (blackWhiteCheckBox.getValue()==true)
			return colorLightGray;
		else
			return colorGray;
	}
	public CssColor setBackgroundColor() { 
		if (blackWhiteCheckBox.getValue()==true)
			return colorBlack;
		else
			return colorWhite;
	}
	
	public void paintDateGrid() {
		
		assert(quote != null);

		context1.moveTo(0, 0);
		context1.beginPath();
		context1.setFillStyle(setGridColor());
		
		String yesterdayString = "";
		for (int i=0; i<quote.length; i++) {
			String todayString = quote[i].getDateString();
			if (!yesterdayString.startsWith(todayString.substring(0,7))) { 
				Rectangle monthGrid = new Rectangle(i * Constant.CANDLE_SPACING, 0, Constant.LINE_WIDTH, chartHeight);
				context1.fillRect((int)monthGrid.x,(int)monthGrid.y, monthGrid.width,(int)monthGrid.height);	
			}
			yesterdayString = todayString;
		}
		context1.closePath();
		return;
	}
	
	public void paintGrid() {
		if (gridCheckBox.getValue() == true) {
			paintPriceGrid();
			paintDateGrid();
			paintGridText();
		}
	}
	
	public void paintPriceGrid() {
		
		assert(quote != null);
				
		double range = (maxPrice-minPrice);
		double middle = (maxPrice+minPrice)/2;	
		double increment=Math.pow(10,(int)Math.log10(range));
		double maxChartPrice = ((int)((middle+range)/increment))*increment+increment;
		double minChartPrice = ((int)((middle-range)/increment))*increment-increment;
		if ((maxChartPrice - minChartPrice)/increment < 10)
			increment = increment/2;
		
		//System.err.println("pg i mxCp mnCp mx mn m r " + increment + " " + maxChartPrice + " " +  minChartPrice + " " + maxPrice + " " + minPrice + " " + middle + " " + range + " " + (int)((middle-range)/increment));

		context1.beginPath();
		context1.moveTo(0, 0);
		context1.setFillStyle(setGridColor());

		for (double price=minChartPrice; price <= maxChartPrice; price+=increment) {
			double pixY = QuotePlus.pixY(price, maxPrice, minPrice, chartHeight)-chartHeight/4;
			context1.fillRect(0, pixY, chartWidth, 1);			
		}
		
		context1.stroke();
		context1.closePath();	
	}

	
	public void paintGridText() {

		assert(quote != null);

		boolean pricePass=true, datePass=true;
		
		double range = (maxPrice-minPrice);
		double middle = (maxPrice+minPrice)/2;	
		double increment=Math.pow(10,(int)Math.log10(range));
		double maxChartPrice = ((int)((middle+range)/increment))*increment+increment;
		double minChartPrice = ((int)((middle-range)/increment))*increment-increment;
		if ((maxChartPrice - minChartPrice)/increment < 10)
			increment = increment/2;

		//System.err.println("pg i mxCp mnCp mx mn m r " + increment + " " + maxChartPrice + " " +  minChartPrice + " " + maxPrice + " " + minPrice + " " + middle + " " + range + " " + (int)((middle-range)/increment));
		
		for (double price=minChartPrice; price <= maxChartPrice; price+=increment) {
			
			double pixY = QuotePlus.pixY(price, maxPrice, minPrice, chartHeight)-chartHeight/4;
			//System.err.println("price grid " + price + " " + pixY);
			//Rectangle priceGrid = new Rectangle(0,pixY,quote.length*Constant.CANDLE_SPACING,Constant.LINE_WIDTH);
			String priceText = "";
			if (price < 5)
				priceText = NumberFormat.getFormat("0.00").format(price);
			else
				priceText = NumberFormat.getFormat("0").format(price);
				
			context1.beginPath();
			context1.setStrokeStyle(setGridColor());
			context1.setFont("normal 10px Arial");
			String yesterdayString = "";
			for (int i=0; i<quote.length; i++) {
				String todayString = quote[i].getDateString();
				if (!yesterdayString.startsWith(todayString.substring(0,7)) 
					// && todayString.substring(5,7).matches("01|04|07|10") || yesterdayString.equals("")
						) { 
					double pixX = i * Constant.CANDLE_SPACING;				
					if (pricePass) {
						context1.setFillStyle(setGridColor());
						context1.setFont("normal 10px Arial");
						context1.strokeText(priceText, pixX, pixY);
					}
					if (datePass) {
						String dateText = todayString.substring(5);
						context1.strokeText(dateText , pixX, pixY+10);
					}
				}
				yesterdayString=todayString;
			}
			context1.closePath();
		}
	}
	
	public void paintBuySellLine(int index) {

		if (quote == null) return;
		if (index==-1) return;
		assert(index>-1);
		assert(index<quote.length);
		assert(buyIndex < quote.length);
		assert(sellIndex< quote.length);
		//System.err.println("i bi si" + index + " " + buyIndex + " " + sellIndex);

		if (buyIndex > -1) {
			Rectangle buy = new Rectangle();
			buy.x = buyIndex*Constant.CANDLE_SPACING;
			buy.y = QuotePlus.pixY(quote[buyIndex].getClose(), maxPrice, minPrice, chartHeight)-chartHeight/4;
			buy.width=(index-buyIndex+1)*Constant.CANDLE_SPACING;
			if (buyIndex == index)
				buy.height = 5;
			else
				buy.height = 1;
			context1.beginPath();
			context1.setFillStyle(colorDarkGreen); // dark green
			context1.fillRect(buy.x,buy.y, buy.width,buy.height);
			context1.fill();
			context1.closePath();
			if (longTrade == false && shortTrade == false)
				buyIndex = -1;
			
		}
		if (sellIndex > -1) {
			Rectangle sell = new Rectangle();
			sell.x = sellIndex*Constant.CANDLE_SPACING;
			sell.y = QuotePlus.pixY(quote[sellIndex].getClose(), maxPrice, minPrice, chartHeight)-chartHeight/4;
			sell.width=(index-sellIndex+1)*Constant.CANDLE_SPACING;
			if (sellIndex == index)
				sell.height = 5;
			else
				sell.height = 1;
			context1.beginPath();
			context1.setFillStyle(colorDarkRed); // dark red
			context1.fillRect(sell.x,sell.y, sell.width,sell.height);
			context1.fill();
			context1.closePath();
			if (longTrade == false && shortTrade == false)
				sellIndex = -1;			
		}
	
	}
	
	private void paintLogo(int urlIndex) {
		
		String[] urls = new String[] {"/images/TradingSimulator.png","/images/TradingSimulatorLoading.png"};//,"http://localhost:8888/images/BlankLoading.png","http://localhost:8888/images/Blank.png"};
		Image image = new Image();
		image.setUrl(urls[urlIndex]);
		
		if (urlIndex == Constant.TRADING_SIMULATOR) {
			displayPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			displayPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);	
			displayPanel.clear();
			displayPanel.add(image);
		}
		if (urlIndex == Constant.TRADING_SIMULATOR_LOADING) {
			displayPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			displayPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);	
			displayPanel.clear();
			displayPanel.add(image);
		}	
		return;
	}
	
	
	// Async data retrieval functions for name and quotes follow.
	
	private void getName() {
	    
		if (historicalPriceSvc == null) {
			historicalPriceSvc = GWT.create(HistoricalPriceService.class);
		}
		// Set up the callback object.
		AsyncCallback<String> callbackString = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
		        //symbolName = "";
			}
			public void onSuccess(String result) {
				symbolName = result;
			}
		};
		symbolName = "";
		// Make the call to the historical price service.
		historicalPriceSvc.getName(symbolTextBox.getText(), callbackString);
		
	}
	
	private void getQuotes() {
    
		if (historicalPriceSvc == null) {
			historicalPriceSvc = GWT.create(HistoricalPriceService.class);
		}
		// Set up the callback object.
		AsyncCallback<QuotePlus[]> callback = new AsyncCallback<QuotePlus[]>() {
			public void onFailure(Throwable caught) {
				String symbol = "";
				String message = "";
		        if (caught instanceof HistoricalPriceServiceException) {
		          symbol = ((HistoricalPriceServiceException)caught).getSymbol();
		          message = ((HistoricalPriceServiceException)caught).getMessage();
		        }
				quote = null;
				displayIndex = -1;
				checkQuotes(symbol, message);
			}

			public void onSuccess(QuotePlus[] result) {
				if (result==null || result.length==0)
					quote = null;
				else
					quote = result;
				displayIndex = -1;
				if (checkQuotes()) {
					cancelTimer();
					initializeRun();
					paintGrid();	
					paintPrice();
					initializeTimer();
				}
			}
		};
		quote = null;
		displayIndex = -1;
		// Make the call to the historical price service.
		historicalPriceSvc.getQuotesPlus(symbolTextBox.getText(),yearTextBox.getText(), maxYears, callback);
		
	}
	
	// These check functions are called by the async getQuotes function.
	
	public boolean checkQuotes() {

		if (quote == null) {
			paintLogo(Constant.TRADING_SIMULATOR);
			Window.alert(
				"'" + symbolTextBox.getText() + "' is not found.  " + 
				"The back end didn't read the data. Try pressing <ENTER> again.");
  		    return false;
		}
		return true;
	}

	public boolean checkQuotes(String symbol, String message) {
	
		if (quote == null) {
			paintLogo(Constant.TRADING_SIMULATOR);
			if (message.equals("DataException"))
				Window.alert(
				"'" + symbol + "' is not found.  " +
				"Either the symbol is invalid or the data does not go that far back.");
			else 
			if (message.contains("Timeout"))
				Window.alert(
				"'" + symbol + "' is not found.  " +
				"The back end timed out trying to read the data. Try pressing <ENTER> again.");
			else 
				Window.alert(
				"'" + symbol + "' is not found.  " +
				"Either the symbol is invalid, the data does not go that far back, or the " +
				"back end didn't read the data. If the latter, try pressing <ENTER> again. (" + message + ")\n\n");
			    return false;
		}
		return true;
	}

	// Buy and sell functions follow
	
	public void buy(int index) {

		if (quote == null) return;
		if (index==-1) return;
		assert(index<quote.length);
		assert(buyIndex < quote.length);
		assert(sellIndex< quote.length);
		
    	if (shortTrade) {
    		buyIndex = index;
    		if (sellIndex > -1) {
    			tradeProfitLoss = (quote[sellIndex].getClose() - quote[buyIndex].getClose());
     		} else { 
    			tradeProfitLoss = 0;
    		}
    		totalProfitLoss+=tradeProfitLoss;
    		if (tradeProfitLoss==0)
    			tradeProfitLossLabel.setStyleName("profitLossLabelBlack");
    		else if (tradeProfitLoss<0)
    			tradeProfitLossLabel.setStyleName("profitLossLabelRed");
    		else
    			tradeProfitLossLabel.setStyleName("profitLossLabelGreen");
    		String tradeProfitLossString = NumberFormat.getFormat("##,##0.00").format(tradeProfitLoss);
    		tradeProfitLossLabel.setText(tradeProfitLossString);
    		if (totalProfitLoss==0)
    			totalProfitLossLabel.setStyleName("profitLossLabelBlack");
    		else if (totalProfitLoss<0)
    			totalProfitLossLabel.setStyleName("profitLossLabelRed");
    		else
    			totalProfitLossLabel.setStyleName("profitLossLabelGreen");
    		String totalProfitLossString = NumberFormat.getFormat("##,##0.00").format(totalProfitLoss);
    		totalProfitLossLabel.setText(totalProfitLossString);
    		shortTrade = false;
    		buyButton.setText("Buy");
    		sellButton.setText("Short");
    		//System.err.println("buy " + tradeProfitLoss + " " + quote[buyIndex].getClose() + " " + quote[sellIndex].getClose() + " " + index + " " + buyIndex + " " + sellIndex);
    	} else if (!longTrade) {
    		tradeProfitLoss=0;
			tradeProfitLossLabel.setStyleName("profitLossLabelBlack");
    		String tradeProfitLossString = NumberFormat.getFormat("##,##0.00").format(tradeProfitLoss);
    		tradeProfitLossLabel.setText(tradeProfitLossString);
    		buyIndex = index;
    		sellIndex = -1;
    		longTrade = true;
    		buyButton.setText("");
    		sellButton.setText("Sell");
    		//System.err.println("buy " + tradeProfitLoss + " " + quote[buyIndex].getClose() + " " + " " + index + " " + buyIndex + " " + sellIndex);
    	}

    	
	}
	
	public void sell(int index) {

		assert(quote != null);
		if (index==-1) return;
		assert(index<quote.length);
		assert(buyIndex < quote.length);
		assert(sellIndex< quote.length);
		
		if (longTrade) {
    		sellIndex = index;
    		if (buyIndex > -1) {
    			tradeProfitLoss = (quote[sellIndex].getClose() - quote[buyIndex].getClose());
    		} else { 
    			tradeProfitLoss = 0;
    		}
    		totalProfitLoss+=tradeProfitLoss;
    		if (tradeProfitLoss==0)
    			tradeProfitLossLabel.setStyleName("profitLossLabelBlack");
    		else if (tradeProfitLoss<0)
    			tradeProfitLossLabel.setStyleName("profitLossLabelRed");
    		else
    			tradeProfitLossLabel.setStyleName("profitLossLabelGreen");
    		String tradeProfitLossString = NumberFormat.getFormat("##,##0.00").format(tradeProfitLoss);
    		tradeProfitLossLabel.setText(tradeProfitLossString);
    		if (totalProfitLoss==0) 
    			totalProfitLossLabel.setStyleName("profitLossLabelBlack");
    		else if (totalProfitLoss<0)
    			totalProfitLossLabel.setStyleName("profitLossLabelRed");
    		else
    			totalProfitLossLabel.setStyleName("profitLossLabelGreen");
   		    String totalProfitLossString = NumberFormat.getFormat("##,##0.00").format(totalProfitLoss);
    		totalProfitLossLabel.setText(totalProfitLossString);
     		longTrade = false;
    		buyButton.setText("Buy");
    		sellButton.setText("Short");
    		//System.err.println("sell " + tradeProfitLoss + " " + quote[buyIndex].getClose() + " " + quote[sellIndex].getClose() + " " + index + " " + buyIndex + " " + sellIndex);
    	} else if (!shortTrade) {
	    	tradeProfitLoss=0;
			tradeProfitLossLabel.setStyleName("profitLossLabelBlack");
    		String tradeProfitLossString = NumberFormat.getFormat("##,##0.00").format(tradeProfitLoss);
    		tradeProfitLossLabel.setText(tradeProfitLossString);
	    	buyIndex = -1;
	    	sellIndex = index;
	    	shortTrade=true;
    		buyButton.setText("Cover");
    		sellButton.setText("");
    		//System.err.println("short " + tradeProfitLoss + " " + " " + quote[sellIndex].getClose() + " " + index + " " + buyIndex + " " + sellIndex);
    	}
	}
	
	// Timer functions follow
	
	private final Timer refreshTimer = new Timer() {
		@Override
		public void run() {
			if (quote == null) return;
			// incremented in front so displayIndex is in sync with buyIndex and sellIndex
			// Allow an extra iteration, if the animation has stopped, and the user wants to close a trade.
			boolean cancel = false;
			displayIndex++;
			if (displayIndex >= quote.length) {
				displayIndex = quote.length-1;
				cancel=true;
			}			
			paintBuySellLine(displayIndex);
			context2.setFillStyle(colorWhite);
			context2.fillRect(0,0,displayWidth,displayHeight);
			double pixX = displayWidth-(displayIndex*Constant.CANDLE_SPACING+Constant.CANDLE_GAP);
			double pixY = QuotePlus.pixY(quote[displayIndex].getClose(), maxPrice, minPrice, chartHeight) - displayHeight;
			String nameText = symbolTextBox.getText().toUpperCase();
			if (!symbolName.equals("")) {
				nameText = nameText + " - " + symbolName;
			}
			context2.setFillStyle(setGridColor());
			context2.setFont("normal 20px Arial");
			context2.fillText(nameText, pixX-displayWidth*2/3, displayHeight/2, displayWidth*1/3);
		
			//if (index%10 == 0) System.err.println("i pY pYd dH c mp mp ch " + index + " " + pixY + " " + pixYd + " " + displayHeight + " " + quote[index].getClose() + " " + maxPrice + " " + minPrice + " " + chartHeight + " " + quote[index].getMA20()); 
			context2.drawImage(canvas1.getCanvasElement(), 0, pixY, chartWidth, displayHeight, pixX, 0, chartWidth, displayHeight);
			updateLabels(displayIndex);
			if (cancel)
				this.cancel();
		}
	};

	public void initializeTimer() {
		// Setup timer to refresh list automatically.
		if (quote == null) return;
		if (lightingRadioButton.getValue())
			REFRESH_INTERVAL=50;
		else
		if (fastRadioButton.getValue())
			REFRESH_INTERVAL=500;
		else
		if (mediumRadioButton.getValue())
			REFRESH_INTERVAL=1000;
		else
		if (verySlowRadioButton.getValue())
			REFRESH_INTERVAL=5000;
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
	}
	
	public void cancelTimer() {
		refreshTimer.cancel();
	}
}
