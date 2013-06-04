package agent;

import environment.Order;

public interface Strategy {

	void receiveMarketInformation();
	Order.BuySell decideOrderSide();
	Order.Type decideOrderType();
	int decideMarketOrderLength();
	int decideTradeVolume();
	int decideTradePrice();
}
