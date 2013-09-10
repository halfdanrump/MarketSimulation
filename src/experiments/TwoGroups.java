package experiments;

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
		this.overrideExperimentSpecificParameters();
		super.initializeExperimentWithChangedParameters(logRootFolder, this);
	}
	
	public void overrideExperimentSpecificParameters() {
		this.nGroups = 2;
		this.nHFTsPerGroup = 25;
		this.randomWalkFundamental = false;
	}
	
	public String getParameterString() {
		String header = String.format("nGroups,nHFTsPerGroup, randomWalkFundamental");
		String values = String.format("%s,%s");
		return header + "\n" + values;
	}
	
	public void createAgents(){
		int nGroups = this.nGroups;
		int nAgentsInGroup = this.nHFTsPerGroup;
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
