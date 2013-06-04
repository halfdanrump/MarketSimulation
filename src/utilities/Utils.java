package utilities;

import java.util.Random;

import setup.Global;
import environment.*;

import java.util.HashMap;
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

		if(Global.marketNames.length != Global.nMarkets){
			System.out.println("ERROR: The list of market names is too int or short for the specified number of markets!");
			valid = false;
		}
		
		double sum = 0;
		for(double i:Global.marketAgentShare){
			sum += i;
		}
		int s = (int) Math.round(sum*1000000);
		if(s != 1000000){
			System.out.println("ERROR: marketAgentShare must sum to 1!");
			valid = false;
		}
		
		/*
		 * Checking stock parameters.
		 */

		if(Global.stockIDs.length != Global.nStocks){
			System.out.println("ERROR: The list of stock names is too int or short for the specified number of stocks!");
			valid = false;
		}
		
		if(Global.initialFundamental.length != Global.nStocks){
			System.out.println("ERROR: The list of inital stock prices is too int or short for the specified number of stocks!");
			valid = false;
		}
		
		/*
		 * Checks that orderbook parameters are legal
		 */
		if(Global.orderbookSpecification.length != Global.nStocks){
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
		int number = random.nextInt(maximum-minimum+1)+minimum;
		return number;
	}
	
	public static int getNonNegativeGaussianInteger(double mean, double std){
		Random random = new Random();
		return Math.abs((int) (random.nextGaussian() * std + mean));
	}
	
	public static int getGaussianInteger(double mean, double std){
		Random random = new Random();
		double noise = random.nextGaussian() * std + mean;
		return (int) Math.round(noise);
	}
	
	
	
	public static HashMap<Market, Integer> getLatencyHashMap(Market[] markets, int[] latencies){
		HashMap<Market, Integer> latencyMap = new HashMap<Market, Integer>();
		if(latencies.length != markets.length){
			World.errorLog.logError("In getLatencyHashMap: Number of given markets must be the same as number of specified latencies!");
			System.exit(1);
		}
		for(int i = 0; i < markets.length; i++){
			latencyMap.put(markets[i], latencies[i]);
		}
		return latencyMap;
	}
	
//	public static Order getRandomOrder(int minPrice, int maxPrice, int minVolume, int maxVolume, Order.Type type, Order.BuySell buysell){
//		int price = Utils.getRandomUniformInteger(minPrice, maxPrice);
//		int volume = Utils.getRandomUniformInteger(minVolume, maxVolume);
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
