package setup;

import java.util.ArrayList;

import utilities.StockMarketPair;

import log.AgentLogger;
import log.Logger;
import log.Logging;
import log.OrderbookLogger;
import log.StockLogger;
import log.WorldLogger;
import environment.Market;
import environment.Orderbook;
import environment.Stock;
import environment.World;
import agent.HFT;
import agent.SingleStockMarketMaker;

public class WorldObjectHandler {
	
	private static ArrayList<Logger> logs = new ArrayList<Logger>();

	public static void createAgents(){
//		HFT agent = new HFT();	
		int[] stockIDs = {0}; int[] marketIDs = {0, 1}; int[] latencies = {2, 3}; long minimumSpread = 100;  
		new SingleStockMarketMaker(stockIDs, marketIDs, latencies, minimumSpread);
//		int[] stockIDs2 = {0}; int[] marketIDs2 = {0, 1}; int[] latencies2 = {10, 30}; long minimumSpread2 = 100;  
//		new SingleStockMarketMaker(stockIDs2, marketIDs2, latencies2, minimumSpread2);
	}
	
	public static void createStocks(){
		new Stock();
	}
	
	public static void createMarkets(){
		new Market();
		new Market();
	}
	
	public static void createOrderbooks() {
		/*
		 * Should be executed whenever one (or several) new stock or market is
		 * created.
		 */
		for (Stock stock : World.getStocks()) {
			for (Market market : World.getMarkets()) {
				StockMarketPair p = new StockMarketPair(stock, market);
				if (!World.getOrderbooksByPair().containsKey(p)) {
					Orderbook ob = new Orderbook(stock, market);
					World.getOrderbooksByPair().put(p, ob);
					market.addOrderbook(ob);
				}
			}
		}
		World.getOrderbooks().clear();
		World.getOrderbooks().addAll(World.getOrderbooksByPair().values());
	}
	
	public static void createObjectLoggers() {
		
		World.warningLog = new WorldLogger(Logging.logFolder,"lineLog_worldWarnings", false, Logger.Type.TXT);
		logs.add(World.warningLog);
		World.errorLog = new WorldLogger(Logging.logFolder,"lineLog_errors", false, Logger.Type.TXT);
		logs.add(World.errorLog);
		World.eventLog = new WorldLogger(Logging.logFolder,"lineLog_worldEvents", false, Logger.Type.TXT);
		logs.add(World.eventLog);
		World.ruleViolationsLog = new WorldLogger(Logging.logFolder,"lineLog_ruleViolations", false, Logger.Type.TXT);
		logs.add(World.ruleViolationsLog);
		World.dataLog = new WorldLogger(Logging.logFolder,"columnLog_worldData", true, Logger.Type.CSV);
		logs.add(World.dataLog);
		
		for(Stock stock:World.getStocks()){
			stock.roundBasedDatalog = new StockLogger(Logging.logFolder, "columnLog_roundBased", stock, StockLogger.Type.LOG_AFTER_EVERY_ROUND, Logger.Type.CSV);
			logs.add(stock.roundBasedDatalog);
			stock.transactionBasedDataLog = new StockLogger(Logging.logFolder, "columnLog_transactionBased", stock, StockLogger.Type.LOG_AFTER_EVERY_TRANSACTION, Logger.Type.CSV);
			logs.add(stock.transactionBasedDataLog);
		}
		
		for(Orderbook orderbook:World.getOrderbooks()){
			orderbook.orderflowLog = new OrderbookLogger(Logging.logFolder, "lineLog_orderFlow_", orderbook, OrderbookLogger.Type.ORDER_FLOW_LOG, Logger.Type.TXT);
			logs.add(orderbook.orderflowLog);
			orderbook.eventLog = new OrderbookLogger(Logging.logFolder, "linelog_events_", orderbook, OrderbookLogger.Type.EVENT_LOG, Logger.Type.TXT);
			logs.add(orderbook.eventLog);
		}
		
		for(HFT agent:World.getHFTAgents()){
			agent.eventlog = new AgentLogger(Logging.logFolder, "lineLog", agent, false, Logger.Type.TXT);
			logs.add(agent.eventlog);
			agent.datalog = new AgentLogger(Logging.logFolder, "columnLog", agent, true, Logger.Type.CSV);
			logs.add(agent.datalog);
		}
	}
	
	public static void closeLogs() {
		for(Logger log:logs){
			log.closeLog();
		}
	}

	public static void initializeEmptyOrderbooksWithMarketOrders() {
		for(Orderbook orderbook:World.getOrderbooks()) {
			orderbook.initializeBookWithRandomOrders();
		}
		World.processNewOrdersInAllOrderbooks();
		
	}
	
	
}
