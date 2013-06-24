package agent;

import environment.Order;

public interface Strategy {

	void receiveMarketInformation();
	Order.BuySell decideOrderSide();
	Order.Type decideOrderType();
	long decideMarketOrderLength();
	long decideTradeVolume();
	long decideTradePrice();
}
