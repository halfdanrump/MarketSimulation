package environment;
import java.util.ArrayList;
import java.util.TreeSet;

import umontreal.iro.lecuyer.stochprocess.GeometricBrownianMotion;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import utilities.MarketIdComparator;
import utilities.Utils;
import log.StockLogger;
import setup.Global;
import setup.TradeableAsset;
import environment.World;

public class Stock implements TradeableAsset{ 
//	private World world;
	private int id;
	private ArrayList<Integer> fundamentalPrice;
	private GeometricBrownianMotion gbm;
	private static int nStocks = 0;
//	private TreeSet<Orderbook> tradedOn;
	private TreeSet<Market> markets;
	public StockLogger roundBasedDatalog;
	public StockLogger transactionBasedDataLog;
//	private int[] fundamentalPrice;
//	private int[] bestNBBOBID;
//	private int[] bestNBBOASK;
//	private ArrayList<Orderbook> tradedOn = new ArrayList<Orderbook>();
//	private Hashtable<String, Orderbook[]> orderbooksByStockID = new Hashtable<String, Orderbook[]>();
	
	
	public Stock(int initialFundamentalPrice, double fundamentalBrownianMean, double fundamentalBrownianVariance){
		initialize();
		this.fundamentalPrice.add(initialFundamentalPrice);
		this.gbm = new GeometricBrownianMotion(initialFundamentalPrice, fundamentalBrownianMean, fundamentalBrownianVariance, new MRG32k3a());
		gbm.setObservationTimes(1, Global.nRounds);
	}
	
	public Stock(){
		initialize();
		int initialFundamentalPrice = Utils.getRandomUniformInteger(100, 1000);
		this.fundamentalPrice.add(initialFundamentalPrice);
		this.gbm = new GeometricBrownianMotion(initialFundamentalPrice, fundamentalBrownianMean, fundamentalBrownianVariance, new MRG32k3a());
		gbm.setObservationTimes(1, Global.nRounds);
	}
	
	private void initialize(){
		this.id = nStocks;
		nStocks++;
		this.fundamentalPrice = new ArrayList<Integer>();
		this.markets = new TreeSet<Market>(new MarketIdComparator());
		World.addStock(this);
	}
	
	public void updateFundamentalPrice(){
		
		//		Implement Brownian motion
		long price = Math.round(this.gbm.nextObservation());
		if(price>Integer.MAX_VALUE){
			World.errorLog.logError("In Stock.updateFundamenralPrice: price exceeded range for integers!");
		} else{
			this.fundamentalPrice.add((int) Math.round(price));
		}
		
//		WarningLogger.logWarning("BROWNIAN MOTION NOT IMPLEMENTED YET!!!");
//		fundamentalPrice[time]++;
	}
	
	public ArrayList<Integer> getFundamentalPriceHistory(){
		return this.fundamentalPrice;
	}
	
	public GeometricBrownianMotion getGBM(){
		return this.gbm;
	}
	
	public void registerWithMarket(Market market){
		this.markets.add(market);
	}
	
	public int getID() {
		return id;
	}
	
	public ArrayList<Integer> getFundamentalPrice(){
		return this.fundamentalPrice;
	}
	
	public int getFundamentalPrice(int time){
		return this.fundamentalPrice.get(time);
	}
	
	public TreeSet<Market> getMarkets(){
		return this.markets;
	}
	
	
}