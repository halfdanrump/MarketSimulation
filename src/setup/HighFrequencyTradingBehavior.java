package setup;

public interface HighFrequencyTradingBehavior {
	
	boolean keepOrderHistory = true;
	
	long emptyOrderbookWaitTime = 1;
	boolean randomStartStockAmount = false;
	long startStockAmount = 100;
	
	boolean randomStartWealth = false;
	long constantStartWealth = (int) Math.pow(10, 8);
	
	public static final long wealthMean = (int) Math.pow(10, 6);
	public static final long wealthStd = (int) Math.pow(10, 4);
	
	public static final int minimumLatency = 10;
	public static final int maximumLatency = 100;
	
}
