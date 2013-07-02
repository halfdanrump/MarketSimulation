package setup;

import environment.Order;

public interface MarketRules {
	public enum institutionPriceDeterminationTypes{
		LAST_PRICE;
	}
	
	final boolean allowsShortSelling = true;
	final boolean agentPaysWhenOrderIsFilledAfterSendingCancellation = true;
	final boolean agentMustBuyAllStocksAsSpecifiedInReceipt = true;
	final boolean agentMustSellAllStocksAsSpecifiedInReceipt = true;
	
//	final boolean institutionFillsEmptyOrderbook = true;
//	final institutionPriceDeterminationTypes institutionPriceDetermination =
//											institutionPriceDeterminationTypes.LAST_PRICE;
	final boolean marketFillsEmptyBook = true;
	final long orderVolumeWhenMarketFillsEmptyBook = 99;
	final Order.Type orderTypeWhenMarketFillsEmptyBook = Order.Type.MARKET;
	final int orderLengthWhenMarketFillsEmptyBook = 5;
}
