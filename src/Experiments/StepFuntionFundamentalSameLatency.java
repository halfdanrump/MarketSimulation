package Experiments;

import java.util.ArrayList;

import environment.Stock;

public class StepFuntionFundamentalSameLatency extends ConstantFundamentalSameLatency {

	public StepFuntionFundamentalSameLatency(String rootFolder, int nAgents, int fixedLatency, int nSlowTraderOrdersPerRound) {
		super(rootFolder, nAgents, fixedLatency, nSlowTraderOrdersPerRound);
		// TODO Auto-generated constructor stub
		
		ArrayList<Long> fixedFundamental = new ArrayList<Long>();
		for(int i = 0; i<5000; i++) {
			fixedFundamental.add((long) Math.pow(10,7));
		}
		for(int i=5000; i<this.nTotalRounds + 1; i++) {
			fixedFundamental.add((long) (Math.pow(10, 7) + 5 * Math.pow(10, 4)));
		}
		for(Stock stock:this.getWorld().getStocks()) {
			stock.initializeFixedFundamental(fixedFundamental);
		}
	}

}
