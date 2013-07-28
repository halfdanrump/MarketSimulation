package Experiments;

import utilities.Utils;
import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;

public class ConstantFundamentalNoHFTs extends Experiment {

	/*
	 * Override default setup parameters
	 */
	
	
	public ConstantFundamentalNoHFTs(String logRootFolder) {
		super();
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
