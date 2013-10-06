package experiments;

import java.util.ArrayList;

import utilities.Parameter;
import utilities.Utils;
import agent.FastMovingAverageChartist;
import agent.SingleStockMarketMaker;

import environment.Stock;

public class ExperimentUtils {
	
	public static void setFundamentalToStepFunction(Experiment experiment, Stock stock, long priceChange, int changeTime) {
		if(changeTime > experiment.nTotalRounds) {
			System.out.println("Change time must be less than total number of rounds. Aborting!");
			System.exit(1);
		}
		ArrayList<Long> fixedFundamental = new ArrayList<Long>();
		for(int i = 0; i<changeTime; i++) {
			fixedFundamental.add(experiment.initialFundamentalPrice);
		}
		for(int i=changeTime; i<experiment.nTotalRounds + 1; i++) {
			fixedFundamental.add(experiment.initialFundamentalPrice + priceChange);
		}
		stock.initializeFixedFundamental(fixedFundamental);
	}
	
	public static void makeHFTSingleStockMarketMakers(Experiment e) {
		int minimumLatency = Parameter.getAsInt("minLat");
		int maximumLatency = Parameter.getAsInt("maxLat");
		

		int minimumThinkingTime = Parameter.getAsInt("minThink");
		int maximumThinkingTime = Parameter.getAsInt("maxThink");
		
		
		int nAgents = Parameter.getAsInt("ssmm_nAgents");
		int minSpreadMin = Parameter.getAsInt("ssmm_MinSpread");
		int minSpreadMax = Parameter.getAsInt("ssmm_MaxSpread");
		long orderVolMin = Parameter.getAsLong("ssmm_orderVolMin");
		long orderVolMax = Parameter.getAsLong("ssmm_orderVolMax");
		
		int orderLengthMin = Parameter.getAsInt("ssmm_orderVolMin");
		int orderLengthMax = Parameter.getAsInt("ssmm_orderVolMax");
		
		int[] latencyToMarkets = {Utils.getRandomUniformInteger(minimumLatency, maximumLatency)};
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		for(int i=0; i<nAgents; i++) {
			int thinkingTime = Utils.getRandomUniformInteger(minimumThinkingTime, maximumThinkingTime);
			int minSpread = Utils.getRandomUniformInteger(minSpreadMin, minSpreadMax);
			long orderVol = Utils.getRandomUniformLong(orderVolMin, orderVolMax);
			int orderLength = Utils.getRandomUniformInteger(orderLengthMin, orderLengthMax);
			new SingleStockMarketMaker(stockIDs, marketIDs, latencyToMarkets, minSpread, e, thinkingTime, orderVol, orderLength);
		}
	}
	
	
	public static void makeHFTFastMovingAverageChartists(Experiment e) {
		int minimumLatency = Parameter.getAsInt("minLat");
		int maximumLatency = Parameter.getAsInt("maxLat");

		int minimumThinkingTime = Parameter.getAsInt("minThink");
		int maximumThinkingTime = Parameter.getAsInt("maxThink");
		int nAgents = Parameter.getAsInt("sc_nAgents");
		int timeHorizonMin = Parameter.getAsInt("sc_timeHorizonMin");
		int timeHorizonMax = Parameter.getAsInt("sc_timeHorizonMax");
		long ticksBeforeReactingMin = Parameter.getAsLong("sc_ticksBeforeReactingMin");
		long ticksBeforeReactingMax = Parameter.getAsLong("sc_ticksBeforeReactingMax");
		long priceTickSizeMin = Parameter.getAsLong("sc_priceTickSizeMin");
		long priceTickSizeMax = Parameter.getAsLong("sc_priceTickSizeMax");
		int waitTimeBetweenTradingMin = Parameter.getAsInt("sc_waitTimeBetweenTradingMin");
		int waitTimeBetweenTradingMax = Parameter.getAsInt("sc_waitTimeBetweenTradingMax");
		int orderVolMin = Parameter.getAsInt("sc_orderVolMin");
		int orderVolMax = Parameter.getAsInt("sc_orderVolMax");
		
		int[] latencyToMarkets = {Utils.getRandomUniformInteger(minimumLatency, maximumLatency)};
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		for(int i=0; i<nAgents; i++) {
			int thinkingTime = Utils.getRandomUniformInteger(minimumThinkingTime, maximumThinkingTime);
			int timeHorizon = Utils.getRandomUniformInteger(timeHorizonMin, timeHorizonMax);
			long ticksBeforeReacting = Utils.getRandomUniformLong(ticksBeforeReactingMin, ticksBeforeReactingMax);
			long aggressiveness = Utils.getRandomUniformLong(priceTickSizeMin, priceTickSizeMax);
			int waitTimeBetweenTrades = Utils.getRandomUniformInteger(waitTimeBetweenTradingMin, waitTimeBetweenTradingMax);
			long orderVol = Utils.getRandomUniformLong(orderVolMin, orderVolMax);
			new FastMovingAverageChartist(stockIDs, marketIDs, latencyToMarkets, e, thinkingTime, timeHorizon, ticksBeforeReacting, aggressiveness, waitTimeBetweenTrades, orderVol);
//			new FastMovingAverageChartist(stockIDs, marketIDs, latencyToMarkets, e, thinkingTime, ticksBeforeReacting, priceTickSize, waitTimeBetweenTrades);
		}
	}
}
