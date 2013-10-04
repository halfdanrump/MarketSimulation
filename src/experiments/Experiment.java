package experiments;

import java.util.ArrayList;


import setup.Logging;
import utilities.StockMarketPair;

import log.AgentLogger;
import log.Logger;
import log.OrderbookLogger;
import log.StockLogger;
import log.WorldLogger;
import agent.HFT;
import environment.Market;
import environment.Order;
import environment.Orderbook;
import environment.Stock;
import environment.World;

public abstract class Experiment{
	private static ArrayList<Logger> openLogs = new ArrayList<Logger>();
	/*
	 * General setup
	 */
	public final int nTotalRounds = Integer.valueOf(System.getProperty("nRounds", "30000"));
	public final int nInitialSlowTraderRounds = 5000;
	public final int nHFTRounds = nTotalRounds - nInitialSlowTraderRounds-1; 
	
	public final boolean constantNumberOfSlowtraderOrdersPerRound = false;
	public long nSlowTraderOrdersPerRound = 1;	
	public final double slowTraderOrdersPerRoundAverage = 1;
	
	
	/*
	 * Orderbook settings
	 */
	public final int ob_nStartOrders = 100000;
	public final int ob_initialOrderStd = 100;
	public final int ob_initialOrderVolumeMean = 10;
	public final int ob_initialOrderVolumeStd = 10;
	public final int ob_startingSpread = 0;
	public final int ob_orderExpirationTime = 1000000;
	
	/*
	 * Market rules
	 */
	public final boolean allowsShortSelling = true;
	public final boolean agentPaysWhenOrderIsFilledAfterSendingCancellation = true;
	public final boolean agentMustBuyAllStocksAsSpecifiedInReceipt = true;
	public final boolean agentMustSellAllStocksAsSpecifiedInReceipt = true;
	
	public final boolean marketFillsEmptyBook = false;
	public final long orderVolumeWhenMarketFillsEmptyBook = 1;
	public final Order.Type orderTypeWhenMarketFillsEmptyBook = Order.Type.MARKET;
	public final int orderLengthWhenMarketFillsEmptyBook = 5;
	
	/*
	 * High frequency trader behavior
	 */
	
	public final boolean hft_keepOrderHistory = false;
	
	public final long hft_emptyOrderbookWaitTime = 0;
	public final boolean hft_randomStartStockAmount = false;
	public final long hft_startStockAmount = 10000000;
	
	public final boolean hft_randomStartWealth = false;
	public final long hft_constantStartWealth = (int) Math.pow(10, 3);
	
	public final long hft_wealthMean = (int) Math.pow(10, 6);
	public final long hft_wealthStd = (int) Math.pow(10, 4);
	
//	public final int hft_minimumThinkingTime = 1;
//	public final int hft_maximumThinkingTime = 100;
	
//	public final int hft_minimumLatency = 1;
//	public final int hft_maximumLatency = 100;
	
	/*
	 * Single stock minimum spread market maker parameters 
	 */

	
	public final boolean ssmm_doesNotPlaceSellOrderWhenHoldingNegativeAmountOfStock = true;
	public final long ssmm_minimumSpread = 4;
	
	/*
	 * HFT simple chartist settings
	 */
	
	public final int nRoundsBetweenSamples = 10;
//	public final int sc_nLags = 500;
//	public final int sc_largestLag = 1000;
//	public final long sc_requiredPriceDifferenceBeforeOrdering = 3;
//	public final long sc_priceTickSize = 1;
//	public final int sc_waitTimeBetweenTrading = 10;

	
	
	/*
	 * Fundamental random walk parameters
	 */
//	public boolean randomWalkFundamental = false;
	public final long initialFundamentalPrice = (long) Math.pow(10, 4);
	public final double fundamentalBrownianMean = 0;
	public final double fundamentalBrownianVariance = 0.00001;
	
	/*
	 * Stylized trader parameters
	 */
	public final int st_minimumDelay = 1000;
	public final double st_delayStd = 1000;
	
	public final int st_orderLength = 1000000;
//	public final double st_noiseStd = 10;
	
