package experiments;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import environment.Market;
import environment.Stock;

public class GenericExperiment extends Experiment {

	
	public static void main(String[] args) {
		String logFolder = System.getProperty("logFolder", "/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/logs/");
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
		try {
			int minLat = Integer.valueOf(System.getProperty("minLat"));
			
			int maxLat = Integer.valueOf(System.getProperty("maxLat"));
			int minThink = Integer.valueOf(System.getProperty("minThink"));;
			int maxThink = Integer.valueOf(System.getProperty("maxThink"));
			
			
			int nSSMM = Integer.valueOf(System.getProperty("ssmm_nAgents"));;
			int ssmmMinSpread = Integer.valueOf(System.getProperty("ssmm_MinSpread"));;
			int ssmmMaxSpread = Integer.valueOf(System.getProperty("ssmm_MaxSpread"));
			ExperimentUtils.makeHFTSingleStockMarketMakers(this, nSSMM, minLat, maxLat, minThink, maxThink, ssmmMinSpread, ssmmMaxSpread);
			
			int nSC = Integer.valueOf(System.getProperty("sc_nAgents"));;
			int timeHorizonMin = Integer.valueOf(System.getProperty("sc_timeHorizonMin"));
			int timeHorizonMax = Integer.valueOf(System.getProperty("sc_timeHorizonMax"));
			long ticksBeforeReactingMin = Integer.valueOf(System.getProperty("sc_ticksBeforeReactingMin"));
			long ticksBeforeReactingMax = Integer.valueOf(System.getProperty("sc_ticksBeforeReactingMax"));
			long priceTickSizeMin = Integer.valueOf(System.getProperty("sc_priceTickSizeMin"));
			long priceTickSizeMax = Integer.valueOf(System.getProperty("sc_priceTickSizeMax"));
			int waitTimeBetweenTradingMin = Integer.valueOf(System.getProperty("sc_waitTimeBetweenTradingMin"));
			int waitTimeBetweenTradingMax = Integer.valueOf(System.getProperty("sc_waitTimeBetweenTradingMax"));
			ExperimentUtils.makeHFTSimpleChartists(this, nSC, minLat, maxLat, minThink, maxThink, timeHorizonMin, timeHorizonMax, ticksBeforeReactingMin, ticksBeforeReactingMax, priceTickSizeMin, priceTickSizeMax, waitTimeBetweenTradingMin, waitTimeBetweenTradingMax);
		}
		catch(NumberFormatException e) {
			System.out.println(e.getStackTrace());
		} finally {
//			this.closeLogs();
		}
		
		

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
