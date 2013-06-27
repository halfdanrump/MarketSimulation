package log;

import utilities.NoOrdersException;
import environment.Market;
import environment.Orderbook;
import environment.Stock;
import environment.World;

public class StockLogger extends Logger {
	public enum Type{
		LOG_AFTER_EVERY_ROUND,
		LOG_AFTER_EVERY_TRANSACTION
	}
	
	private Stock stock;
	private Type type;
	
	public StockLogger(String directory, String logType, Stock stock, StockLogger.Type loggerType, Logger.Type type) {
		super(directory, String.format("%s_stock%s", logType, stock.getID()), type);
		this.stock = stock;
		this.type = loggerType;
		if(loggerType == StockLogger.Type.LOG_AFTER_EVERY_ROUND) {
			this.recordRoundBasedHeader();
		} else if(loggerType == StockLogger.Type.LOG_AFTER_EVERY_TRANSACTION){
			this.recordStockInformationAfterTransactionHeader();
		}
	}
	
	public void recordStockInformationAfterTransactionHeader() {
		String header = super.getNewEntry() + ", Book, Price";
		super.writeToFile(header);
	}
	
	public void recordStockInformationAfterTransaction(long tradePrice, Orderbook orderbook) {
		String entry = super.getNewEntry() + String.format(", %s, %s", orderbook.getMarket().getID(), tradePrice);
		super.writeToFile(entry);
	}
	
	
	public void recordRoundBasedHeader(){
		if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
			String header = "Round,  Fund";
			for(Market market:stock.getMarkets()){
				header += String.format(", bSell%s, bBuy%s, Spread", market.getID(), market.getID());
			}
			super.writeToFile(header);			
		} else {
			World.warningLog.logOnelineWarning("A stock logger called the wrong function.");
		}
	}
	
	public void recordEndRoundPriceInformation(int round){
		if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
			String bestBuyPrice, bestSellPrice;
			String entry = String.format("%s, %s", round, stock.getFundamentalPrice(round));
			
			for(Market market:stock.getMarkets()){
				Orderbook orderbook = market.getOrderbook(stock);
				try{
					bestBuyPrice = String.valueOf(orderbook.getBestBuyPriceAtEndOfRound(round));
				} catch(NoOrdersException e){
					bestBuyPrice = "NaN";
				}
				try {
					bestSellPrice = String.valueOf(orderbook.getBestSellPriceAtEndOfRound(round));
				} catch (NoOrdersException e) {
					bestSellPrice = "NaN";
				}
				
				String spread = market.getOrderbook(stock).getSpreadForBestPricesAtEndOfRound(round);
				
				entry += String.format(", %s, %s, %s",bestSellPrice, bestBuyPrice, spread);
			}
			super.writeToFile(entry);
		} else {
			World.warningLog.logOnelineWarning(String.format("A stock logger of type %s was asked to record round based information...", this.type));
		}
		
		
		//		TreeSet keys = new TreeSet(this.stock.getBestAskPrices(World.getCurrentTime()).keySet());
//		keys.iterator()
		
		
	}

}
