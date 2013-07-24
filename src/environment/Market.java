package environment;

import java.util.HashMap;

import setup.MarketRules;

import agent.HFT;

public class Market implements MarketRules{
	private static int nMarkets = 0;
	private int id;
	private HashMap<Stock, Orderbook> orderbooksByStock;
	
	public Market(){
		initialize();
		this.id = nMarkets;
		nMarkets++;
	}
	
	private void initialize(){
		this.orderbooksByStock = new HashMap<Stock, Orderbook>();
		World.addMarket(this);
	}
	
	
	public void addOrderbook(Orderbook ob){
		this.orderbooksByStock.put(ob.getStock(), ob);
	}
	
	public int getID(){
		return this.id;
	}
	
	public long getDelayedBestSellPriceAtEndOfRound(HFT agent, Stock stock) throws NoOrdersException{
		int time = World.getCurrentRound() - agent.getLatency(this);
		try {
			Orderbook orderbook = this.orderbooksByStock.get(stock);
			return orderbook.getLocalBestSellPriceAtEndOfRound(time);		
		} catch (NoOrdersException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	public long getDelayedBestBuyPriceAtEndOfRound(HFT agent, Stock stock) throws NoOrdersException{
		int time = World.getCurrentRound() - agent.getLatency(this);
		try{
			return this.orderbooksByStock.get(stock).getLocalBestBuyPriceAtEndOfRound(time);
		} catch(NoOrdersException e){
			throw e;
		}
	}
	
	public Orderbook getOrderbook(Stock stock){
		return this.orderbooksByStock.get(stock);
	}


	
}
	