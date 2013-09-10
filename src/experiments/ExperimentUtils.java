package experiments;

import java.util.ArrayList;

import utilities.Utils;
import agent.SimpleChartist;
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
	
	public static void makeHFTSingleStockMarketMakers(Experiment e, int nAgents, int minimumLatency, int maximumLatency, int minimumThinkingTime, int maximumThinkingTime, int minSpreadMin, int minSpreadMax) {
		int[] latencyToMarkets = {Utils.getRandomUniformInteger(minimumLatency, maximumLatency)};
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		for(int i=0; i<nAgents; i++) {
			int thinkingTime = Utils.getRandomUniformInteger(minimumThinkingTime, maximumThinkingTime);
			int minSpread = Utils.getRandomUniformInteger(minSpreadMin, minSpreadMax);
			new SingleStockMarketMaker(stockIDs, marketIDs, latencyToMarkets, minSpread, e, thinkingTime);
		}
	}
	
	public static void makeHFTSimpleChartists(Experiment e, int nAgents, int minimumLatency, int maximumLatency, int minimumThinkingTime, int maximumThinkingTime, int timeHorizonMin, int timeHorizonMax, long ticksBeforeReactingMin, long ticksBeforeReactingMax, long priceTickSizeMin, long priceTickSizeMax, int waitTimeBetweenTradingMin, int waitTimeBetweenTradingMax) {
		int[] latencyToMarkets = {Utils.getRandomUniformInteger(minimumLatency, maximumLatency)};
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		for(int i=0; i<nAgents; i++) {
			int thinkingTime = Utils.getRandomUniformInteger(minimumThinkingTime, maximumThinkingTime);
			int nLags = Utils.getRandomUniformInteger(timeHorizonMin, timeHorizonMax);
			long ticksBeforeReacting = Utils.getRandomUniformLong(ticksBeforeReactingMin, ticksBeforeReactingMin);
			long priceTickSize = Utils.getRandomUniformLong(priceTickSizeMin, priceTickSizeMax);
			int waitTimeBetweenTrades = Utils.getRandomUniformInteger(waitTimeBetweenTradingMin, waitTimeBetweenTradingMax);
			new SimpleChartist(stockIDs, marketIDs, latencyToMarkets, e, thinkingTime, nLags, ticksBeforeReacting, priceTickSize, waitTimeBetweenTrades);
		}
	}
}
