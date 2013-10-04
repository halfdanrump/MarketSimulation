package agent;

import java.util.HashMap;

import environment.Message;
import environment.Order;
import environment.Orderbook;
import environment.Order.BuySell;
import experiments.Experiment;

public class FastMovingAverageChartist extends HFT {
//	private HashMap<Orderbook, Long> knownLaggedBestBuyPrices;
//	private HashMap<Orderbook, Long> knownLaggedBestSellPrices;
	private HashMap<Orderbook, Long> mostRecentBuyOrderPrice;
	private HashMap<Orderbook, Long> mostRecentSellOrderPrice;
	private HashMap<Orderbook, Long> sumBuy;
	private HashMap<Orderbook, Long> sumSell;

	private int timeHorizon;
	private long ticksBeforeReacting;
	private long aggresiveness;
	private int waitTimeBetweenTrading;
	private long orderVol;
	private int previousRoundCollectedInfo;
	private HashMap<Orderbook, Integer> previousRoundSubmittedOrder;
	
	public FastMovingAverageChartist(int[] stockIDs, int[] marketIDs, int[] latencies, Experiment experiment, int thinkingTime, int timeHorizon, long ticksBeforeReacting, long aggresiveness, int waitTimeBetweenTrading, long orderVol) {
		super(stockIDs, marketIDs, latencies, experiment, thinkingTime);
		// TODO Auto-generated constructor stub
		this.ticksBeforeReacting = ticksBeforeReacting;
		this.orderVol = orderVol;
		this.timeHorizon = timeHorizon;
		this.aggresiveness = aggresiveness;
		this.waitTimeBetweenTrading = waitTimeBetweenTrading;
//		this.knownLaggedBestBuyPrices = new HashMap<Orderbook, Long>();
//		this.knownLaggedBestSellPrices = new HashMap<Orderbook, Long>();
		this.mostRecentBuyOrderPrice = new HashMap<Orderbook, Long>();
		this.mostRecentSellOrderPrice = new HashMap<Orderbook, Long>();
		this.sumBuy = new HashMap<Orderbook, Long>();
		this.sumSell = new HashMap<Orderbook, Long>();
		this.previousRoundCollectedInfo = 0;
		this.previousRoundSubmittedOrder = new HashMap<Orderbook, Integer>();
		
		int now = this.getCurrentRound();
		for(Orderbook o:this.orderbooks) {
			long sb = 0;
			long ss = 0;
			for(int i = 0; i < this.timeHorizon; i++) {
				int time = now - this.timeHorizon;
				sb += o.getLocalBestBuyPriceAtEndOfRound(time);
				ss += o.getLocalBestSellPriceAtEndOfRound(time);
//				P.p(sb);
//				P.p(ss);
			}
			this.sumBuy.put(o, sb);
			this.sumSell.put(o, ss);
			this.previousRoundSubmittedOrder.put(o, 0);
		}
	}

	@Override
	public void collectMarketInformation(){
		int now = this.getCurrentRound();

		for(Orderbook o:this.orderbooks) {
			long buysum = this.sumBuy.get(o);
			long sellsum = this.sumSell.get(o);

			for(int i = this.previousRoundCollectedInfo - this.timeHorizon; i < now - this.timeHorizon; i++) {
				buysum -= o.getMarket().getDelayedBestBuyPriceAtEndOfRound(this, o.getStock());
				sellsum -= o.getMarket().getDelayedBestBuyPriceAtEndOfRound(this, o.getStock());
//				P.p(i);
			}
			
			for(int i = this.previousRoundCollectedInfo; i < now; i++) {
				buysum += o.getMarket().getDelayedBestBuyPriceAtEndOfRound(this, o.getStock());
				sellsum += o.getMarket().getDelayedBestBuyPriceAtEndOfRound(this, o.getStock());
//				P.p(i);
			}
			
			this.sumBuy.put(o, buysum);
			this.sumSell.put(o, sellsum);
			this.mostRecentBuyOrderPrice.put(o, o.getMarket().getDelayedBestBuyPriceAtEndOfRound(this, o.getStock()));
			this.mostRecentSellOrderPrice.put(o, o.getMarket().getDelayedBestSellPriceAtEndOfRound(this, o.getStock()));
			
			this.previousRoundCollectedInfo = now;
		}
	}

	@Override
	public boolean executeStrategyAndSubmit() {
		/*
		 * Calculate new sum
		 */
		int now = this.getCurrentRound();

		for(Orderbook o:this.orderbooks) {
			if(now >= this.previousRoundSubmittedOrder.get(o) + this.waitTimeBetweenTrading) {
				double avrBuy = this.sumBuy.get(o) / this.timeHorizon;
				double avrSell = this.sumSell.get(o) / this.timeHorizon;		
				long avrMidPrice = Math.round((avrBuy + avrSell)/2);
				long mostRecentMidPrice = Math.round((this.mostRecentBuyOrderPrice.get(o) + this.mostRecentSellOrderPrice.get(o))/2);
				
				BuySell buysell;
				long price;
				
				int tDelay = super.getTransmissionDelayToMarket(o.getMarket());
				
				
				if(mostRecentMidPrice > avrMidPrice + this.ticksBeforeReacting) {
					/*
					 * Agent thinks that there is an up-trend, so he puts out a buy order.
					 */
					buysell = BuySell.BUY;
					price = mostRecentMidPrice + this.aggresiveness;
					new Order(tDelay, now,0 ,this.orderVol, price, Order.Type.LIMIT, buysell, this, o, Message.TransmissionType.WITH_TRANSMISSION_DELAY, this.experiment);
					this.previousRoundSubmittedOrder.put(o,  now);
//				this.waitForNRounds(this.waitTimeBetweenTrading);
				} else if(mostRecentMidPrice < avrMidPrice - this.ticksBeforeReacting) {
					buysell = BuySell.SELL;
					price = mostRecentMidPrice - this.aggresiveness;
					new Order(tDelay, now, 0, this.orderVol, price, Order.Type.LIMIT, buysell, this, o, Message.TransmissionType.WITH_TRANSMISSION_DELAY, this.experiment);
					this.previousRoundSubmittedOrder.put(o, now);
//				this.waitForNRounds(this.waitTimeBetweenTrading);
				}
			}
		}
		
		
		
		return true;
	}

}
