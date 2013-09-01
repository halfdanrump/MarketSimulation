package utilities;

import java.util.ArrayList;
import java.util.Random;

import setup.SimulationSetup;
public class Utils {

	
	
	
	public static void checkGlobalParameters(){
		/*
		 * Helper methods that checks if the parameters have impossible combinations.
		 * Exits if errors are found.
		 */
		boolean valid = true;
//		Global g = new Global();
		
		/*
		 * Checking market parameters.
		 */

		if(SimulationSetup.marketNames.length != SimulationSetup.nMarkets){
			System.out.println("ERROR: The list of market names is too long or short for the specified number of markets!");
			valid = false;
		}
		
		double sum = 0;
		for(double i:SimulationSetup.marketAgentShare){
			sum += i;
		}
		long s = (int) Math.round(sum*1000000);
		if(s != 1000000){
			System.out.println("ERROR: marketAgentShare must sum to 1!");
			valid = false;
		}
		
		/*
		 * Checking stock parameters.
		 */

		if(SimulationSetup.stockIDs.length != SimulationSetup.nStocks){
			System.out.println("ERROR: The list of stock names is too long or short for the specified number of stocks!");
			valid = false;
		}
		
		if(SimulationSetup.initialFundamental.length != SimulationSetup.nStocks){
			System.out.println("ERROR: The list of inital stock prices is too long or short for the specified number of stocks!");
			valid = false;
		}
		
		/*
		 * Checks that orderbook parameters are legal
		 */
		if(SimulationSetup.orderbookSpecification.length != SimulationSetup.nStocks){
			System.out.println("ERROR: orderbookSpecification has too many or few stocks");
			valid = false;
		}
		
		/*
		 * Exits if errors were found
		 */
		if(!valid){
			System.out.println("Errors in the parameter settings were found. Exitting...");
			System.exit(1);
		}
		
		
	}
	
	public static int getRandomUniformInteger(int minimum, int maximum){
		Random random = new Random();
		int number = (int) random.nextInt(maximum-minimum+1)+minimum;
		return number;
	}
	
	
	public static long getNonNegativeGaussianInteger(double mean, double std){
		Random random = new Random();
		return Math.abs((int) (random.nextGaussian() * std + mean));
	}
	
	public static long getGaussianInteger(double mean, double std){
		Random random = new Random();
		double noise = random.nextGaussian() * std + mean;
		return (int) Math.round(noise);
	}
	
	
	
//	public static HashMap<Market, Integer> getLatencyHashMap(Market[] markets, int[] latencies){
//		HashMap<Market, Integer> latencyMap = new HashMap<Market, Integer>();
//		if(latencies.length != markets.length){
//			World.errorLog.logError("In getLatencyHashMap: Number of given markets must be the same as number of specified latencies!");
//			System.exit(1);
//		}
//		for(int i = 0; i < markets.length; i++){
//			latencyMap.put(markets[i], latencies[i]);
//		}
//		return latencyMap;
//	}
	
	public static void initializeStringArrayWithEmptyStrings(ArrayList<String> arrayList, int capacity, String initialValue) {
		for(int i = 0; i<capacity; i++) {
			arrayList.add(initialValue);
		}
	}
	
	public static String convertArrayListToString(@SuppressWarnings("rawtypes") ArrayList arrayList) {
		return arrayList.toString().replace("[", "").replace("]", "").replace(" ", "");
	}
	
//	public static Order getRandomOrder(long minPrice, long maxPrice, long minVolume, long maxVolume, Order.Type type, Order.BuySell buysell){
//		long price = Utils.getRandomUniformInteger(minPrice, maxPrice);
//		long volume = Utils.getRandomUniformInteger(minVolume, maxVolume);
//		Order order = new Order(volume,price,type,buysell);
//		return order;
//	}
	
	

	


	
//	public static HashMap<Stock, Integer> getRandomPortfolio(int[] numbers){
//		HashMap<Stock, Integer> portfolio = new HashMap<Stock, Integer>();
//		int[] number = new int[3];
//		
//		for(Iterator<Stock> ite = World.getStocks().iterator(); ite.hasNext(); ){
//			
//		}
////		for(Stock stock:World.getStocks()){
////			portfolio.put(stock, numbers[])
////		}
//		return portfolio;
//	}
}
