package Experiments;

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
	public int nRounds = 10000;
	public static long nSlowTraderOrdersPerRounds = 10;

	/*
	 * Market rules
	 */
	public boolean allowsShortSelling = true;
	public boolean agentPaysWhenOrderIsFilledAfterSendingCancellation = true;
	public boolean agentMustBuyAllStocksAsSpecifiedInReceipt = true;
	public boolean agentMustSellAllStocksAsSpecifiedInReceipt = true;
	public boolean marketFillsEmptyBook = true;
	public long orderVolumeWhenMarketFillsEmptyBook = 99;
	public Order.Type orderTypeWhenMarketFillsEmptyBook = Order.Type.MARKET;
	public int orderLengthWhenMarketFillsEmptyBook = 5;

	
	
	/*
	 * High frequency trader behavior
	 */
	
	public boolean keepOrderHistory = true;
	
	public long emptyOrderbookWaitTime = 1;
	public boolean randomStartStockAmount = false;
	public long startStockAmount = 100;
	
	public boolean randomStartWealth = false;
	public long constantStartWealth = (int) Math.pow(10, 7);
	
	public long wealthMean = (int) Math.pow(10, 6);
	public long wealthStd = (int) Math.pow(10, 4);
	
//	public int minimumLatency = 10;
//	public int maximumLatency = 100;
	
	/*
	 * Single stock minimum spread market maker parameters 
	 */
	public long ssmm_tradeVolume = 20;
	public int ssmm_marketOrderLength = 200;
	public boolean doesNotPlaceSellOrderWhenHoldingNegativeAmountOfStock = true;
	public long minimumSpread = (long) Math.pow(10,4);
	
	/*
	 * Random walk parameters
	 */
	public boolean isRandomWalk = false;
	public final long initialFundamentalPrice = (long) Math.pow(10, 9);
	public final double fundamentalBrownianMean = 0;
	public final double fundamentalBrownianVariance = 0.00001;
	
	/*
	 * Stylized trader parameters
	 */
	public int orderLength = 1000;
	public boolean randomOrderVolume = false;
	public long constantVolume = 10;
	public double volumeMean = 100d;
	public double volumeStd = 10d;
	public double addivePriceNoiseMean = 0d;
	public double additivePriceNoiseStd = Math.pow(10, 5);
	
	/*
	 * Single stock market maker default 
	 */

	
	
	
	//	public long fundamentalistWeightMean = 0;
//	public long fundamentalistWeightStd = 1;
////	public long fundamentalMeanReversion = 
//	
//	public long chartistWeightMean = 0;
//	public long chartistWeightStd = 0;
//	
//	public long noiseWeightMean = 0;
//	public long noiseWeightStd = 1;
	
	public Experiment(){
		
	}
	
	protected void initializeExperimentWithChangedParameters(String logRootFolder, Experiment experiment) {
		this.createStocks();
		this.createMarkets();
		this.createOrderbooks(experiment);
		this.createAgents();
		this.createObjectLoggers(logRootFolder);
		this.initializeEmptyOrderbooksWithMarketOrders();
	}
	
	
	public abstract void createAgents();
	public abstract void createStocks();
	public abstract void createMarkets();
	public abstract void overrideDefaultParameters();
	
	public void createOrderbooks(Experiment experiment) {
		/*
		 * Should be executed whenever one (or several) new stock or market is
		 * created.
		 */
		for (Stock stock : World.getStocks()) {
			for (Market market : World.getMarkets()) {
				StockMarketPair p = new StockMarketPair(stock, market);
				if (!World.getOrderbooksByPair().containsKey(p)) {
					Orderbook ob = new Orderbook(experiment, stock, market);
					World.getOrderbooksByPair().put(p, ob);
					market.addOrderbook(ob);
				}
			}
		}
		World.getOrderbooks().clear();
		World.getOrderbooks().addAll(World.getOrderbooksByPair().values());
	}
	
