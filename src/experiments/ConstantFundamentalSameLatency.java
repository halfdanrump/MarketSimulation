package experiments;

import org.rosuda.JRI.Rengine;

import utilities.Utils;

import agent.GoldenCrossLimitOrderChartist;
import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;

public class ConstantFundamentalSameLatency extends Experiment {

	/*
	 * Override default setup parameters
	 */
	private int nAgents;
	private int fixedLatency;
	
	public static void main(String[] args) {
		System.out.println("Running experiment");
		String rootFolder = System.getProperty("rootFolder", "/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/");
		int fixedLatency = Integer.valueOf(System.getProperty("fixedLatency", "1"));
		int nSTOrdersPerRound = Integer.valueOf(System.getProperty("STOrdersPerRound", "1"));
		
		int nAgents = 0;
		Experiment e1 = new ConstantFundamentalSameLatency(rootFolder, nAgents, fixedLatency, nSTOrdersPerRound);
		
		
		try{
			Experiment.runExperiment(e1);
		} catch(java.lang.StackOverflowError e) {
			e1.getWorld().getOrderbookByNumbers(0, 0).printOrderbook();
		}
//		Experiment.runRscript(e1, re);
		
	}
	
	public ConstantFundamentalSameLatency(String rootFolder, int nAgents, int fixedLatency, int nSlowTraderOrdersPerRound) {
		super(rootFolder);
		this.nAgents = nAgents;
		this.fixedLatency = fixedLatency;
		this.nSlowTraderOrdersPerRound = nSlowTraderOrdersPerRound;
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
		String header = String.format("randomWalkFundamental,nHFTsPerGroup,nSlowTraderOrdersPerRound, fixedLatency");
		String values = String.format("%s,%s,%s,%s", 0, this.nHFTMarketMakers, this.nSlowTraderOrdersPerRound, this.fixedLatency);
		return header + "\n" + values;
		
	}
	
	@Override
	public void createAgents(){
		int[] latencyToMarkets = {this.fixedLatency};
		int group = 0;
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		for(int i=0; i<this.nHFTMarketMakers; i++) {
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
