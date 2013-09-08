package utilities;

import org.rosuda.JRI.Rengine;

import Experiments.ConstantFundamentalDifferentLatency;
import Experiments.ConstantFundamentalSameLatency;
import Experiments.Experiment;
import Experiments.StepFunctionFundamentalSameLatency;


public class Main {
	
//	public static void main(String[] args){
//		String rootFolder = "/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/";
//		System.out.println("Creating Rengine (with arguments)");
//	    //If not started with --vanilla, funny things may happen in this R shell.
//	    String[] Rargs = {"--vanilla"};
//		Rengine re = new Rengine(Rargs, false, null);
//		
//		int nAgents;
//		int nSTOrdersPerRound;
//		int fixedLatency = Integer.valueOf(System.getProperty("fixedLatency", "1"));
//		nSTOrdersPerRound = 20;
////		Runtime runtime = Runtime.getRuntime();
//		Experiment e1 = new ConstantFundamentalSameLatency(rootFolder, 25, fixedLatency, nSTOrdersPerRound);
//		Experiment.runExperiment(e1);
////		Experiment e2 = new ConstantFundamentalDifferentLatency(rootFolder, 180, 10, 19, nSTOrdersPerRound);
////		Experiment.runExperiment(e2);
////		Experiment e3 = new SetFundamentalSameLatency(rootFolder, 100, fixedLatency, nSTOrdersPerRound);
////		Experiment.runExperiment(e3);
//		
//
//		
//		//		int[] numberOfAgents = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300};
////		int[] numberOfAgents = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
////		int[] numberOfAgents = {33, 34, 35, 36, 37};
////		ArrayList<Integer> k = new ArrayList<Integer>();
////		for(int na:numberOfAgents) {
////			if(na==36) {
////				System.out.println("Break!");
////			}
////			Experiment e1 = new ConstantFundamentalSameLatency(rootFolder, na, fixedLatency, nSTOrdersPerRound);
////			Experiment.runExperiment(e1);
////			Experiment.runRscript(e1, re);
////		}
////			re.end();
////		logRootFolder = String.format("/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalRandomLatency/");
////		nAgents = 150;
////		int minLatency = 10;
////		int maxLatency = 50;
////		nSTOrdersPerRound = 20;
////		Experiment e2 = new ConstantFundamentalRandomLatency(logRootFolder, nAgents,nSTOrdersPerRound, mi	nLatency, maxLatency);
////		e2.runExperiment(e2);
//		
//		
//	    
//		
//	}
//	
	
}