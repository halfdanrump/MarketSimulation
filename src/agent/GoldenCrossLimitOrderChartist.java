package agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import utilities.Utils;

import Experiments.Experiment;
import environment.Market;
import environment.Message;
import environment.NoOrdersException;
import environment.Order;
import environment.Orderbook;
import environment.World;

public class GoldenCrossLimitOrderChartist extends HFT {
	private int longTermMALength;
	private int shortTermMALength;
	private int initialWaitTime;
	private int orderSize;
	private float aggressiveness;
	private HashMap<Orderbook, LinkedList<Long>> knownMidSpreads;
	private World world;
	
	
	public GoldenCrossLimitOrderChartist(float aggressiveness, int longTermMALength, int shortTermMALength, int orderSize, int initialWaitTime,  
			int[] stockIDs, int[] marketIDs, int[] latencies, int group, Experiment experiment, int thinkingTime) {
		super(stockIDs, marketIDs, latencies, group, experiment, thinkingTime);
		this.longTermMALength = longTermMALength;
		this.shortTermMALength = shortTermMALength;
		this.orderSize = orderSize;
		this.initialWaitTime = initialWaitTime;
		this.aggressiveness = aggressiveness;
		this.world = experiment.getWorld();
		/*
		 * Initialize knownMidSpreads data structure
		 */
		this.knownMidSpreads = new HashMap<Orderbook, LinkedList<Long>>();
		for(Orderbook ob:this.orderbooks) {
			System.out.println(ob.getIdentifier());
			this.knownMidSpreads.put(ob, new LinkedList<Long>());
		}
	}


	@Override
	public void collectMarketInformation() throws NoOrdersException {
		if(this.world.getCurrentRound() > this.initialWaitTime) {
			for(Orderbook ob:this.orderbooks) {
				try{
					long bestBuy = ob.getMarket().getDelayedBestBuyPriceAtEndOfRound(this, ob.getStock());
					long bestSell = ob.getMarket().getDelayedBestSellPriceAtEndOfRound(this, ob.getStock());
					long bestSpread = (bestBuy + bestSell) / 2;
					if(this.knownMidSpreads.get(ob).size() > this.longTermMALength) {
						/*
						 * remove the first element and add the new spread to the end
						 */
						this.knownMidSpreads.get(ob).remove();
					}					
					this.knownMidSpreads.get(ob).add(bestSpread);
				} catch(NoOrdersException e) {
					this.hibernate();
					this.requestMarketInformation();
				}
			}
		} else {
			/*
			 * Agent is still in his initial waiting phase.
			 */
		}

	}

	@Override
	public boolean executeStrategyAndSubmit() {
		// TODO Auto-generated method stub
		int nStandingOrders = this.standingBuyOrders.size() + this.standingSellOrders.size();
		Random random = new Random();
		for(Orderbook ob:this.orderbooks) {
			long sum = 0;
			ListIterator<Long> it = this.knownMidSpreads.get(ob).listIterator();
			while(it.hasNext()) {
				sum += it.next();
			}
			double longTermMA = sum / this.longTermMALength;
			
			sum = 0;
			int i = 0;
			it = this.knownMidSpreads.get(ob).listIterator();
			while(i < this.shortTermMALength) {
				sum += it.next();
				i++;
			}
			double shortTermMA = sum/this.shortTermMALength;
			System.out.println(String.format("STMA: %s, LTMA: %s", shortTermMA, longTermMA));

			int transmissionDelay = super.getTransmissionDelayToMarket(ob.getMarket());
			int dispatchTime = this.experiment.getWorld().getCurrentRound();
			if(shortTermMA > longTermMA) {
				/*
				 * The agent decide it's time to buy
				 */
				long buyPrice = this.knownMidSpreads.get(ob).getLast() + (long) (this.aggressiveness * transmissionDelay); 

				new Order(transmissionDelay, dispatchTime, 1, this.orderSize, buyPrice, Order.Type.LIMIT, Order.BuySell.BUY, this, ob, Message.TransmissionType.WITH_TRANSMISSION_DELAY, this.experiment);
			} else {
				/*
				 * The agent decide it's time to sell
				 */
				long sellPrice = this.knownMidSpreads.get(ob).getLast() + (long) (this.aggressiveness * transmissionDelay); 

				new Order(transmissionDelay, dispatchTime,
						1, this.orderSize, sellPrice, Order.Type.LIMIT, Order.BuySell.SELL, this, ob, Message.TransmissionType.WITH_TRANSMISSION_DELAY, this.experiment);				
			}
			nStandingOrders++;
		}
		return false;
	}


	@Override
	public void removeOrderWhenExpired(Order order) {
		// TODO Auto-generated method stub
		
	}
	
//	private long calculateAverageOverNPreviousRounds(Orderbook ob, int nRounds) {
//		int sum = 0;
//		for(int i=0; i<nRounds; i++) {
//			sum += this.knownMidSpreads.get(ob).
//		}
//	}
	
//	private long calculatePredictedPrice(Orderbook ob, double longTermMovingAverage) {
//		double rateOfGrowth = longTermMovingAverage / this.longTermMALength;
//		long predictedPrice = (long) (this.getLatency(ob.getMarket()) * rateOfGrowth);
//		System.out.println(String.format("Predicted price: %s", predictedPrice));
//		return predictedPrice;
//	}

}
