package experiments;

import environment.Market;
import environment.Stock;

public class StepFunctionFundamentalWithChartists extends Experiment{

	public StepFunctionFundamentalWithChartists(String rootFolder) {
		super(rootFolder);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createAgents() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createStocks() {
		// TODO Auto-generated method stub
		new Stock(this);
		
	}

	@Override
	public void createMarkets() {
		// TODO Auto-generated method stub
		new Market(this);
		
	}

	@Override
	public void overrideExperimentSpecificParameters() {
		// TODO Auto-generated method stub
		
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
