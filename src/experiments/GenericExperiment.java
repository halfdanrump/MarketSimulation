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
		 /*
		  * All this parameter fetching could really be moved to the parent class... this way other experiments wouldn't have to repeat the same code.
		  */
//		int minLat = Parameter.getAsInt("minLat");
//		int maxLat = Parameter.getAsInt("maxLat");
//		
//
//		int minThink = Parameter.getAsInt("minThink");
//		int maxThink = Parameter.getAsInt("maxThink");
		
		
//		int nSSMM = Parameter.getAsInt("ssmm_nAgents");
//		int ssmmMinSpread = Parameter.getAsInt("ssmm_MinSpread");
//		int ssmmMaxSpread = Parameter.getAsInt("ssmm_MaxSpread");
//		int ssmmOrderVolMin = Parameter.getAsInt("ssmm_orderVolMin");
//		int ssmmOrderVolMax = Parameter.getAsInt("ssmm_orderVolMax");
		ExperimentUtils.makeHFTSingleStockMarketMakers(this);
		
//		int nSC = Parameter.getAsInt("sc_nAgents");
//		int timeHorizonMin = Parameter.getAsInt("sc_timeHorizonMin");
//		int timeHorizonMax = Parameter.getAsInt("sc_timeHorizonMax");
//		long ticksBeforeReactingMin = Parameter.getAsLong("sc_ticksBeforeReactingMin");
//		long ticksBeforeReactingMax = Parameter.getAsLong("sc_ticksBeforeReactingMax");
//		long priceTickSizeMin = Parameter.getAsLong("sc_priceTickSizeMin");
//		long priceTickSizeMax = Parameter.getAsLong("sc_priceTickSizeMax");
//		int waitTimeBetweenTradingMin = Parameter.getAsInt("sc_waitTimeBetweenTradingMin");
//		int waitTimeBetweenTradingMax = Parameter.getAsInt("sc_waitTimeBetweenTradingMax");
//		int scOrderVolMin = Parameter.getAsInt("sc_orderVolMin");
//		int scOrderVolMax = Parameter.getAsInt("sc_orderVolMax");
//		//ExperimentUtils.makeHFTMovingAverageChartists(this, nSC, minLat, maxLat, minThink, maxThink, timeHorizonMin, timeHorizonMax, ticksBeforeReactingMin, ticksBeforeReactingMax, priceTickSizeMin, priceTickSizeMax, waitTimeBetweenTradingMin, waitTimeBetweenTradingMax, scOrderVolMin, scOrderVolMax);
		
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
		// TODO Auto-generated method stub
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
