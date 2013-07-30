package utilities;

import java.io.IOException;

import Experiments.ConstantFundamentalSameLatency;
import Experiments.ConstantFundamentalRandomLatency;
import Experiments.Experiment;
import Experiments.TwoGroups;
import environment.*;


public class Main {
	
	public static void main(String[] args){
		
		
//		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/TwoGroups/";
//		Experiment e = new TwoGroups(logRootFolder);
//		e.runExperiment(e);
		String logRootFolder;
		int nAgents;
		int nSTOrdersPerRound;
		
		
		logRootFolder = String.format("/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalSameLatency/");
		nAgents = 71	;
		int latency = 10;
		nSTOrdersPerRound = 20;
		Experiment e1 = new ConstantFundamentalSameLatency(logRootFolder, nAgents, latency, nSTOrdersPerRound);
		e1.runExperiment(e1);

//		logRootFolder = String.format("/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalRandomLatency/");
//		nAgents = 500	;
//		int minLatency = 1;
//		int maxLatency = 10;
//		nSTOrdersPerRound = 20;
//		Experiment e2 = new ConstantFundamentalRandomLatency(logRootFolder, nAgents,nSTOrdersPerRound, minLatency, maxLatency);
//		e2.runExperiment(e2);
		
		
		
//		ProcessBuilder pb = new ProcessBuilder("/Library/Frameworks/R.framework/Resources/bin/Rscript", "/Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/Rscripts/ConstantFundamentalSameLatency.r");
//		pb.redirectErrorStream(true);
//		try {
//			Process p = pb.start();
//			System.out.println("asdasd");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		try {
//			System.out.println("ASDASD");
//			Runtime.getRuntime().exec("/Library/Frameworks/R.framework/Resources/bin/Rscript ConstantFundamentalSameLatency.r /Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/Rscripts/ConstantFundamentalSameLatency.r");
//			Runtime.getRuntime().exec("mkdir /Users/halfdan/Dropbox/Waseda/Research/Simulation/dataAnalysis/Rscripts/asd");
//			System.out.println("aASDASDASSD");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		
		
//		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalNoHFTs/";
//		Experiment e = new ConstantFundamentalNoHFTs(logRootFolder);
//		e.runExperiment(e);
		
//		String logRootFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/ConstantFundamentalRandomLatency/";
//		Experiment e = new ConstantFundamentalRandomLatency(logRootFolder);
//		e.runExperiment(e);
		
		System.out.println(String.format("Finished simulation in %s seconds", ((double) World.runTime)/1000f));
	}
	
}