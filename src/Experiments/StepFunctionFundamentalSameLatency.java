package Experiments;

import java.util.ArrayList;

import environment.Stock;

public class StepFunctionFundamentalSameLatency extends ConstantFundamentalSameLatency {
	
	public static void main(String[] args) {
		String rootFolder = System.getProperty("rootFolder", "/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/");
		int fixedLatency = Integer.valueOf(System.getProperty("fixedLatency", "10"));
		int nSTOrdersPerRound = Integer.valueOf(System.getProperty("STOrdersPerRound", "1"));
		
		int nAgents = 0;
		Experiment e1 = new StepFunctionFundamentalSameLatency(rootFolder, nAgents, fixedLatency, nSTOrdersPerRound);
		Experiment.runExperiment(e1);
		
	}

	public StepFunctionFundamentalSameLatency(String rootFolder, int nAgents, int fixedLatency, int nSlowTraderOrdersPerRound) {
		super(rootFolder, nAgents, fixedLatency, nSlowTraderOrdersPerRound);
		// TODO Auto-generated constructor stub
		
		ArrayList<Long> fixedFundamental = new ArrayList<Long>();
		for(int i = 0; i<10000; i++) {
			fixedFundamental.add(this.initialFundamentalPrice);
		}
		for(int i=10000; i<this.nTotalRounds + 1; i++) {
			fixedFundamental.add(this.initialFundamentalPrice - 10);
		}
		for(Stock stock:this.getWorld().getStocks()) {
			stock.initializeFixedFundamental(fixedFundamental);
		}
	}

}
