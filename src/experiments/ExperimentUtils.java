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
		float hft_latency_mu = Parameter.getAsFloat("hft_latency_mu");
		float hft_latency_s = Parameter.getAsFloat("hft_latency_s");
		float hft_think_mu = Parameter.getAsFloat("hft_think_mu");
		float hft_think_s = Parameter.getAsFloat("hft_think_s");
		
		
		int nAgents = Parameter.getAsInt("ssmm_nAgents");
		float ssmm_spread_mu = Parameter.getAsFloat("ssmm_spread_mu");
		float ssmm_spread_s = Parameter.getAsFloat("ssmm_spread_s");
		float ssmm_ordervol_mu = Parameter.getAsFloat("ssmm_ordervol_mu");
		float ssmm_ordervol_s = Parameter.getAsFloat("ssmm_ordervol_s");
		
		float ssmm_orderlength_mu = Parameter.getAsFloat("ssmm_orderlength_mu");
		float ssmm_orderlength_s = Parameter.getAsFloat("ssmm_orderlength_mu");
		
		int[] latencyToMarkets = {Utils.getNonNegativeGaussianInteger(hft_latency_mu, hft_latency_s)};
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		for(int i=0; i<nAgents; i++) {
			int thinkingTime = Utils.getNonNegativeGaussianInteger(hft_think_mu, hft_think_s);
			int minSpread = Utils.getNonNegativeGaussianInteger(ssmm_spread_mu, ssmm_spread_s);
			long orderVol = Utils.getNonNegativeGaussianLong(ssmm_ordervol_mu, ssmm_ordervol_s);
			int orderLength = Utils.getNonNegativeGaussianInteger(ssmm_orderlength_mu, ssmm_orderlength_s);
			new SingleStockMarketMaker(stockIDs, marketIDs, latencyToMarkets, minSpread, e, thinkingTime, orderVol, orderLength);
		}
	}
	
	
	public static void makeHFTFastMovingAverageChartists(Experiment e) {
		int nAgents = Parameter.getAsInt("sc_nAgents");
		
		float hft_latency_mu = Parameter.getAsFloat("hft_latency_mu");
		float hft_latency_s = Parameter.getAsFloat("hft_latency_s");
		float hft_think_mu = Parameter.getAsFloat("hft_think_mu");
		float hft_think_s = Parameter.getAsFloat("hft_think_s");
		
		float sc_timehorizon_mu = Parameter.getAsFloat("sc_timehorizon_mu");
		float sc_timehorizon_s = Parameter.getAsFloat("sc_timehorizon_s");
		float sc_ticksBeforeReacting_mu = Parameter.getAsFloat("sc_ticksBeforeReacting_mu");
		float sc_ticksBeforeReacting_s = Parameter.getAsFloat("sc_ticksBeforeReacting_s");
		float sc_priceTickSize_mu= Parameter.getAsFloat("sc_priceTickSize_mu");
		float sc_priceTickSize_s = Parameter.getAsFloat("sc_priceTickSize_s");
		float sc_waitTimeBetweenTrading_mu= Parameter.getAsFloat("sc_waitTimeBetweenTrading_mu");
		float sc_waitTimeBetweenTrading_s = Parameter.getAsFloat("sc_waitTimeBetweenTrading_s");
		float sc_ordervol_mu = Parameter.getAsFloat("sc_ordervol_mu");
		float sc_ordervol_s = Parameter.getAsFloat("sc_ordervol_s");
		
		int[] latencyToMarkets = {Utils.getNonNegativeGaussianInteger(hft_latency_mu, hft_latency_s)};
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		
		for(int i=0; i<nAgents; i++) {
			int thinkingTime = Utils.getNonNegativeGaussianInteger(hft_think_mu, hft_think_s);
			int timeHorizon = Utils.getNonNegativeGaussianInteger(sc_timehorizon_mu, sc_timehorizon_s);
			long ticksBeforeReacting = Utils.getNonNegativeGaussianLong(sc_ticksBeforeReacting_mu, sc_ticksBeforeReacting_s);
			long aggressiveness = Utils.getNonNegativeGaussianLong(sc_priceTickSize_mu, sc_priceTickSize_s);
			int waitTimeBetweenTrades = Utils.getNonNegativeGaussianInteger(sc_waitTimeBetweenTrading_mu, sc_waitTimeBetweenTrading_s);
			long orderVol = Utils.getNonNegativeGaussianLong(sc_ordervol_mu, sc_ordervol_s);
			new FastMovingAverageChartist(stockIDs, marketIDs, latencyToMarkets, e, thinkingTime, timeHorizon, ticksBeforeReacting, aggressiveness, waitTimeBetweenTrades, orderVol);
//			new FastMovingAverageChartist(stockIDs, marketIDs, latencyToMarkets, e, thinkingTime, ticksBeforeReacting, priceTickSize, waitTimeBetweenTrades);
		}
	}
}
