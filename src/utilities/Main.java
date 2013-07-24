package utilities;

import Experiments.Experiment;
import Experiments.TwoGroups;
import environment.*;

import setup.SimulationSetup;

public class Main {

	
	
	
	public static void main(String[] args){
		
		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/TwoGroups/";
		Experiment e = new TwoGroups(logRootFolder);
		e.runExperiment();
		
		
		System.out.println(String.format("Finished simulation in %s seconds", ((double) World.runTime)/1000f));
	}
	
}