package utilities;

import Experiments.ConstantFundamentalSameLatency;
import Experiments.ConstantFundamentalNoHFTs;
import Experiments.ConstantFundamentalRandomLatency;
import Experiments.Experiment;
import Experiments.TwoGroups;
import environment.*;


public class Main {
	
	public static void main(String[] args){
		
//		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/TwoGroups/";
//		Experiment e = new TwoGroups(logRootFolder);
//		e.runExperiment(e);
		
		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamental/";
		Experiment e = new ConstantFundamentalSameLatency(logRootFolder, 100, 1);
		e.runExperiment(e);
		
//		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalNoHFTs/";
//		Experiment e = new ConstantFundamentalNoHFTs(logRootFolder);
//		e.runExperiment(e);
		
//		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalRandomLatency/";
//		Experiment e = new ConstantFundamentalRandomLatency(logRootFolder);
//		e.runExperiment(e);
		
		System.out.println(String.format("Finished simulation in %s seconds", ((double) World.runTime)/1000f));
	}
	
}