	/*
	 * Slow trader volume parameters
	 */
	public final boolean st_randomOrderVolume = false;
	public final long st_minimumVolume = 10;
	public final double st_volumeNoiseStd = 10;
	
	/*
	 * Slow trader fundamentalist parameters
	 */
	public final double st_fund_additivePriceNoiseStd = 0;
	
	public final double st_fund_fundamentalNoiseStd = 5;
	public final long  st_fund_tickChange = 1;
//	public final long st_fund_orderVolume = 10;
	public final double st_fund_priceNoiseStd = 0;
	
	/*
	 * Other variables
	 */
	protected Logger config;
	protected Logger meta;
	
	private World world;
	public String experimentName;

	public String logFolder;
	
	public Experiment(String logFolder){
		this.logFolder = logFolder;
		this.world = new World(this);
		this.createStocks();
		this.createMarkets();
		this.createOrderbooks();
		this.createAgents();
		this.createObjectLoggers(this.logFolder);
		this.initializeEmptyOrderbooksWithMarketOrders();
		this.config = new Logger(this.logFolder, "config", Logger.Type.CSV, true, true, this);
		this.meta = new Logger(this.logFolder, "meta", Logger.Type.CSV, true, true, this);
	}
	
	protected void initializeExperimentWithChangedParameters(Experiment experiment) {
		
		this.overrideExperimentSpecificParameters();

	}
	
