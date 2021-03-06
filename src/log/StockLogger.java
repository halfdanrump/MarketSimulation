package log;

import environment.Order;
import environment.Orderbook;
import environment.Stock;
import experiments.Experiment;

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
		String header = "round, price";
		if(this.logToFile){
			super.writeToFile(header);
		}
	}
	
//	public void recordStockInformationAfterTransactionHeader() {
//		String header = "round, marketID, price, buyer, seller";
//		if(this.logToFile){
//			super.writeToFile(header);
//		}
//	}
	
	public void recordStockInformationAfterTransaction(long tradePrice, Orderbook orderbook, Order newOrder, Order matchingOrder) {
		if(this.createLogString){
			
			String entry = String.format("%s,%s", this.experiment.getWorld().getCurrentRound(), tradePrice);
//		String entry = super.getNewEntry() + String.format(", %s, %s", orderbook.getMarket().getID(), tradePrice);
			if(this.logToFile){
				super.writeToFile(entry);				
			}
			if(this.logToConsole){
				super.writeToConsole(entry);
			}
		}
	}
	
//	public void recordStockInformationAfterTransaction(long tradePrice, Orderbook orderbook, Order newOrder, Order matchingOrder) {
//		if(this.createLogString){
//			String buyerID = "";
//			String sellerID = "";
//			
//			if(newOrder.getBuySell() == Order.BuySell.BUY) {
//				buyerID = newOrder.getOwnerID();
//				sellerID = matchingOrder.getOwnerID();
//			} else {
//				buyerID = matchingOrder.getOwnerID();
//				sellerID = newOrder.getOwnerID();
//			}
//			
//			String entry = String.format("%s,%s,%s,%s,%s", this.experiment.getWorld().getCurrentRound(), orderbook.getMarket().getID(), tradePrice, buyerID, sellerID);
////		String entry = super.getNewEntry() + String.format(", %s, %s", orderbook.getMarket().getID(), tradePrice);
//			if(this.logToFile){
//				super.writeToFile(entry);				
//			}
//			if(this.logToConsole){
//				super.writeToConsole(entry);
//			}
//		}
//	}
	
	
	public void recordRoundBasedHeader(){
		if(this.createLogString){
			if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
				String header = "round,fundamental,tradedVolume";
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
	
//	public void recordRoundBasedHeader(){
//		if(this.createLogString){
//			if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
//				String header = "round,fundamental,tradedVolume,smallestLocalSpread,globalLowestBuyPrice,globalHighestBuyPrice," +
//						"globalLowestSellPrice,globalHighestSellPrice";
//				for(Market market:stock.getMarkets()){
//					header += String.format(",bestSellAtMarket%s,bestBuyAtMarket%s, spreadAtMarket%s", market.getID(), market.getID(), market.getID());
//				}
//				if(this.logToFile){
//					super.writeToFile(header);			
//				}
//				if(this.logToConsole){
//					super.writeToConsole(header);
//				}
//			} else {
//				this.experiment.getWorld().warningLog.logOnelineWarning("A stock logger called the wrong function.");
//			}
//		}
//	}
	
	public void recordEndRoundPriceInformation(int round){
		if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
			if(this.createLogString){

				String entry = String.format("%s,%s,%s",
						round, 
						stock.getFundamentalPrice(round),
						stock.getTotalTradedVolume(round));
				
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
		
//		public void recordEndRoundPriceInformation(int round){
//			if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
//				if(this.createLogString){
//					String bestBuyPrice, bestSellPrice;
//					String entry = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
//							round, 
//							stock.getFundamentalPrice(round),
//							stock.getTotalTradedVolume(round),
//							stock.getLocalSmallestOrderbookSpread(round),
//							stock.getGlobalLowestBuyPrice(round),
//							stock.getGlobalHighestBuyPrice(round),
//							stock.getGlobalLowestSellPrice(round),
//							stock.getGlobalHighestSellPrice(round));
//					
//					for(Market market:stock.getMarkets()){
//						Orderbook orderbook = market.getOrderbook(stock);
//						bestBuyPrice = String.valueOf(orderbook.getLocalBestBuyPriceAtEndOfRound(round));
//						bestSellPrice = String.valueOf(orderbook.getLocalBestSellPriceAtEndOfRound(round));					
//						String spread = market.getOrderbook(stock).getSpreadForBestPricesAtEndOfRound(round);
//						
//						entry += String.format(", %s, %s, %s",bestSellPrice, bestBuyPrice, spread);
//					}
//					if(this.logToFile){
//						super.writeToFile(entry);
//					}
//					
//					if(this.logToConsole){
//						super.writeToConsole(entry);
//					}
//				}
//			} else {
//				this.experiment.getWorld().warningLog.logOnelineWarning(String.format("A stock logger of type %s was asked to record round based information...", this.type));
//			}

		
		
		//		TreeSet keys = new TreeSet(this.stock.getBestAskPrices(World.getCurrentTime()).keySet());
//		keys.iterator()
		
		
	}

}
