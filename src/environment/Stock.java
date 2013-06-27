package environment;
import java.util.ArrayList;
import java.util.TreeSet;

import umontreal.iro.lecuyer.stochprocess.GeometricBrownianMotion;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import utilities.MarketIdComparator;
import log.StockLogger;
import setup.SimulationSetup;
import setup.TradeableAsset;
import environment.World;

public class Stock implements TradeableAsset{ 
//	private World world;
	private long id;
	private ArrayList<Long> fundamentalPrice;
	private GeometricBrownianMotion gbm;
	private static long nStocks = 0;
//	private TreeSet<Orderbook> tradedOn;
	private TreeSet<Market> markets;
	public StockLogger roundBasedDatalog;
	public StockLogger transactionBasedDataLog;
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
	
	public Stock(){
		initialize();
		long initialFundamentalPrice = TradeableAsset.initialPrice;
		this.fundamentalPrice.add(initialFundamentalPrice);
		this.gbm = new GeometricBrownianMotion(initialFundamentalPrice, fundamentalBrownianMean, fundamentalBrownianVariance, new MRG32k3a());
		gbm.setObservationTimes(1, SimulationSetup.nRounds+1);
	}
	
	private void initialize(){
		this.id = nStocks;
		nStocks++;
		this.fundamentalPrice = new ArrayList<Long>(SimulationSetup.nRounds);
		this.markets = new TreeSet<Market>(new MarketIdComparator());
		World.addStock(this);
	}
	
	public void updateFundamentalPrice(){
		
		//		Implement Brownian motion
		long price = Math.round(this.gbm.nextObservation());
		if(price>Integer.MAX_VALUE){
			World.errorLog.logError("In Stock.updateFundamenralPrice: price exceeded range for integers!");
		} else{
			this.fundamentalPrice.add((long) Math.round(price));
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
	
	public ArrayList<Long> getFundamentalPrice(){
		return this.fundamentalPrice;
	}
	
	public long getFundamentalPrice(int time){
		return this.fundamentalPrice.get(time);
	}
	
	public TreeSet<Market> getMarkets(){
		return this.markets;
	}

	public Integer getEstimatedTradedPriceFromEndOfCurrentRound() {
		return 0;
	
	}
	
	
}