package Experiments;

import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;

public class ConstantFundamentalSameLatency extends Experiment {

	/*
	 * Override default setup parameters
	 */
	private int nAgents;
	private int fixedLatency;
	  
	
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
		this.nHFTsPerGroup = this.nAgents;
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
		String values = String.format("%s,%s,%s,%s", 0, this.nHFTsPerGroup, this.nSlowTraderOrdersPerRound, this.fixedLatency);
		return header + "\n" + values;
		
	}
	
	@Override
	public void createAgents(){
		int[] latencyToMarkets = {this.fixedLatency};
		int group = 0;
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		for(int i=0; i<this.nHFTsPerGroup; i++) {
			new SingleStockMarketMaker(stockIDs, marketIDs, latencyToMarkets, this.minimumSpread, group, this);
		}
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