	public abstract void createAgents();
	public abstract void createStocks();
	public abstract void createMarkets();
	public abstract void overrideExperimentSpecificParameters();
	public abstract String getParameterString();
	public abstract void storeMetaInformation();
	
	
	
	
	
	
	public void createOrderbooks() {
		/*
		 * Should be executed whenever one (or several) new stock or market is
		 * created.
		 */
		for (Stock stock : this.world.getStocks()) {
			for (Market market : this.world.getMarkets()) {
				StockMarketPair p = new StockMarketPair(stock, market);
				if (!this.world.getOrderbooksByPair().containsKey(p)) {
					Orderbook ob = new Orderbook(this, stock, market);
					this.world.getOrderbooksByPair().put(p, ob);
					market.addOrderbook(ob);
				}
			}
		}
		this.world.getOrderbooks().clear();
		this.world.getOrderbooks().addAll(this.world.getOrderbooksByPair().values());
	}
	
public void createObjectLoggers(String logRootFolder) {
		
		
		
		this.world.warningLog = new WorldLogger(logRootFolder,"lineLog_worldWarnings", false, Logger.Type.TXT, true, Logging.logWorldWarningsToConsole, this);
		openLogs.add(this.world.warningLog);
		this.world.errorLog = new WorldLogger(logRootFolder,"lineLog_errors", false, Logger.Type.TXT, true, true, this);
		openLogs.add(this.world.errorLog);
		this.world.eventLog = new WorldLogger(logRootFolder,"lineLog_worldEvents", false, Logger.Type.TXT, true, Logging.logWorldEventsToConsole, this);
		openLogs.add(this.world.eventLog);
		this.world.ruleViolationsLog = new WorldLogger(logRootFolder,"lineLog_ruleViolations", false, Logger.Type.TXT, true, false, this);
		openLogs.add(this.world.ruleViolationsLog);
		this.world.dataLog = new WorldLogger(logRootFolder,"columnLog_worldData", true, Logger.Type.CSV, Logging.logWorldRoundDataToFile, Logging.logWorldRoundDataToConsole, this);
		openLogs.add(this.world.dataLog);
		
		for(Stock stock:this.world.getStocks()){
			stock.roundBasedDatalog = new StockLogger(logRootFolder, "columnLog_roundBased", stock, StockLogger.Type.LOG_AFTER_EVERY_ROUND, Logger.Type.CSV, Logging.logStockRoundDataToFile, Logging.logStockRoundDataToConsole, this);
			if(Logging.logStockRoundDataToFile){
				openLogs.add(stock.roundBasedDatalog);				
			}
			
			stock.transactionBasedDataLog = new StockLogger(logRootFolder, "columnLog_transactionBased", stock, StockLogger.Type.LOG_AFTER_EVERY_TRANSACTION, Logger.Type.CSV, Logging.logStockTransactionDataToFile, Logging.logStockTransactionDataToConsole, this);
			if(Logging.logStockTransactionDataToFile){
				openLogs.add(stock.transactionBasedDataLog);
			}
		}
		
		for(Orderbook orderbook:this.world.getOrderbooks()){
			orderbook.orderflowLog = new OrderbookLogger(logRootFolder, "lineLog_orderFlow_", orderbook, OrderbookLogger.Type.ORDER_FLOW_LOG, Logger.Type.TXT, Logging.logOrderbookOrderFlowToFile, Logging.logOrderbookOrderFlowToConsole, this);
			if(Logging.logOrderbookOrderFlowToFile){
				openLogs.add(orderbook.orderflowLog);
			}
			orderbook.eventLog = new OrderbookLogger(logRootFolder, "lineLog_events_", orderbook, OrderbookLogger.Type.EVENT_LOG, Logger.Type.TXT, Logging.logOrderbookEventsToFile, Logging.logOrderbookEventsToConsole, this);
			if(Logging.logOrderbookEventsToFile){
				openLogs.add(orderbook.eventLog);
			}
			
			orderbook.roundBasedLog = new OrderbookLogger(logRootFolder, "columnLog_roundBased", orderbook, OrderbookLogger.Type.ORDERBOOK_ROUND_BASED_DATA, Logger.Type.CSV, Logging.logOrderbookRoundBasedDataToFile, Logging.logOrderbookRoundBasedDataToConsole, this);
			if(Logging.logOrderbookRoundBasedDataToFile) {
				openLogs.add(orderbook.roundBasedLog);
			}
		}
		
		for(HFT agent:this.world.getHFTAgents()){
			agent.eventlog = new AgentLogger(logRootFolder, "lineLog", agent, Logger.Type.TXT, AgentLogger.headerType.NO_HEADER, Logging.logAgentActionsToFile, Logging.logAgentActionsToConsole, this);
			if(Logging.logAgentActionsToFile){
				openLogs.add(agent.eventlog);
			}
			agent.roundDatalog = new AgentLogger(logRootFolder, "columnLog_roundBased", agent, Logger.Type.CSV, AgentLogger.headerType.ROUND_DATA, Logging.logAgentRoundDataToFile, Logging.logAgentRoundDataToConsole, this);
			if(Logging.logAgentRoundDataToFile){
				openLogs.add(agent.roundDatalog);
			}
			
			agent.tradeLog = new AgentLogger(logRootFolder, "columnLog_tradelog", agent, Logger.Type.CSV, AgentLogger.headerType.TRADE_DATA, Logging.logAgentTradeDataToFile, Logging.logAgentTradeDataToConsole, this);
			if(Logging.logAgentTradeDataToFile){
				openLogs.add(agent.tradeLog);
			}
		}
	}
	
	public void closeLogs() {
		for(Logger log:openLogs){
			log.closeLog();
		}
		this.config.closeLog();
		this.meta.closeLog();
	}

	public void initializeEmptyOrderbooksWithMarketOrders() {
		for(Orderbook orderbook:this.world.getOrderbooks()) {
			orderbook.fillBookWithRandomOrders();
		}
		this.world.processNewOrdersInAllOrderbooks();
		
	}
	
	public void runExperiment(){
		this.world.executeInitalRoundsWithSlowTraders();
		this.world.executeHFTRounds();
		this.closeLogs();
		System.out.println(String.format("Finished simulation in %s seconds", ((double) this.world.runTime)/1000f));
	}
	
	protected void receiveParameter() {
		
	}
	
	

	public World getWorld() {
		return this.world;
	}

	public int getCurrentRound() {
		// TODO Auto-generated method stub
		return this.world.getCurrentRound();
	}
	
	public WorldLogger getErrorLog() {
		return this.world.errorLog;
	}
	
}



