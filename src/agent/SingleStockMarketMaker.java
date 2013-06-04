package agent;

import java.util.HashMap;

import utilities.NoOrdersException;

import environment.Market;
import environment.Order;
import environment.OrderCancellation;
import environment.Orderbook;
import environment.Stock;
import environment.World;
import environment.Order.BuySell;
import environment.Order.Type;

public class SingleStockMarketMaker extends HFT implements SingleStockMarketMakerBehavior{
	private int minimumSpread;
	int fixedOrderVolume;
	private Stock stock;
	private int largestLatency;
	private int marketOrderLength;
	private HashMap<Market, Integer> knownMarketAsks;
	private HashMap<Market, Integer> knownMarketBids;

	/*
	 * -The agent monitors a single stock and tries to trade the stock
	 * simultaneously on one or several markets. -The agent market maker
	 * strategy: He tries to always have standing orders on both the buy side
	 * and the sell side. -The agent uses the "spread limited always follow"
	 * behavior: -When the buy price goes down, he drops his price to the new
	 * price. -When they buy price goes up, he increases his bid, as int as the
	 * spread does not get smaller than a certain limit. -When the sell price
	 * goes down, he decreases his ask, as int as the spread does not get
	 * smaller than a certain limit. -When the sell prices goes up, he increases
	 * his ask to the new price. -
	 */

	// public SingleStockMarketMaker(int wealth, int[] stocks, int[]
	// ownedStocks, int[] markets, int[] latencies, int minimumSpread) {
	// super(wealth, stocks, ownedStocks, markets, latencies);
	// if(stocks.length>1){
	// WarningLogger.logWarning("SingleStockMarketMaker should only trade a single stock, but more were specified! Using the first specified stock...");
	// }
	// this.stock = World.getStockByNumber(stocks[0]);
	// this.minimumSpread = minimumSpread;
	// this.fixedOrderVolume = Global.SingleStockMarketMakerTradeVolume;
	// this.marketOrderLength = Global.SingleStockMarketMakerMarketOrderLength;
	// this.initialize();
	// }

	public SingleStockMarketMaker(int[] stocks, int[] markets, int[] latencies,
			int minimumSpread) {
		super(stocks, markets, latencies);
		if (stocks.length > 1) {
			World.errorLog
					.logError("SingleStockMarketMaker should only trade a single stock, but more were specified! Using the first specified stock...");
		}
		this.stock = World.getStockByNumber(stocks[0]);
		this.minimumSpread = minimumSpread;
		this.fixedOrderVolume = tradeVolume;
		this.marketOrderLength = SingleStockMarketMakerBehavior.marketOrderLength;
		this.initialize();
	}

	private void initialize() {
		this.knownMarketAsks = new HashMap<Market, Integer>();
		this.knownMarketBids = new HashMap<Market, Integer>();

		this.largestLatency = 0;
		for (Market market : markets) {
			this.largestLatency = Math.max(this.largestLatency,
					this.latencyToMarkets.get(market));
		}
	}

	@Override
	public void storeMarketInformation() throws NoOrdersException {
		/*
		 * Method which takes care of putting the market information into the
		 * agent's internal data structores for storage Returns false if there
		 * was no price information to get from one or more markets, true
		 * otherwise
		 */
		int sellPrice, buyPrice;
		for (Market market : this.markets) {
			try {
				sellPrice = market.getDelayedBestSellPrice(this, this.stock);
				this.knownMarketAsks.put(market, sellPrice);
			} catch (NoOrdersException e) {
				this.requestMarketInformation();
				this.eventlog.logAgentAction(String.format("Agent %s requested new market information because there was no sell order at market %s. Wakes up in round %s",this.getID(),market.getID(), this.wakeupTime));
				throw e;
			}
			try {
				buyPrice = market.getDelayedBestBuyPrice(this, this.stock);
				this.knownMarketBids.put(market, buyPrice);
			} catch (NoOrdersException e) {
				this.hibernate();
				this.requestMarketInformation();
				this.eventlog.logAgentAction(String.format("Agent %s requested new market information because there was no sell order at market %s. Wakes up in round %s",this.getID(), market.getID(), this.wakeupTime));
				throw e;
			}

		}
	}

