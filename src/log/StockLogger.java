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
	
	public StockLogger(String directory, String logType, Stock stock, StockLogger.Type loggerType) {
		super(directory, String.format("%s_stock%s", logType, stock.getID()));
		this.stock = stock;
		this.type = loggerType;
		if(loggerType == StockLogger.Type.LOG_AFTER_EVERY_ROUND) {
			this.recordRoundBasedHeader();
		} else if(loggerType == StockLogger.Type.LOG_AFTER_EVERY_TRANSACTION){
			
		}
	}
	
	public void recordStockInformationAfterTransactionHeader() {
		String header = super.getNewEntry() + "\tOrderbook\tPrice";
		super.writeToFile(header);
	}
	
	public void recordStockInformationAfterTransaction(int price, Orderbook orderbook) {
		String entry = super.getNewEntry() + String.format("\t%s\t%s", orderbook.getMarket().getID(), price);
		super.writeToFile(entry);
	}
	
	
	public void recordRoundBasedHeader(){
		if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
			String header = "Round\t Fund";
			for(Market market:stock.getMarkets()){
				header += String.format("\tbSell%s\tbBuy%s\tSpread", market.getID(), market.getID());
			}
			super.writeToFile(header);			
		} else {
			World.warningLog.logOnelineWarning("A stock logger called the wrong function.");
		}
	}
	
	public void recordEndRoundPriceInformation(int round){
		if(this.type == Type.LOG_AFTER_EVERY_ROUND) {
			String bestBuyPrice, bestSellPrice;
			String entry = String.format("%s\t%s", round, stock.getFundamentalPrice(round));
			
			for(Market market:stock.getMarkets()){
				Orderbook orderbook = market.getOrderbook(stock);
				try{
					bestBuyPrice = String.valueOf(orderbook.getBestBuyPrice(round));
				} catch(NoOrdersException e){
					bestBuyPrice = "NaN";
				}
				try {
					bestSellPrice = String.valueOf(orderbook.getBestSellPrice(round));
				} catch (NoOrdersException e) {
					bestSellPrice = "NaN";
				}
				
				String spread = market.getOrderbook(stock).getSpread(round);
				
				entry += String.format("\t%s\t%s\t%s",bestSellPrice, bestBuyPrice, spread);
			}
			super.writeToFile(entry);
		} else {
			World.warningLog.logOnelineWarning(String.format("A stock logger of type %s was asked to record round based information...", this.type));
		}
		
		
		//		TreeSet keys = new TreeSet(this.stock.getBestAskPrices(World.getCurrentTime()).keySet());
//		keys.iterator()
		
		
	}

}
