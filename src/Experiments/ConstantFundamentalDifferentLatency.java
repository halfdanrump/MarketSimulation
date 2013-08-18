package Experiments;

import utilities.Utils;
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
	  
	
	public ConstantFundamentalDifferentLatency(String rootFolder, int nAgents, int minimumLatency, int maximumLatency, int nSlowTraderOrdersPerRound) {
		super(rootFolder);
		this.nAgents = nAgents;
		this.minimumLatency = minimumLatency;
		this.maximumLatency = maximumLatency;
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
		String header = String.format("randomWalkFundamental,nHFTsPerGroup,nSlowTraderOrdersPerRound,minLat,maxLat");
		String values = String.format("%s,%s,%s,%s,%s", 0, this.nHFTsPerGroup, this.nSlowTraderOrdersPerRound, this.minimumLatency, this.maximumLatency);
		return header + "\n" + values;
		
	}
	
	@Override
	public void createAgents(){
		int[] latencyToMarkets = new int[1];
		int group = 0;
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		for(int agent=0; agent<this.nHFTsPerGroup; agent++) {
			latencyToMarkets[0] = Utils.getRandomUniformInteger(this.minimumLatency, this.maximumLatency);
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
