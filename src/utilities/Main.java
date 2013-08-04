package utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import Experiments.ConstantFundamentalSameLatency;
import Experiments.Experiment;
import environment.*;


public class Main {
	
	public static void main(String[] args){
		String rootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/";
		System.out.println("Creating Rengine (with arguments)");
	    //If not started with --vanilla, funny things may happen in this R shell.
	    String[] Rargs = {"--vanilla"};
	    Rengine re = new Rengine(Rargs, false, null);
		
		int nAgents;
		int nSTOrdersPerRound;
		
		
		
		int fixedLatency = 1;
		nSTOrdersPerRound = 20;
//		int[] numberOfAgents = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300};
		int[] numberOfAgents = {40, 50, 60, 70, 80, 90, 100, 150, 200};
		ArrayList<Integer> k = new ArrayList<Integer>();
		for(int na:numberOfAgents) {
			Experiment e1 = new ConstantFundamentalSameLatency(rootFolder, na, fixedLatency, nSTOrdersPerRound);
			e1.runExperiment(e1);
			e1.runRscript(e1, re);
		}
//		logRootFolder = String.format("/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalRandomLatency/");
//		nAgents = 150;
//		int minLatency = 10;
//		int maxLatency = 50;
//		nSTOrdersPerRound = 20;
//		Experiment e2 = new ConstantFundamentalRandomLatency(logRootFolder, nAgents,nSTOrdersPerRound, minLatency, maxLatency);
//		e2.runExperiment(e2);
		
		
	    
		re.end();
		
	}
	
}