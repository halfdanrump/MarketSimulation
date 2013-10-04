package agent;

import java.util.ArrayList;
import java.util.HashMap;

import environment.Message;
import environment.NoOrdersException;
import environment.Order;
import environment.Order.BuySell;
import environment.Orderbook;
import experiments.Experiment;

public class MovingAverageChartist extends HFT {
	private HashMap<Orderbook, ArrayList<Long>> knownLaggedBestBuyPrices;
	private HashMap<Orderbook, ArrayList<Long>> knownLaggedBestSellPrices;
	private HashMap<Orderbook, Long> mostRecentBuyOrderPrice;
	private HashMap<Orderbook, Long> mostRecentSellOrderPrice;
	private int timeHorizon;
	private long ticksBeforeReacting;
	private long priceTickSize;
	private int waitTimeBetweenTrading;
	private int nLags;
	private long orderVol;
	
	
	
	public MovingAverageChartist(int[] stockIDs, int[] marketIDs, int[] latencies, Experiment experiment, int thinkingTime, int timeHorizon, long ticksBeforeReacting, long priceTickSize, int waitTimeBetweenTrading, long orderVol) {
		super(stockIDs, marketIDs, latencies, experiment, thinkingTime);
		this.timeHorizon = timeHorizon;
		this.ticksBeforeReacting = ticksBeforeReacting;
		this.priceTickSize = priceTickSize;
		this.waitTimeBetweenTrading = waitTimeBetweenTrading;
		this.nLags = Math.round(this.timeHorizon / this.experiment.nRoundsBetweenSamples);
	}


	@Override
	public void collectMarketInformation(){
		this.knownLaggedBestBuyPrices = new HashMap<Orderbook, ArrayList<Long>>();
		this.knownLaggedBestSellPrices = new HashMap<Orderbook, ArrayList<Long>>();
		this.mostRecentBuyOrderPrice = new HashMap<Orderbook, Long>();
		this.mostRecentSellOrderPrice = new HashMap<Orderbook, Long>();
		
		int now = this.getCurrentRound();
		for(Orderbook o:this.orderbooks) {
			this.knownLaggedBestBuyPrices.put(o, new ArrayList<Long>(this.nLags));
			this.knownLaggedBestSellPrices.put(o, new ArrayList<Long>(this.nLags));
			for(int i=0; i<this.nLags; i++) {
				int time = now - this.timeHorizon + i*this.experiment.nRoundsBetweenSamples;
				long price = o.getLocalBestBuyPriceAtEndOfRound(time);
				this.knownLaggedBestBuyPrices.get(o).add(price);
				price = o.getLocalBestSellPriceAtEndOfRound(time);
				this.knownLaggedBestSellPrices.get(o).add(price);
			}			
			this.mostRecentBuyOrderPrice.put(o, o.getMarket().getDelayedBestBuyPriceAtEndOfRound(this, o.getStock()));
			this.mostRecentSellOrderPrice.put(o, o.getMarket().getDelayedBestSellPriceAtEndOfRound(this, o.getStock()));
		}

	}


	@Override
	public boolean executeStrategyAndSubmit() {
		// TODO Auto-generated method stub
		for(Orderbook o:this.orderbooks) {
			double avrBuy = 0;
			for(Long price:this.knownLaggedBestBuyPrices.get(o)) {
				avrBuy += price;
			}
			avrBuy /= this.nLags;
			
			double avrSell = 0;
			for(Long price:this.knownLaggedBestSellPrices.get(o)) {
				avrSell += price;
			}
			avrSell /= this.nLags;
			
			long avrMidPrice = Math.round((avrBuy + avrSell)/2);
			long mostRecentMidPrice = Math.round((this.mostRecentBuyOrderPrice.get(o) + this.mostRecentSellOrderPrice.get(o))/2);
			BuySell buysell;
			long price;
			
			int now = this.getCurrentRound();
			int tDelay = super.getTransmissionDelayToMarket(o.getMarket());
			
			
			
			if(mostRecentMidPrice > avrMidPrice + this.ticksBeforeReacting) {
				/*
				 * Agent thinks that there is an up-trend, so he puts out a buy order.
				 */
				buysell = BuySell.BUY;
				price = this.mostRecentBuyOrderPrice.get(o) + this.priceTickSize;
				new Order(tDelay, now,0 , this.orderVol, price, Order.Type.LIMIT, buysell, this, o, Message.TransmissionType.WITH_TRANSMISSION_DELAY, this.experiment);
				this.waitForNRounds(this.waitTimeBetweenTrading);
			} else if(mostRecentMidPrice < avrMidPrice - this.ticksBeforeReacting) {
				buysell = BuySell.SELL;
				price = this.mostRecentSellOrderPrice.get(o) - this.priceTickSize;
				new Order(tDelay, now, 0, this.orderVol, price, Order.Type.LIMIT, buysell, this, o, Message.TransmissionType.WITH_TRANSMISSION_DELAY, this.experiment);
				this.waitForNRounds(this.waitTimeBetweenTrading);
			}
		}
		
		
		return true;
	}
	

}
