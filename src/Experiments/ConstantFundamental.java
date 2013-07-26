package Experiments;

import utilities.Utils;
import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;

public class ConstantFundamental extends Experiment {

	/*
	 * Override default setup parameters
	 */
	
	
	public ConstantFundamental(String logRootFolder) {
		super();
		this.overrideDefaultParameters();
		super.initializeExperimentWithChangedParameters(logRootFolder, this);
		// TODO Auto-generated constructor stub
	}
	
	boolean isRandomWalk = true;

	@Override
	public void overrideDefaultParameters() {
		this.isRandomWalk = false;
	}
	
	@Override
	public void createAgents(){
		int nAgents = 10;
		int[] latencyToMarkets = {1};
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
