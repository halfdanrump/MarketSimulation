package environment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

import Experiments.Experiment;

import umontreal.iro.lecuyer.stochprocess.GeometricBrownianMotion;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import utilities.MarketIdComparator;
import log.StockLogger;
import environment.World;

public class Stock { 
//	private World world;
	private long id;
	private Experiment experiment;
	private ArrayList<Long> fundamentalPrice;
	/*
	 * 
	 */
	private ArrayList<Long> globalLowestBuy;
	private ArrayList<Long> globalHighestBuy;
	private ArrayList<Long> globalLowestSell;
	private ArrayList<Long> globalHighestSell;
	private ArrayList<Long> localSmallestOrderbookSpread;
	
	private ArrayList<Market> bestMarketInRound;
	private GeometricBrownianMotion gbm;
	private static long nStocks = 0;
//	private TreeSet<Orderbook> tradedOn;
	private TreeSet<Market> markets;
	public StockLogger roundBasedDatalog;
	public StockLogger transactionBasedDataLog;
	private long globalLastTradedMarketOrderBuyPrice;
	private long globalLastTradedMarketOrderSellPrice;
//	private int[] fundamentalPrice;
//	private int[] bestNBBOBID;
//	private int[] bestNBBOASK;
//	private ArrayList<Orderbook> tradedOn = new ArrayList<Orderbook>();
//	private Hashtable<String, Orderbook[]> orderbooksByStockID = new Hashtable<String, Orderbook[]>();
	
	
//	public Stock(long initialFundamentalPrice, double fundamentalBrownianMean, double fundamentalBrownianVariance){
//		initialize();
//		this.fundamentalPrice.add(initialFundamentalPrice);
//		this.gbm = new GeometricBrownianMotion(initialFundamentalPrice, fundamentalBrownianMean, fundamentalBrownianVariance, new MRG32k3a());
//		gbm.setObservationTimes(1, Global.nRounds);
//	}
	
	public Stock(Experiment experiment){
		this.experiment = experiment;
		initialize();
		
	}
	
	private void initialize(){
		this.id = nStocks;
		nStocks++;
		this.markets = new TreeSet<Market>(new MarketIdComparator());
		this.fundamentalPrice = new ArrayList<Long>(Collections.nCopies(this.experiment.nRounds, 0l));
		
		this.globalLowestBuy = new ArrayList<Long>(Collections.nCopies(this.experiment.nRounds, 0l));
		this.globalHighestBuy = new ArrayList<Long>(Collections.nCopies(this.experiment.nRounds, 0l));
		this.globalLowestSell = new ArrayList<Long>(Collections.nCopies(this.experiment.nRounds, Long.MAX_VALUE));
		this.globalHighestSell = new ArrayList<Long>(Collections.nCopies(this.experiment.nRounds, Long.MAX_VALUE));
		
		this.bestMarketInRound = new ArrayList<Market>();
		this.localSmallestOrderbookSpread = new ArrayList<Long>(Collections.nCopies(this.experiment.nRounds, Long.MAX_VALUE));
		this.fundamentalPrice.set(0,this.experiment.initialFundamentalPrice);
		this.globalLastTradedMarketOrderBuyPrice = this.experiment.initialFundamentalPrice - 10*(long) this.experiment.additivePriceNoiseStd;
		this.globalLastTradedMarketOrderSellPrice = this.experiment.initialFundamentalPrice + 10*(long) this.experiment.additivePriceNoiseStd;
		this.gbm = new GeometricBrownianMotion(this.experiment.initialFundamentalPrice, this.experiment.fundamentalBrownianMean, this.experiment.fundamentalBrownianVariance, new MRG32k3a());
		gbm.setObservationTimes(1, this.experiment.nRounds+1);
		World.addStock(this);
	}
	
