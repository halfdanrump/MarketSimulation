package Experiments;

import utilities.Utils;
import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;

public class ConstantFundamentalRandomLatency extends Experiment {

	/*
	 * Variables for storing values received when constructed
	 */
	private int minimumLatency;
	private int maximumLatency;
	
	public ConstantFundamentalRandomLatency(String logRootFolder, int nAgents, int nSlowTraderOrdersPerRound, int minimumLatency, int maximumLatency) {
		super();
		this.nHFTsPerGroup = nAgents;
		this.nSlowTraderOrdersPerRound = nSlowTraderOrdersPerRound;
		this.minimumLatency = minimumLatency;
		this.maximumLatency = maximumLatency;
		this.overrideExperimentSpecificParameters();
		super.initializeExperimentWithChangedParameters(logRootFolder, this);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public void overrideExperimentSpecificParameters() {
		this.randomWalkFundamental = false;
	}
	
	@Override
	public String getParameterString() {
		String header = String.format("randomWalkFundamental,nHFTsPerGroup,minLatency,maxLatency,nSTOrders");
		String values = String.format("%s,%s,%s,%s", this.randomWalkFundamental, this.nHFTsPerGroup, this.minimumLatency, this.maximumLatency, this.nSlowTraderOrdersPerRound);
		return header + "\n" + values;
	}
	
	@Override
	public void createAgents(){
		int[] latencyToMarkets = new int[1];
		
		
		int group = 0;
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		
		int minimumLatency = 10;
		int maximumLatency = 20;
		for(int agent=0; agent<this.nHFTsPerGroup; agent++) {
			latencyToMarkets[0] = Utils.getRandomUniformInteger(minimumLatency, maximumLatency);
			new SingleStockMarketMaker(stockIDs, marketIDs, latencyToMarkets, this.minimumSpread, group, this);
		}
	}


	@Override
	public void createStocks() {
		new Stock(this);
	}

	@Override
	public void createMarkets() {
		new Market();
	}

}
