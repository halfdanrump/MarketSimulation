package Experiments;

import utilities.Utils;
import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;

public class ConstantFundamentalRandomLatency extends Experiment {

	/*
	 * Override default setup parameters
	 */
	private int nAgents;
	
	public ConstantFundamentalRandomLatency(String logRootFolder, int nAgents) {
		super();
		this.nAgents = nAgents;
		this.overrideDefaultParameters();
		super.initializeExperimentWithChangedParameters(logRootFolder, this);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public void overrideDefaultParameters() {
		this.isRandomWalk = false;
	}
	
	@Override
	public void createAgents(){
		int[] latencyToMarkets = new int[1];
		
		
		int group = 0;
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		
		int minimumLatency = 10;
		int maximumLatency = 20;
		for(int agent=0; agent<this.nAgents; agent++) {
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