	public boolean executeStrategyAndSubmit() {
		Order standingBuyOrder, standingSellOrder;
		for (Market market : this.markets) {
			Orderbook orderbook = World.getOrderbookByObjects(stock, market);

			int arrivalTime = super.getArrivalTimeToMarket(market);
			int dispatchTime = World.getCurrentRound();
			int currentMarketBuyPrice = this.knownMarketBids.get(market);
			int currentMarketSellPrice = this.knownMarketAsks.get(market);
			int currentMarketSpread = currentMarketSellPrice
					- currentMarketBuyPrice;

			if (currentMarketSpread == 0) {
				World.errorLog.logError(String.format("SSMM Agent says: Spread at orderbook %s is zero", orderbook.getIdentifier()));
			} else if (currentMarketSpread < 0) {
				World.errorLog.logError(String.format("SSMM Agent says: Spread at orderbook %s is negative", orderbook.getIdentifier()));
			}

			/*
			 * Checks whether or not the current market spread is smaller than
			 * the agent's minimum spread
			 */
			boolean spreadViolation;
			if (this.minimumSpread - currentMarketSpread < 0) {
				spreadViolation = false;
			} else {
				spreadViolation = true;
			}

			if (!this.standingBuyOrders.containsKey(orderbook)) {
				/*
				 * If the agent has no standing BUY MARKET order he creates a
				 * new order. The price of the order is the current market BUY
				 * price, so later when the agents Has to decide whether or not
				 * to update his standing order, the standing order will be the
				 * order that he just submitted. Hence the price will be the
				 * same, and the agent will do nothing.
				 */
				int newBuyPrice = this.getNewBuyPrice(spreadViolation, currentMarketBuyPrice, currentMarketSellPrice);
				standingBuyOrder = new Order(arrivalTime, dispatchTime, this.marketOrderLength, this.fixedOrderVolume, newBuyPrice, Type.MARKET, BuySell.BUY, this, orderbook);
				this.submitOrder(standingBuyOrder);
				this.eventlog.logAgentAction(String.format("Agent %s submitted a new BUY order, id: %s.", this.getID(), standingBuyOrder.getID()));
			} else {
				standingBuyOrder = this.standingBuyOrders.get(orderbook);
			}

			if (!this.standingSellOrders.containsKey(orderbook)) {
				int newSellPrice = this.getNewSellPrice(spreadViolation, currentMarketBuyPrice, currentMarketSellPrice);
				standingSellOrder = new Order(arrivalTime, dispatchTime, this.marketOrderLength, this.fixedOrderVolume, newSellPrice, Type.MARKET, BuySell.SELL, this, orderbook);
				this.submitOrder(standingSellOrder);
				this.eventlog.logAgentAction(String.format("Agent %s submitted a new SELL order, id: %s", this.getID(), standingSellOrder.getID()));
			} else {
				standingSellOrder = this.standingSellOrders.get(orderbook);
			}

			/*
			 * buyPriceDiff is positive if the buy/BID prices have gone up, that
			 * is, if currentMarketBuy is higher than the price order of the BUY
			 * order that the agent has in the market. This likely means SMALLER
			 * spread.
			 */
			int buyPriceDiff = currentMarketBuyPrice
					- standingBuyOrder.getPrice();

			/*
			 * sellPriceDiff is positive if the sell/ASK prices have gone up,
			 * likely leading to LARGER spread
			 */
			int sellPriceDiff = currentMarketSellPrice
					- standingSellOrder.getPrice();

			if (buyPriceDiff == 0 && sellPriceDiff != 0) {
				this.updateSellSideOrder(dispatchTime, arrivalTime,
						currentMarketBuyPrice, currentMarketSellPrice,
						standingSellOrder, spreadViolation, orderbook);
			} else if (buyPriceDiff != 0 && sellPriceDiff == 0) {
				this.updateBuySideOrder(dispatchTime, arrivalTime,
						currentMarketBuyPrice, currentMarketSellPrice,
						standingBuyOrder, spreadViolation, orderbook);
			} else if (buyPriceDiff != 0 && sellPriceDiff != 0) {
				this.updateBothSideOrders(dispatchTime, arrivalTime,
						currentMarketSpread, currentMarketBuyPrice,
						currentMarketSellPrice, buyPriceDiff, sellPriceDiff,
						standingBuyOrder, standingSellOrder, spreadViolation,
						orderbook);
			}
		}
		return true;
	}

