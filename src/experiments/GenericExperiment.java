package experiments;

import environment.Market;
import environment.Stock;

public class GenericExperiment extends Experiment {

	
	public static void main(String[] args) {
		String rootFolder = System.getProperty("rootFolder");
		String experimentName = System.getProperty("experimentName");
		if(rootFolder == null && experimentName == null) {
			System.out.println("Please specify root folder and experiment name");
		}
		
		Experiment e = new GenericExperiment(rootFolder, experimentName);
		e.runExperiment();
	}

	public GenericExperiment(String rootFolder, String experimentName) {
		super(rootFolder, experimentName);
	}

	@Override
	public void createAgents() {
		 /*
		  * All this parameter fetching could really be moved to the parent class... this way other experiments wouldn't have to repeat the same code.
		  */
		
		int nSTOrdersPerRound = Integer.valueOf(System.getProperty("nStOrderPerRound", "1"));
		int minimumLatency = Integer.valueOf(System.getProperty("minimumLatency", "2"));
		int maximumLatency = Integer.valueOf(System.getProperty("maximumLatency", "10"));
		
		int minLat = Integer.valueOf(System.getProperty("minLat", "5"));
		int maxLat = Integer.valueOf(System.getProperty("maxLat", "100"));
		int minThink = Integer.valueOf(System.getProperty("minThink", "1"));;
		int maxThink = Integer.valueOf(System.getProperty("maxThink", "1"));
				
		
		int nSSMM = Integer.valueOf(System.getProperty("ssmm_nAgents", "30"));;
		int ssmmMinSpread = Integer.valueOf(System.getProperty("ssmm_MinSpread", "2"));;
		int ssmmMaxSpread = Integer.valueOf(System.getProperty("ssmm_MaxSpread", "10"));
		ExperimentUtils.makeHFTSingleStockMarketMakers(this, nSSMM, minLat, maxLat, minThink, maxThink, ssmmMinSpread, ssmmMaxSpread);
		
		int nSC = Integer.valueOf(System.getProperty("sc_nAgents", "0"));;
		int timeHorizonMin = Integer.valueOf(System.getProperty("sc_timeHorizonMin", "1000"));
		int timeHorizonMax = Integer.valueOf(System.getProperty("sc_timeHorizonMax", "20000"));
		long ticksBeforeReactingMin = Integer.valueOf(System.getProperty("sc_ticksBeforeReactingMin", "2"));
		long ticksBeforeReactingMax = Integer.valueOf(System.getProperty("sc_ticksBeforeReactingMax", "10"));
		long priceTickSizeMin = Integer.valueOf(System.getProperty("sc_priceTickSizeMin", "1"));
		long priceTickSizeMax = Integer.valueOf(System.getProperty("sc_priceTickSizeMax", "5"));
		int waitTimeBetweenTradingMin = Integer.valueOf(System.getProperty("sc_waitTimeBetweenTradingMin", "10"));
		int waitTimeBetweenTradingMax = Integer.valueOf(System.getProperty("sc_waitTimeBetweenTradingMax", "100"));
		ExperimentUtils.makeHFTSimpleChartists(this, nSC, minLat, maxLat, minThink, maxThink, timeHorizonMin, timeHorizonMax, ticksBeforeReactingMin, ticksBeforeReactingMax, priceTickSizeMin, priceTickSizeMax, waitTimeBetweenTradingMin, waitTimeBetweenTradingMax);

	}

	@Override
	public void createStocks() {
		boolean isRandomWalk = false;
		Stock stock = new Stock(this, isRandomWalk);
		int shockSize = Integer.valueOf(System.getProperty("shockSize", "-5"));;
		ExperimentUtils.setFundamentalToStepFunction(this, stock, shockSize, 20000);

	}

	@Override
	public void createMarkets() {
		// TODO Auto-generated method stub
		new Market(this);
	}

	@Override
	public void overrideExperimentSpecificParameters() {
		// TODO Auto-generated method stub
		this.config.writeToFile("hum");
		this.config.writeToConsole("3");

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
