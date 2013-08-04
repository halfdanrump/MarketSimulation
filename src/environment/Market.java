package environment;

import java.util.HashMap;


import Experiments.Experiment;
import agent.HFT;

public class Market{
	private static int nMarkets = 0;
	private int id;
	private HashMap<Stock, Orderbook> orderbooksByStock;
	private Experiment experiment;
	
	public Market(Experiment experiment){
		this.experiment = experiment;
		this.orderbooksByStock = new HashMap<Stock, Orderbook>();
		this.experiment.getWorld().addMarket(this);
		this.id = nMarkets;
		nMarkets++;
	}
	
	public void addOrderbook(Orderbook ob){
		this.orderbooksByStock.put(ob.getStock(), ob);
	}
	
	public int getID(){
		return this.id;
	}
	
	public long getDelayedBestSellPriceAtEndOfRound(HFT agent, Stock stock) throws NoOrdersException{
		int time = this.experiment.getWorld().getCurrentRound() - agent.getLatency(this);
		try {
			Orderbook orderbook = this.orderbooksByStock.get(stock);
			return orderbook.getLocalBestSellPriceAtEndOfRound(time);		
		} catch (NoOrdersException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	public long getDelayedBestBuyPriceAtEndOfRound(HFT agent, Stock stock) throws NoOrdersException{
		int time = this.experiment.getWorld().getCurrentRound() - agent.getLatency(this);
		try{
			return this.orderbooksByStock.get(stock).getLocalBestBuyPriceAtEndOfRound(time);
		} catch(NoOrdersException e){
			throw e;
		}
	}
	
	public Orderbook getOrderbook(Stock stock){
		return this.orderbooksByStock.get(stock);
	}

	public Experiment getExperiment()
	{
		return this.experiment;
	}
	
}
	