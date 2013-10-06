package experiments;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import environment.Market;
import environment.Stock;

import utilities.Parameter;

public class GenericExperiment extends Experiment {

	
	public static void main(String[] args) {
		String logFolder = Parameter.getAsString("logFolder");
		if(logFolder == null) {
			System.out.println("Please specify log folder. Aborting...");
			System.exit(1);
		}
		
		Experiment e = new GenericExperiment(logFolder);
		e.runExperiment();
		BufferedWriter out;
		
		/*
		 * Write dummy file that tells the Python script that the simulation finished without problems
		 */
		try {
			out = new BufferedWriter(new FileWriter(logFolder + "finished.txt"));
			out.write("Simulation finished!");
	        out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public GenericExperiment(String logFolder) {
		super(logFolder);
	}

	@Override
	public void createAgents() {
		
		ExperimentUtils.makeHFTSingleStockMarketMakers(this);
				
		ExperimentUtils.makeHFTFastMovingAverageChartists(this);
		 
	}
	
	

	@Override
	public void createStocks() {
		boolean isRandomWalk = false;
		Stock stock = new Stock(this, isRandomWalk);
		int shockSize = Integer.valueOf(System.getProperty("shockSize", "-10"));;
		ExperimentUtils.setFundamentalToStepFunction(this, stock, shockSize, 10000);

	}

	@Override
	public void createMarkets() {
	
		new Market(this);
	}

	@Override
	public void overrideExperimentSpecificParameters() {

	}

	@Override
	public String getParameterString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeMetaInformation() {
		// TODO Auto-generated method stub

	}


}
