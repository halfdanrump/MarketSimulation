package agent;

import setup.HighFrequencyTradingBehavior;

public interface SingleStockMarketMakerBehavior extends HighFrequencyTradingBehavior {
	
	long tradeVolume = 10;
	int marketOrderLength = 200;
	long spreadDecrease = 1; //Number of ticks that the agent decreases the spread when he submits order
	
	boolean doesNotPlaceSellOrderWhenHoldingNegativeAmountOfStock = true;
}
