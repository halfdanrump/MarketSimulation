package Experiments;

import utilities.Utils;
import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;

public class ConstantFundamentalSameLatency extends Experiment {

	/*
	 * Override default setup parameters
	 */
	private int nAgents;
	private int fixedLatency;
	
	public ConstantFundamentalSameLatency(String logRootFolder, int nAgents, int fixedLatency) {
		super();
		this.nAgents = nAgents;
		this.fixedLatency = fixedLatency;
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
		int[] latencyToMarkets = {this.fixedLatency};
		int group = 0;
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		for(int i=0; i<nAgents; i++) {
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
