package setup;

public interface HighFrequencyTradingBehavior {
	
	boolean keepOrderHistory = true;
	
	int emptyOrderbookWaitTime = 1;
	boolean randomStartStockAmount = false;
	int startStockAmount = 100;
	
	boolean randomStartWealth = false;
	int constantStartWealth = (int) Math.pow(10, 6);
	
	public static final int wealthMean = (int) Math.pow(10, 6);
	public static final int wealthStd = (int) Math.pow(10, 4);
	
	public static final int minimumLatency = 10;
	public static final int maximumLatency = 100;
	
}