	public void updateFundamentalPrice(){
		
		//		Implement Brownian motion
		int now = World.getCurrentRound();
		long price;
		if(this.experiment.randomWalkFundamental) {
			price = Math.round(this.gbm.nextObservation());			
		} else {
			price = this.experiment.initialFundamentalPrice;
		}
		if(price>Long.MAX_VALUE){
			World.errorLog.logError("In Stock.updateFundamenralPrice: price exceeded range for integers!", this.experiment);
		} else{
			try {
				this.fundamentalPrice.set(now, (long) Math.round(price));
			} catch(IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		
//		WarningLogger.logWarning("BROWNIAN MOTION NOT IMPLEMENTED YET!!!");
//		fundamentalPrice[time]++;
	}
	
	public ArrayList<Long> getFundamentalPriceHistory(){
		return this.fundamentalPrice;
	}
	
	public GeometricBrownianMotion getGBM(){
		return this.gbm;
	}
	
	public void registerWithMarket(Market market){
		this.markets.add(market);
	}
	
	public long getID() {
		return id;
	}
	
	public long getFundamentalPrice(int time){
		return this.fundamentalPrice.get(time);
	}
	
	public TreeSet<Market> getMarkets(){
		return this.markets;
	}
	
	

	public long getGlobalLastTradedMarketOrderBuyPrice() {
		return globalLastTradedMarketOrderBuyPrice;
	}

//	public void setGlobalLastTradedMarketOrderBuyPrice(long lastTradedMarketOrderBuyPrice) {
//		long midOfLastTradedPrices = this.globalLastTradedMarketOrderSellPrice - this.globalLastTradedMarketOrderBuyPrice;
//		if(midOfLastTradedPrices <= 0) {
//			Exception e = new Exception();
//			e.printStackTrace();
//			World.errorLog.logError("Price must not be negative!");
//		}
//		this.globalLastTradedMarketOrderBuyPrice = lastTradedMarketOrderBuyPrice;
//	}

	public long getGlobalLastTradedMarketOrderSellPrice() {
		return globalLastTradedMarketOrderSellPrice;
	}
	
	public void collectGlobalBestPricesAtEndOfRound() {
		/*
		 * This function collect the best buy/sell prices from all the orderbooks, and therefore it must be run
		 * AFTER the prices in each orderbook have been updated.
		 * The prices collected here are used to evaluate agent wealth in that particular round.
		 */
		long globalHighestBuyPrice = 0;
		long globalLowestBuyPrice = Long.MAX_VALUE;
		long globalHighestSellPrice = 0;
		long globalLowestSellPrice = Long.MAX_VALUE;
		
		long localBestBuyPrice = 0;
		long localBestSellPrice = Long.MAX_VALUE;
		long spread = Long.MAX_VALUE;
		long bestSpread = Long.MAX_VALUE;
		int now = World.getCurrentRound();
		Market bestMarket = null;
		for(Market market:this.markets) {
			Orderbook orderbook = market.getOrderbook(this);
			try {
				localBestBuyPrice = orderbook.getLocalBestBuyPriceAtEndOfRound(now);
				localBestSellPrice = orderbook.getLocalBestSellPriceAtEndOfRound(now);
				spread = localBestSellPrice - localBestBuyPrice;
			} catch(NoOrdersException e) {
				World.warningLog.logOnelineWarning(String.format("In round %s, Could not collect bestPrices for stock %s at market %s", now, this.id, market.getID()));
			} finally {
				globalLowestBuyPrice = (localBestBuyPrice < globalLowestBuyPrice) ? localBestBuyPrice : globalLowestBuyPrice;
				globalHighestBuyPrice = (localBestBuyPrice > globalHighestBuyPrice) ? localBestBuyPrice : globalHighestBuyPrice;
				globalHighestSellPrice = (localBestSellPrice > globalHighestSellPrice) ? localBestSellPrice : globalHighestSellPrice;
				globalLowestSellPrice = (localBestSellPrice < globalLowestSellPrice) ? localBestSellPrice : globalLowestSellPrice;
				if (spread < bestSpread) {
					bestSpread = spread;
					bestMarket = market;
				}
//				bestSpread = (spread < bestSpread) ? spread : bestSpread;
			}
		}
		this.globalLowestBuy.set(now, globalLowestBuyPrice);
		this.globalHighestBuy.set(now, globalHighestBuyPrice);
		this.globalLowestSell.set(now, globalLowestSellPrice);
		this.globalHighestSell.set(now, globalHighestSellPrice);
		this.localSmallestOrderbookSpread.set(now, bestSpread);
		this.bestMarketInRound.add(bestMarket);
	}
	
	
	
//	public long getStockValueAtRound(int round) {
//		/*
//		 * The stock value is the mid of the spread at the market where it was traded at the best price in that round.
//		 */
//		long stockValue = this.globalLowestSell.get(round) - this.globalHighestBuy.get(round);
//		if(stockValue<0) {
//			/*
//			 * Could this be because the prices are at different levels in different markets?
//			 */
//			World.errorLog.logError(String.format("In round %s, the stock value was negative, that is the global best buy was larger than the global best sell.", round));
//		}
//		return stockValue;
//	}
	
	public long getLocalSmallestOrderbookSpread(int round) {
		return this.localSmallestOrderbookSpread.get(round);
	}
	
	public long getGlobalLowestBuyPrice(int round) {
		return this.globalLowestBuy.get(round);
	}
	
	public long getGlobalHighestBuyPrice(int round) {
		return this.globalHighestBuy.get(round);
	}
	
	public long getGlobalLowestSellPrice(int round) {
		return this.globalLowestSell.get(round);
	}
	
	public long getGlobalHighestSellPrice(int round) {
		return this.globalHighestSell.get(round);
	}

//	public void setGlobalLastTradedMarketOrderSellPrice(long lastTradedMarketOrderSellPrice) {
//		long midOfLastTradedPrices = this.globalLastTradedMarketOrderSellPrice - this.globalLastTradedMarketOrderBuyPrice;
//		if(midOfLastTradedPrices <= 0) {
//			Exception e = new Exception();
//			e.printStackTrace();
//			World.errorLog.logError("Price must not be negative!");
//		}
//		this.globalLastTradedMarketOrderSellPrice = lastTradedMarketOrderSellPrice;
//	}

	
	
	
}