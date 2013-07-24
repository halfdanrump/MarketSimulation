package setup;


public interface SingleStockMarketMakerBehavior extends HighFrequencyTradingBehavior {
	
	long tradeVolume = 20;
	int marketOrderLength = 200;
	
	boolean doesNotPlaceSellOrderWhenHoldingNegativeAmountOfStock = true;
}
