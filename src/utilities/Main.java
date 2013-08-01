package utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import Experiments.ConstantFundamentalSameLatency;
import Experiments.ConstantFundamentalRandomLatency;
import Experiments.Experiment;
import Experiments.TwoGroups;
import environment.*;


public class Main {
	
	public static void main(String[] args){
		System.out.println("Creating Rengine (with arguments)");
	    //If not started with --vanilla, funny things may happen in this R shell.
	    String[] Rargs = {"--vanilla"};
	    Rengine re = new Rengine(Rargs, false, null);
		
//		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/TwoGroups/";
//		Experiment e = new TwoGroups(logRootFolder);
//		e.runExperiment(e);
		String logRootFolder;
		String RscriptFilePath;
		int nAgents;
		int nSTOrdersPerRound;
		
		
		logRootFolder = String.format("/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalSameLatency/");
		RscriptFilePath = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/Rscripts/ConstantFundamentalSameLatency.r";
		int fixedLatency = 1;
		nSTOrdersPerRound = 20;
//		int[] numberOfAgents = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300};
		int[] numberOfAgents = {0};
		ArrayList<Integer> k = new ArrayList<Integer>();
		for(int na:numberOfAgents) {
			
			String logFolder = String.format("%snStylizedOrders=%s/",logRootFolder,nSTOrdersPerRound);
			System.out.println(logFolder);
			File f = new File(logFolder);
			f.mkdir();
			Experiment e1 = new ConstantFundamentalSameLatency(logFolder, na, fixedLatency, nSTOrdersPerRound);
			e1.runExperiment(e1);
//			re.eval(String.format("source('%s')", RscriptFilePath));
		}

//		logRootFolder = String.format("/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalRandomLatency/");
//		nAgents = 150;
//		int minLatency = 10;
//		int maxLatency = 50;
//		nSTOrdersPerRound = 20;
//		Experiment e2 = new ConstantFundamentalRandomLatency(logRootFolder, nAgents,nSTOrdersPerRound, minLatency, maxLatency);
//		e2.runExperiment(e2);
		
		
	    
		re.end();
		System.out.println(String.format("Finished simulation in %s seconds", ((double) World.runTime)/1000f));
	}
	
}