package Experiments;

import java.io.File;
import java.util.ArrayList;

import org.rosuda.JRI.Rengine;

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
	public final int nTotalRounds = 21000;
	public final int nInitialSlowTraderRounds = 3000;
	public final int nHFTRounds = nTotalRounds - nInitialSlowTraderRounds-1; 
	
	public long nSlowTraderOrdersPerRound = 50;
	public int nHFTsPerGroup = 10;
	public int nGroups = 1;
	
	/*
	 * Orderbook settings
	 */
	public int ob_nStartOrders = 10;
	public int ob_initialOrderStd = 5;
	public int ob_initialOrderVolumeMean = 10;
	public int ob_initialOrderVolumeStd = 10;
	public int ob_startingSpread = 20;
	public int ob_orderExpirationTime = 100000;
	
	/*
	 * Market rules
	 */
	public boolean allowsShortSelling = true;
	public boolean agentPaysWhenOrderIsFilledAfterSendingCancellation = true;
	public boolean agentMustBuyAllStocksAsSpecifiedInReceipt = true;
	public boolean agentMustSellAllStocksAsSpecifiedInReceipt = true;
	public boolean marketFillsEmptyBook = true;
	public long orderVolumeWhenMarketFillsEmptyBook = 10;
	public Order.Type orderTypeWhenMarketFillsEmptyBook = Order.Type.MARKET;
	public int orderLengthWhenMarketFillsEmptyBook = 5;
	
	/*
	 * High frequency trader behavior
	 */
	
	public boolean keepOrderHistory = false;
	
	public long emptyOrderbookWaitTime = 1;
	public boolean randomStartStockAmount = false;
	public long startStockAmount = 10000000;
	
	public boolean randomStartWealth = false;
	public long constantStartWealth = (int) Math.pow(10, 3);
	
	public long wealthMean = (int) Math.pow(10, 6);
	public long wealthStd = (int) Math.pow(10, 4);
	
	
	/*
	 * Single stock minimum spread market maker parameters 
	 */
	public long ssmm_tradeVolume = 10;
	public int ssmm_marketOrderLength = 200;
	public boolean ssmm_doesNotPlaceSellOrderWhenHoldingNegativeAmountOfStock = true;
	public long ssmm_minimumSpread = 2;
	
	/*
	 * Random walk parameters
	 */
	public boolean randomWalkFundamental = false;
	public final long initialFundamentalPrice = (long) Math.pow(10, 4);
	public final double fundamentalBrownianMean = 0;
	public final double fundamentalBrownianVariance = 0.00001;
	
	/*
	 * Stylized trader parameters
	 */
	public int st_delayInRounds = 1000;
	public int st_orderLength = 1000;
	public double st_noiseStd = 5;
	public boolean st_randomOrderVolume = false;
	public long st_constantVolume = 10;
	public double st_volumeMean = 100d;
	public double st_volumeStd = 10d;

	
	public double addivePriceNoiseMean = 0d;
	public double additivePriceNoiseStd = 5;
	
	/*
	 * Other variables
	 */
	protected Logger config;
	protected Logger meta;
	
	private World world;
	public String experimentName;

	public String rootFolder;
	public String logRootFolder;
	public String logFolder;
	public String RscriptFilePath;
	public String graphRootFolder;
	public String graphFolder;
	
	public Experiment(String rootFolder){
		this.rootFolder = rootFolder;
		this.world = new World(this);
	}
	
	protected void initializeExperimentWithChangedParameters(Experiment experiment) {
		this.setExperimentSpecificFolders(experiment.getClass().getSimpleName());
		
		this.config = new Logger(this.logRootFolder, "config", Logger.Type.CSV, true, true, experiment);
		this.meta = new Logger(this.logRootFolder, "meta", Logger.Type.CSV, true, true, experiment);
		
		this.overrideExperimentSpecificParameters();
		String parameters = this.getParameterString();
		this.config.writeToFile(parameters);
		this.config.writeToConsole(parameters);
		this.createStocks();
		this.createMarkets();
		this.createOrderbooks(experiment);
		this.createAgents();
		this.createObjectLoggers(this.logRootFolder, experiment);
		this.initializeEmptyOrderbooksWithMarketOrders();
	}
	
	public abstract void createAgents();
	public abstract void createStocks();
	public abstract void createMarkets();
	public abstract void overrideExperimentSpecificParameters();
	public abstract String getParameterString();
	public abstract void storeMetaInformation();
	
	
	
	public void setExperimentSpecificFolders(String experimentName) {
		this.experimentName = experimentName;
		this.logRootFolder = String.format("%slogs/%s/", this.rootFolder, this.experimentName);
		this.RscriptFilePath = String.format("%sdataAnalysis/Rscripts/%s.r", this.rootFolder, this.experimentName);
		this.graphRootFolder = String.format("%sdataAnalysis/graphs/%s/", this.rootFolder, this.experimentName);
	}
	
	
	public void createOrderbooks(Experiment experiment) {
		/*
		 * Should be executed whenever one (or several) new stock or market is
		 * created.
		 */
		for (Stock stock : this.world.getStocks()) {
			for (Market market : this.world.getMarkets()) {
				StockMarketPair p = new StockMarketPair(stock, market);
				if (!this.world.getOrderbooksByPair().containsKey(p)) {
					Orderbook ob = new Orderbook(experiment, stock, market);
					this.world.getOrderbooksByPair().put(p, ob);
					market.addOrderbook(ob);
				}
			}
		}
		this.world.getOrderbooks().clear();
		this.world.getOrderbooks().addAll(this.world.getOrderbooksByPair().values());
	}
	
