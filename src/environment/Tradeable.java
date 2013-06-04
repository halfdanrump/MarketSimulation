package environment;

import java.util.ArrayList;

public interface Tradeable {
	void addToMarket();
	void removeFromMarket();
	
	void updateBestGLobalBuyPrice();
	void updateBestGLobalSellPrice();
	
	ArrayList<Market> getMarkets();
	ArrayList<Orderbook> getOrderbooks();
	ArrayList<Order> getBuyHistory();
	ArrayList<Order> getBuyHistory(int time);
	ArrayList<Order> getSellHistory();
	ArrayList<Order> getSellHistory(int time);
	int getBestGlobalBuyPrice(int time);
	int getBestGlobalSellPrice(int time);
	
	
}