public void createObjectLoggers(String logRootFolder) {
		
		
		
		World.warningLog = new WorldLogger(logRootFolder,"lineLog_worldWarnings", false, Logger.Type.TXT, true, false);
		openLogs.add(World.warningLog);
		World.errorLog = new WorldLogger(logRootFolder,"lineLog_errors", false, Logger.Type.TXT, true, false);
		openLogs.add(World.errorLog);
		World.eventLog = new WorldLogger(logRootFolder,"lineLog_worldEvents", false, Logger.Type.TXT, true, false);
		openLogs.add(World.eventLog);
		World.ruleViolationsLog = new WorldLogger(logRootFolder,"lineLog_ruleViolations", false, Logger.Type.TXT, true, false);
		openLogs.add(World.ruleViolationsLog);
		World.dataLog = new WorldLogger(logRootFolder,"columnLog_worldData", true, Logger.Type.CSV, true, false);
		openLogs.add(World.dataLog);
		
		for(Stock stock:World.getStocks()){
			stock.roundBasedDatalog = new StockLogger(logRootFolder, "columnLog_roundBased", stock, StockLogger.Type.LOG_AFTER_EVERY_ROUND, Logger.Type.CSV, Logging.logStockRoundDataToFile, Logging.logStockRoundDataToConsole);
			if(Logging.logStockRoundDataToFile){
				openLogs.add(stock.roundBasedDatalog);				
			}
			
			stock.transactionBasedDataLog = new StockLogger(logRootFolder, "columnLog_transactionBased", stock, StockLogger.Type.LOG_AFTER_EVERY_TRANSACTION, Logger.Type.CSV, Logging.logStockTransactionDataToFile, Logging.logStockTransactionDataToConsole);
			if(Logging.logStockTransactionDataToFile){
				openLogs.add(stock.transactionBasedDataLog);
			}
		}
		
		for(Orderbook orderbook:World.getOrderbooks()){
			orderbook.orderflowLog = new OrderbookLogger(logRootFolder, "lineLog_orderFlow_", orderbook, OrderbookLogger.Type.ORDER_FLOW_LOG, Logger.Type.TXT, Logging.logOrderbookOrderFlowToFile, Logging.logOrderbookOrderFlowToConsole);
			if(Logging.logOrderbookOrderFlowToFile){
				openLogs.add(orderbook.orderflowLog);
			}
			orderbook.eventLog = new OrderbookLogger(logRootFolder, "lineLog_events_", orderbook, OrderbookLogger.Type.EVENT_LOG, Logger.Type.TXT, Logging.logOrderbookEventsToFile, Logging.logOrderbookEventsToConsole);
			if(Logging.logOrderbookEventsToFile){
				openLogs.add(orderbook.eventLog);
			}
		}
		
		for(HFT agent:World.getHFTAgents()){
			agent.eventlog = new AgentLogger(logRootFolder, "lineLog", agent, Logger.Type.TXT, AgentLogger.headerType.NO_HEADER, Logging.logAgentActionsToFile, Logging.logAgentActionsToConsole);
			if(Logging.logAgentActionsToFile){
				openLogs.add(agent.eventlog);
			}
			agent.roundDatalog = new AgentLogger(logRootFolder, "columnLog_roundBased", agent, Logger.Type.CSV, AgentLogger.headerType.ROUND_DATA, Logging.logAgentRoundDataToFile, Logging.logAgentRoundDataToConsole);
			if(Logging.logAgentRoundDataToFile){
				openLogs.add(agent.roundDatalog);
			}
			
			agent.tradeLog = new AgentLogger(logRootFolder, "columnLog_tradelog", agent, Logger.Type.CSV, AgentLogger.headerType.TRADE_DATA, Logging.logAgentTradeDataToFile, Logging.logAgentTradeDataToConsole);
			if(Logging.logAgentTradeDataToFile){
				openLogs.add(agent.tradeLog);
			}
		}
	}
	
	public static void closeLogs() {
		for(Logger log:openLogs){
			log.closeLog();
		}
	}

	public void initializeEmptyOrderbooksWithMarketOrders() {
		for(Orderbook orderbook:World.getOrderbooks()) {
			orderbook.initializeBookWithRandomOrders();
		}
		World.processNewOrdersInAllOrderbooks();
		
	}
	
	public void runExperiment(Experiment experiment){
		World.executeNRounds(experiment, experiment.nRounds-1);
		closeLogs();
	}
	
	 
}