public void createObjectLoggers(String logRootFolder, Experiment experiment) {
		
		
		
		this.world.warningLog = new WorldLogger(logRootFolder,"lineLog_worldWarnings", false, Logger.Type.TXT, true, Logging.logWorldWarningsToConsole, experiment);
		openLogs.add(this.world.warningLog);
		this.world.errorLog = new WorldLogger(logRootFolder,"lineLog_errors", false, Logger.Type.TXT, true, true, experiment);
		openLogs.add(this.world.errorLog);
		this.world.eventLog = new WorldLogger(logRootFolder,"lineLog_worldEvents", false, Logger.Type.TXT, true, Logging.logWorldEventsToConsole, experiment);
		openLogs.add(this.world.eventLog);
		this.world.ruleViolationsLog = new WorldLogger(logRootFolder,"lineLog_ruleViolations", false, Logger.Type.TXT, true, false, experiment);
		openLogs.add(this.world.ruleViolationsLog);
		this.world.dataLog = new WorldLogger(logRootFolder,"columnLog_worldData", true, Logger.Type.CSV, Logging.logWorldRoundDataToFile, Logging.logWorldRoundDataToConsole, experiment);
		openLogs.add(this.world.dataLog);
		
		for(Stock stock:this.world.getStocks()){
			stock.roundBasedDatalog = new StockLogger(logRootFolder, "columnLog_roundBased", stock, StockLogger.Type.LOG_AFTER_EVERY_ROUND, Logger.Type.CSV, Logging.logStockRoundDataToFile, Logging.logStockRoundDataToConsole, experiment);
			if(Logging.logStockRoundDataToFile){
				openLogs.add(stock.roundBasedDatalog);				
			}
			
			stock.transactionBasedDataLog = new StockLogger(logRootFolder, "columnLog_transactionBased", stock, StockLogger.Type.LOG_AFTER_EVERY_TRANSACTION, Logger.Type.CSV, Logging.logStockTransactionDataToFile, Logging.logStockTransactionDataToConsole, experiment);
			if(Logging.logStockTransactionDataToFile){
				openLogs.add(stock.transactionBasedDataLog);
			}
		}
		
		for(Orderbook orderbook:this.world.getOrderbooks()){
			orderbook.orderflowLog = new OrderbookLogger(logRootFolder, "lineLog_orderFlow_", orderbook, OrderbookLogger.Type.ORDER_FLOW_LOG, Logger.Type.TXT, Logging.logOrderbookOrderFlowToFile, Logging.logOrderbookOrderFlowToConsole, experiment);
			if(Logging.logOrderbookOrderFlowToFile){
				openLogs.add(orderbook.orderflowLog);
			}
			orderbook.eventLog = new OrderbookLogger(logRootFolder, "lineLog_events_", orderbook, OrderbookLogger.Type.EVENT_LOG, Logger.Type.TXT, Logging.logOrderbookEventsToFile, Logging.logOrderbookEventsToConsole, experiment);
			if(Logging.logOrderbookEventsToFile){
				openLogs.add(orderbook.eventLog);
			}
		}
		
		for(HFT agent:this.world.getHFTAgents()){
			agent.eventlog = new AgentLogger(logRootFolder, "lineLog", agent, Logger.Type.TXT, AgentLogger.headerType.NO_HEADER, Logging.logAgentActionsToFile, Logging.logAgentActionsToConsole, experiment);
			if(Logging.logAgentActionsToFile){
				openLogs.add(agent.eventlog);
			}
			agent.roundDatalog = new AgentLogger(logRootFolder, "columnLog_roundBased", agent, Logger.Type.CSV, AgentLogger.headerType.ROUND_DATA, Logging.logAgentRoundDataToFile, Logging.logAgentRoundDataToConsole, experiment);
			if(Logging.logAgentRoundDataToFile){
				openLogs.add(agent.roundDatalog);
			}
			
			agent.tradeLog = new AgentLogger(logRootFolder, "columnLog_tradelog", agent, Logger.Type.CSV, AgentLogger.headerType.TRADE_DATA, Logging.logAgentTradeDataToFile, Logging.logAgentTradeDataToConsole, experiment);
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
//			orderbook.initializeBookWithRandomOrders();
			orderbook.fillBookWithRandomOrders();
		}
		this.world.processNewOrdersInAllOrderbooks();
		
	}
	
	public static void runExperiment(Experiment experiment){
		experiment.world.executeInitalRoundsWithSlowTraders();
		experiment.world.executeHFTRounds();
		experiment.closeLogs();
		System.out.println(String.format("Finished simulation in %s seconds", ((double) experiment.world.runTime)/1000f));
	}
	
	public static void runRscript(Experiment experiment, Rengine re) {
		String gf = experiment.graphFolder;
		new File(experiment.graphFolder).mkdirs();
		System.out.println("Running R script...");
		if(new File(experiment.RscriptFilePath).exists()) {
			re.eval(String.format("source('%s')", experiment.RscriptFilePath));
			System.out.println("Finished running R script...");	
		} else {
			System.out.println("R script not found. Aborting!");
			System.exit(1);
		}
	}
	

	public World getWorld() {
		return this.world;
	}

	public int getCurrentRound() {
		// TODO Auto-generated method stub
		return this.world.getCurrentRound();
	}
}



