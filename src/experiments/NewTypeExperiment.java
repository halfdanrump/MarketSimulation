package experiments;

import environment.Market;
import environment.Stock;

public class NewTypeExperiment extends Experiment {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Experiment e = new NewTypeExperiment("/Users/halfdan/Dropbox/Waseda/Research/MarketSimulation/");
		e.runExperiment();
	}

	public NewTypeExperiment(String rootFolder) {
		super(rootFolder, "test");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createAgents() {
		// TODO Auto-generated method stub
		int minLat = 5;
		int maxLat = 10;
		int minThink = 1;
		int maxThink = 1;
				
		
		int nSSMM = 35;
		int ssmmMinSpread = 2;
		int ssmmMaxSpread = 10;
		ExperimentUtils.makeHFTSingleStockMarketMakers(this, nSSMM, minLat, maxLat, minThink, maxThink, ssmmMinSpread, ssmmMaxSpread);
		
		int nSC = 150;
		int timeHorizonMin = 1000;
		int timeHorizonMax = 20000;
		long ticksBeforeReactingMin = 2;
		long ticksBeforeReactingMax = 5;
		long priceTickSizeMin = 1;
		long priceTickSizeMax = 5;
		int waitTimeBetweenTradingMin = 10;
		int waitTimeBetweenTradingMax = 100;
		ExperimentUtils.makeHFTSimpleChartists(this, nSC, minLat, maxLat, minThink, maxThink, timeHorizonMin, timeHorizonMax, ticksBeforeReactingMin, ticksBeforeReactingMax, priceTickSizeMin, priceTickSizeMax, waitTimeBetweenTradingMin, waitTimeBetweenTradingMax);

	}

	@Override
	public void createStocks() {
		// TODO Auto-generated method stub
		boolean isRandomWalk = false;
		Stock stock = new Stock(this, isRandomWalk);
		ExperimentUtils.setFundamentalToStepFunction(this, stock, -5, 10000);

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
