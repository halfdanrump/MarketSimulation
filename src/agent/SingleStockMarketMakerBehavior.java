package agent;

import setup.HighFrequencyTradingBehavior;

public interface SingleStockMarketMakerBehavior extends HighFrequencyTradingBehavior {
	
	int tradeVolume = 10;
	int marketOrderLength = 200;
	int spreadDecrease = 1; //Number of ticks that the agent decreases the spread when he submits order
}