	private void updateBothSideOrders(int dispatchTime, int arrivalTime,
			int currentMarketSpread, int currentMarketBuyPrice,
			int currentMarketSellPrice, int buyPriceDiff, int sellPriceDiff,
			Order standingBuyOrder, Order standingSellOrder,
			boolean spreadViolation, Orderbook orderbook) {
		
		this.eventlog.logAgentAction(String.format("Agent %s updated both his standing orders, buy-id: %s, sell-id: %s", this.getID(), standingBuyOrder.getID(), standingSellOrder.getID()));

		int newBuyPrice, newSellPrice;
		if (spreadViolation) {
			newBuyPrice = currentMarketBuyPrice
					- (this.minimumSpread - currentMarketSpread)
					* (Math.abs(buyPriceDiff) / (Math.abs(buyPriceDiff) + Math
							.abs(sellPriceDiff)));
			newSellPrice = currentMarketSellPrice
					+ (this.minimumSpread - currentMarketSpread)
					* (Math.abs(sellPriceDiff) / (Math.abs(buyPriceDiff) + Math
							.abs(sellPriceDiff)));
		} else {
			newBuyPrice = currentMarketBuyPrice;
			newSellPrice = currentMarketSellPrice;
		}
		this.cancelOrder(new OrderCancellation(arrivalTime, dispatchTime,
				standingBuyOrder));
		this.cancelOrder(new OrderCancellation(arrivalTime, dispatchTime,
				standingSellOrder));
		Order newBuyOrder = new Order(arrivalTime, dispatchTime,
				this.marketOrderLength, this.fixedOrderVolume, newBuyPrice,
				Type.MARKET, BuySell.BUY, this, orderbook);
		Order newSellOrder = new Order(arrivalTime, dispatchTime,
				this.marketOrderLength, this.fixedOrderVolume, newSellPrice,
				Type.MARKET, BuySell.SELL, this, orderbook);
		this.submitOrder(newBuyOrder);
		this.submitOrder(newSellOrder);
	}

	private void updateSellSideOrder(int dispatchTime, int arrivalTime,
			int currentMarketBuyPrice, int currentMarketSellPrice,
			Order standingSellOrder, boolean spreadViolation,
			Orderbook orderbook) {
		this.eventlog.logAgentAction(String.format("Agent %s updated his standing SELL side order", this.getID()));
		this.cancelOrder(new OrderCancellation(dispatchTime, arrivalTime,
				standingSellOrder));
		int newSellPrice = this.getNewSellPrice(spreadViolation,
				currentMarketBuyPrice, currentMarketSellPrice);
		Order newSellOrder = new Order(arrivalTime, dispatchTime,
				this.marketOrderLength, this.fixedOrderVolume, newSellPrice,
				Type.MARKET, BuySell.SELL, this, orderbook);
		this.submitOrder(newSellOrder);
	}

	private void updateBuySideOrder(int dispatchTime, int arrivalTime, int currentMarketBuyPrice, int currentMarketSellPrice, Order standingBuyOrder, boolean spreadViolation, Orderbook orderbook) {
		this.eventlog.logAgentAction(String.format("Agent %s updated his standing BUY SIDE order, id: %s", this.getID(), standingBuyOrder.getID()));
		this.cancelOrder(new OrderCancellation(dispatchTime, arrivalTime,
				standingBuyOrder));
		int newBuyPrice = this.getNewSellPrice(spreadViolation,
				currentMarketBuyPrice, currentMarketSellPrice);
		Order newBuyOrder = new Order(arrivalTime, dispatchTime,
				this.marketOrderLength, this.fixedOrderVolume, newBuyPrice,
				Type.MARKET, BuySell.BUY, this, orderbook);
		this.submitOrder(newBuyOrder);
	}

	private int getNewSellPrice(boolean spreadViolation, int currentMarketBuyPrice, int currentMarketSellPrice) {
		int newSellPrice;
		if (spreadViolation) {
			newSellPrice = currentMarketBuyPrice + this.minimumSpread;
		} else {
			newSellPrice = currentMarketSellPrice;
		}
		if (newSellPrice < 0 | newSellPrice == Integer.MAX_VALUE
				| newSellPrice == 0) {
			Exception e = new Exception();
			e.printStackTrace();
		}
		return newSellPrice;
	}

	private int getNewBuyPrice(boolean spreadViolation,
			int currentMarketBuyPrice, int currentMarketSellPrice) {
		int newBuyPrice;
		if (spreadViolation) {
			newBuyPrice = currentMarketSellPrice - this.minimumSpread;
		} else {
			newBuyPrice = currentMarketBuyPrice;
		}
		if (newBuyPrice < 0 | newBuyPrice == Integer.MAX_VALUE
				| newBuyPrice == 0) {
			Exception e = new Exception();
			e.printStackTrace();
		}
		return newBuyPrice;
	}

	public int getWaitingTime() {
		return this.largestLatency;
	}

}
