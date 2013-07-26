package Experiments;

import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;
import utilities.Utils;

public class TwoGroups extends Experiment{
	/*
	 * Override default setup parameters here
	 */
	
	
	
	public TwoGroups(String logRootFolder){
		super();
		this.overrideDefaultParameters();
		super.initializeExperimentWithChangedParameters(logRootFolder, this);
	}
	
	public void overrideDefaultParameters() {
		super.nRounds = 10000;
	}
	
	public void createAgents(){
		int nGroups = 2;
		int nAgentsInGroup = 25;
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		int[] latencies = new int[marketIDs.length];
		long minimumSpreadForAllAgents = super.minimumSpread;
		int[] minimumLatencyInGroup = {1, 10};
		int[] maximumLatencyInGroup = {10, 20};

		for(int group = 0; group < nGroups; group++) {
			/*
			 * Create agents within each group. Each group have different minimum and maximum latencies
			 */
			for(int agent=0; agent<nAgentsInGroup; agent++) {
				for(int market=0; market<marketIDs.length; market++) {
					/*
					 * Reusing the latencies array is OK, because the values are copied one by one when the agent
					 * is initialized.
					 */
					latencies[market] = Utils.getRandomUniformInteger(minimumLatencyInGroup[group], maximumLatencyInGroup[group]);
				}	
				new SingleStockMarketMaker(stockIDs, marketIDs, latencies, minimumSpreadForAllAgents, group, this);
			}
		}
		
	}
	
	public void createStocks(){
		new Stock(this);
	}
	
	public void createMarkets(){
		new Market();
	}

}
