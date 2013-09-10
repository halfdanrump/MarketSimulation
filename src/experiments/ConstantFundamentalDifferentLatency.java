package experiments;

import java.util.Random;

import org.rosuda.JRI.Rengine;

import utilities.Utils;

import agent.GoldenCrossLimitOrderChartist;
import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;

public class ConstantFundamentalDifferentLatency extends Experiment {

	/*
	 * Override default setup parameters
	 */
	private int nAgents;
	private int minimumLatency;
	private int maximumLatency;
	
	public static void main(String[] args) {
		System.out.println("Running experiment");
		String rootFolder = System.getProperty("rootFolder", "/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/");
		int nSTOrdersPerRound = Integer.valueOf(System.getProperty("STOrdersPerRound", "2"));
		int minimumLatency = Integer.valueOf(System.getProperty("minimumLatency", "2"));
		int maximumLatency = Integer.valueOf(System.getProperty("maximumLatency", "10"));
		
		int nAgents = 0;
		Experiment e1 = new ConstantFundamentalDifferentLatency(rootFolder, nAgents, nSTOrdersPerRound, minimumLatency, maximumLatency);
		Experiment.runExperiment(e1);
		
		
	}
	
	public ConstantFundamentalDifferentLatency(String rootFolder, int nAgents, int nSlowTraderOrdersPerRound, int minimumLatency, int maximumLatency) {
		super(rootFolder);
		this.nAgents = nAgents;
		this.nSlowTraderOrdersPerRound = nSlowTraderOrdersPerRound;
		this.minimumLatency = minimumLatency;
		this.maximumLatency = maximumLatency;
		this.overrideExperimentSpecificParameters();
		super.initializeExperimentWithChangedParameters(this);
		
		this.logFolder = String.format("%snStylizedOrders%s/",this.logRootFolder, nSlowTraderOrdersPerRound);
		this.graphFolder = String.format("%snStylizedOrders%s/",this.graphRootFolder, nSlowTraderOrdersPerRound);
		this.storeMetaInformation();
	}
	
	
	
	@Override
	public void overrideExperimentSpecificParameters() {
		this.randomWalkFundamental = false;
		this.nHFTMarketMakers = this.nAgents;
	}
	
	@Override
	public void storeMetaInformation() {
		String header = String.format("experimentName,graphFolder\n");
		String values = String.format("%s,%s", this.experimentName, this.graphFolder);
		super.meta.writeToFile(header + values); 
	}
	
	@Override
	public String getParameterString() {
		String header = String.format("randomWalkFundamental,nHFTsPerGroup,nSlowTraderOrdersPerRound,minimumLatency,maximumLatency");
		String values = String.format("%s,%s,%s,%s", 0, this.nHFTMarketMakers, this.nSlowTraderOrdersPerRound, this.minimumLatency, this.maximumLatency);
		return header + "\n" + values;
		
	}
	
	@Override
	public void createAgents(){
		int group = 0;
		int[] stockIDs = {0}; 
		int[] marketIDs = {0};
		int[] latencyToMarkets = new int[marketIDs.length];
		Random random = new Random();
		for(int i=0; i<this.nHFTMarketMakers; i++) {
			for(int j = 0; j<marketIDs.length; j++) {
				latencyToMarkets[j] = Utils.getRandomUniformInteger(this.hft_minimumLatency, this.hft_maximumLatency);
			}
			int thinkingTime = Utils.getRandomUniformInteger(this.hft_minimumThinkingTime, this.hft_maximumThinkingTime);
			new SingleStockMarketMaker(stockIDs, marketIDs, latencyToMarkets, this.ssmm_minimumSpread, this, thinkingTime);
		}
		
//		float aggressiveness = 1;
//		int longTermMA = 300;
//		int shortTermMA = 100;
//		int orderSize = 10;
//		int initialWaitTime = 1000;
//		
//		for(int i=0; i<this.nHFTsPerGroup; i++) {
//			new GoldenCrossLimitOrderChartist(aggressiveness, longTermMA, shortTermMA, orderSize, initialWaitTime, stockIDs, marketIDs, latencyToMarkets, group, this);
//		}
	}


	@Override
	public void createStocks() {
		new Stock(this);
	}

	@Override
	public void createMarkets() {
		new Market(this);
	}

}
