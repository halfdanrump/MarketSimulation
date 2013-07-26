package utilities;

import Experiments.ConstantFundamental;
import Experiments.Experiment;
import Experiments.TwoGroups;
import environment.*;


public class Main {
	
	public static void main(String[] args){
		
//		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/TwoGroups/";
//		Experiment e = new TwoGroups(logRootFolder);
//		e.runExperiment(e);
		
		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamental/";
		Experiment e = new ConstantFundamental(logRootFolder);
		e.runExperiment(e);
		
		System.out.println(String.format("Finished simulation in %s seconds", ((double) World.runTime)/1000f));
	}
	
}