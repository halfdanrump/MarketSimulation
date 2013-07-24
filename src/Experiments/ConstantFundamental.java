package Experiments;

import utilities.Utils;
import agent.SingleStockMarketMaker;
import environment.Market;
import environment.Stock;

public class ConstantFundamental extends Experiment {

	public ConstantFundamental(String logRootFolder) {
		super(logRootFolder);
		// TODO Auto-generated constructor stub
	}
	
	boolean isRandomWalk = true;

	@Override
	public void createAgents(){
		int nGroups = 2;
		int nAgentsInGroup = 25;
		int[] stockIDs = {0}; 
		int[] marketIDs = {0}; 
		int[] latencies = new int[marketIDs.length];
		long minimumSpreadForAllAgents = (long) Math.pow(10, 6);
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


	@Override
	public void createStocks() {
		// TODO Auto-generated method stub
		new Stock();
	}

	@Override
	public void createMarkets() {
		// TODO Auto-generated method stub
		new Market();
	}

}
