package log;

import Experiments.Experiment;
import environment.Market;
import environment.NoOrdersException;
import environment.Order;
import environment.Orderbook;
import environment.Stock;

public class StockLogger extends Logger {
	public enum Type{
		LOG_AFTER_EVERY_ROUND,
		LOG_AFTER_EVERY_TRANSACTION
	}
	
	private Stock stock;
	private Type type;
//	private boolean logToFile;
	
	public StockLogger(String directory, String logType, Stock stock, StockLogger.Type loggerType, Logger.Type type, boolean logToFile, boolean logToConsole, Experiment experiment) {
		super(directory, String.format("%s_stock%s", logType, stock.getID()), type, logToFile, logToConsole, experiment);
		this.stock = stock;
		this.type = loggerType;
		if(loggerType == StockLogger.Type.LOG_AFTER_EVERY_ROUND) {
			this.recordRoundBasedHeader();
		} else if(loggerType == StockLogger.Type.LOG_AFTER_EVERY_TRANSACTION){
			this.recordStockInformationAfterTransactionHeader();
		}
	}
	
	public void recordStockInformationAfterTransactionHeader() {
		String header = "round, marketID, price, buyer, seller";
		if(this.logToFile){
			super.writeToFile(header);
		}
	}
	
	public void recordStockInformationAfterTransaction(long tradePrice, Orderbook orderbook, Order newOrder, Order matchingOrder) {
		if(this.createLogString){
			String buyerID = "";
			String sellerID = "";
			
			if(newOrder.getBuySell() == Order.BuySell.BUY) {
				buyerID = newOrder.getOwnerID();
				sellerID = matchingOrder.getOwnerID();
			} else {
				buyerID = matchingOrder.getOwnerID();
				sellerID = newOrder.getOwnerID();
			}
			
			String entry = String.format("%s,%s,%s,%s,%s", this.experiment.getWorld().getCurrentRound(), orderbook.getMarket().getID(), tradePrice, buyerID, sellerID);
//		String entry = super.getNewEntry() + String.format(", %s, %s", orderbook.getMarket().getID(), tradePrice);
			if(this.logToFile){
				super.writeToFile(entry);				
			}
			if(this.logToConsole){
				super.writeToConsole(entry);
			}
		}
	}
	
	
	public void recordRoundBasedHeader(){
		if(this.createLogString){
			if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
				String header = "round,fundamental,smallestLocalSpread,globalLowestBuyPrice,globalHighestBuyPrice," +
						"globalLowestSellPrice,globalHighestSellPrice";
				for(Market market:stock.getMarkets()){
					header += String.format(",bestSellAtMarket%s,bestBuyAtMarket%s, spreadAtMarket%s", market.getID(), market.getID(), market.getID());
				}
				if(this.logToFile){
					super.writeToFile(header);			
				}
				if(this.logToConsole){
					super.writeToConsole(header);
				}
			} else {
				this.experiment.getWorld().warningLog.logOnelineWarning("A stock logger called the wrong function.");
			}
		}
	}
	
	public void recordEndRoundPriceInformation(int round){
		if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
			if(this.createLogString){
				String bestBuyPrice, bestSellPrice;
				String entry = String.format("%s,%s,%s,%s,%s,%s,%s",
						round, stock.getFundamentalPrice(round),
						stock.getLocalSmallestOrderbookSpread(round),
						stock.getGlobalLowestBuyPrice(round),
						stock.getGlobalHighestBuyPrice(round),
						stock.getGlobalLowestSellPrice(round),
						stock.getGlobalHighestSellPrice(round));
				
				for(Market market:stock.getMarkets()){
					Orderbook orderbook = market.getOrderbook(stock);
					try{
						bestBuyPrice = String.valueOf(orderbook.getLocalBestBuyPriceAtEndOfRound(round));
					} catch(NoOrdersException e){
						bestBuyPrice = "NaN";
					}
					try {
						bestSellPrice = String.valueOf(orderbook.getLocalBestSellPriceAtEndOfRound(round));
					} catch (NoOrdersException e) {
						bestSellPrice = "NaN";
					}
					
					String spread = market.getOrderbook(stock).getSpreadForBestPricesAtEndOfRound(round);
					
					entry += String.format(", %s, %s, %s",bestSellPrice, bestBuyPrice, spread);
				}
				if(this.logToFile){
					super.writeToFile(entry);
				}
				
				if(this.logToConsole){
					super.writeToConsole(entry);
				}
			}
		} else {
			this.experiment.getWorld().warningLog.logOnelineWarning(String.format("A stock logger of type %s was asked to record round based information...", this.type));
		}
		
		
		//		TreeSet keys = new TreeSet(this.stock.getBestAskPrices(World.getCurrentTime()).keySet());
//		keys.iterator()
		
		
	}

}
