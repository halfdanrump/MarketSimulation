package agent;

import java.util.HashMap;

import utilities.NoOrdersException;

import environment.Market;
import environment.Message;
import environment.Order;
import environment.OrderCancellation;
import environment.Orderbook;
import environment.Stock;
import environment.World;

public class SingleStockMarketMaker extends HFT implements SingleStockMarketMakerBehavior{
	private long minimumSpread;
	long fixedOrderVolume;
	private Stock stock;
	private long largestLatency;
	private int marketOrderLength;
	private HashMap<Market, Long> knownMarketAsks;
	private HashMap<Market, Long> knownMarketBids;

	/*
	 * -The agent monitors a single stock and tries to trade the stock
	 * simultaneously on one or several markets. -The agent market maker
	 * strategy: He tries to always have standing orders on both the buy side
	 * and the sell side. -The agent uses the "spread limited always follow"
	 * behavior: -When the buy price goes down, he drops his price to the new
	 * price. -When they buy price goes up, he increases his bid, as long as the
	 * spread does not get smaller than a certain limit. -When the sell price
	 * goes down, he decreases his ask, as long as the spread does not get
	 * smaller than a certain limit. -When the sell prices goes up, he increases
	 * his ask to the new price. -
	 */

	// public SingleStockMarketMaker(long wealth, int[] stocks, int[]
	// ownedStocks, int[] markets, int[] latencies, long minimumSpread) {
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
			long minimumSpread, int group) {
		super(stocks, markets, latencies, group);
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
		this.knownMarketAsks = new HashMap<Market, Long>();
		this.knownMarketBids = new HashMap<Market, Long>();

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
		long sellPrice, buyPrice;
		for (Market market : this.markets) {
			try {
				sellPrice = market.getDelayedBestSellPriceAtEndOfRound(this, this.stock);
				this.knownMarketAsks.put(market, sellPrice);
			} catch (NoOrdersException e) {
				this.requestMarketInformation();
				this.eventlog.logAgentAction(String.format("Agent %s requested new market information because there was no sell order at market %s. Wakes up in round %s",this.getID(),market.getID(), this.wakeupTime));
				throw e;
			}
			try {
				buyPrice = market.getDelayedBestBuyPriceAtEndOfRound(this, this.stock);
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

			int transmissionDelay = super.getTransmissionDelayToMarket(market);
			int dispatchTime = World.getCurrentRound();
			long currentMarketBuyPrice = this.knownMarketBids.get(market);
			long currentMarketSellPrice = this.knownMarketAsks.get(market);
			long currentMarketSpread = currentMarketSellPrice - currentMarketBuyPrice;

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
				long newBuyPrice = this.getNewBuyPrice(spreadViolation, currentMarketBuyPrice, currentMarketSellPrice);
				standingBuyOrder = new Order(transmissionDelay, dispatchTime, this.marketOrderLength, this.fixedOrderVolume, newBuyPrice, Order.Type.MARKET, Order.BuySell.BUY, this, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
				this.submitOrder(standingBuyOrder);
				this.eventlog.logAgentAction(String.format("Agent %s submitted a new BUY order, id: %s.", this.getID(), standingBuyOrder.getID()));
			} else {
				standingBuyOrder = this.standingBuyOrders.get(orderbook);
			}

			if (!this.standingSellOrders.containsKey(orderbook)) {
				long newSellPrice = this.getNewSellPrice(spreadViolation, currentMarketBuyPrice, currentMarketSellPrice);
				standingSellOrder = new Order(transmissionDelay, dispatchTime, this.marketOrderLength, this.fixedOrderVolume, newSellPrice, Order.Type.MARKET, Order.BuySell.SELL, this, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
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
			long buyPriceDiff = currentMarketBuyPrice
					- standingBuyOrder.getPrice();

			/*
			 * sellPriceDiff is positive if the sell/ASK prices have gone up,
			 * likely leading to LARGER spread
			 */
			long sellPriceDiff = currentMarketSellPrice
					- standingSellOrder.getPrice();

			if (buyPriceDiff == 0 && sellPriceDiff != 0) {
				this.updateSellSideOrderPrice(dispatchTime, transmissionDelay,	currentMarketBuyPrice, currentMarketSellPrice,standingSellOrder, spreadViolation, orderbook);
			} else if (buyPriceDiff != 0 && sellPriceDiff == 0) {
				this.updateBuySideOrderPrice(dispatchTime, transmissionDelay,currentMarketBuyPrice, currentMarketSellPrice, standingBuyOrder, spreadViolation, orderbook);
			} else if (buyPriceDiff != 0 && sellPriceDiff != 0) {
				this.updateBothSideOrderPrices(dispatchTime, transmissionDelay, currentMarketSpread, currentMarketBuyPrice, currentMarketSellPrice, buyPriceDiff, sellPriceDiff, standingBuyOrder, standingSellOrder, spreadViolation, orderbook);
			}
		}
		return true;
	}

	private void updateBothSideOrderPrices(int dispatchTime, int transmissionDelay, long currentMarketSpread, long currentMarketBuyPrice, long currentMarketSellPrice, long buyPriceDiff, long sellPriceDiff,	Order standingBuyOrder, Order standingSellOrder,boolean spreadViolation, Orderbook orderbook) {
		
		this.eventlog.logAgentAction(String.format("Agent %s updated both his standing orders at market %s. Order buy-id: %s, sell-id: %s", this.getID(), orderbook.getMarket().getID(), standingBuyOrder.getID(), standingSellOrder.getID()));

		long newBuyPrice, newSellPrice;
		if (spreadViolation) {
			newBuyPrice = currentMarketBuyPrice - (this.minimumSpread - currentMarketSpread) * (Math.abs(buyPriceDiff) / (Math.abs(buyPriceDiff) + Math.abs(sellPriceDiff)));
			newSellPrice = currentMarketSellPrice + (this.minimumSpread - currentMarketSpread) * (Math.abs(sellPriceDiff) / (Math.abs(buyPriceDiff) + Math.abs(sellPriceDiff)));
		} else {
			newBuyPrice = currentMarketBuyPrice;
			newSellPrice = currentMarketSellPrice;
		}
		this.cancelOrder(new OrderCancellation(dispatchTime, transmissionDelay, standingBuyOrder));
		this.cancelOrder(new OrderCancellation(dispatchTime, transmissionDelay, standingSellOrder));
		this.confirmThatBuyOrderIsInAccordanceWithStrategy(transmissionDelay, dispatchTime, newBuyPrice, Order.Type.MARKET, Order.BuySell.BUY, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
		this.confirmThatSellOrderIsInAccordanceWithStrategy(transmissionDelay, dispatchTime, newSellPrice, Order.Type.MARKET, Order.BuySell.SELL, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
		
	}
	
	private void confirmThatSellOrderIsInAccordanceWithStrategy(int transmissionDelay, int dispatchTime, long newSellPrice, Order.Type orderType, Order.BuySell buysell, Orderbook orderbook, Message.TransmissionType transmissionType) {
		Stock stock = orderbook.getStock();
		long numberOfOwnedStockAfterSellingOrderIsFullfilled = this.numberOfStocksInStandingSellOrders.get(stock) - this.fixedOrderVolume; 
		if(numberOfOwnedStockAfterSellingOrderIsFullfilled < 0 & SingleStockMarketMakerBehavior.doesNotPlaceSellOrderWhenHoldingNegativeAmountOfStock) {
			Order newSellOrder = new Order(transmissionDelay, dispatchTime, this.marketOrderLength, this.fixedOrderVolume, newSellPrice, Order.Type.MARKET, Order.BuySell.SELL, this, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
			this.submitOrder(newSellOrder);
		} else {
			this.eventlog.logAgentAction(String.format("Agent would like to place a SELL order, but he could not because he would have %s stocks left if the order was completely filled.", numberOfOwnedStockAfterSellingOrderIsFullfilled));
		}
	}
	
	private void confirmThatBuyOrderIsInAccordanceWithStrategy(int transmissionDelay, int dispatchTime, long newBuyPrice, Order.Type orderType, Order.BuySell buysell, Orderbook orderbook, Message.TransmissionType transmissionType) {
		/*
		 * At present this does nothing apart from accepting the order as the agent has no policy about when not to submit buy orders
		 */
		Order newBuyOrder = new Order(transmissionDelay, dispatchTime, this.marketOrderLength, this.fixedOrderVolume, newBuyPrice, Order.Type.MARKET, Order.BuySell.BUY, this, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
		this.submitOrder(newBuyOrder);
	}

	private void updateSellSideOrderPrice(int dispatchTime, int transmissionDelay,	long currentMarketBuyPrice, long currentMarketSellPrice, Order standingSellOrder, boolean spreadViolation, Orderbook orderbook) {
		this.eventlog.logAgentAction(String.format("Agent %s updated his standing SELL side order", this.getID()));
		this.cancelOrder(new OrderCancellation(dispatchTime, transmissionDelay,
				standingSellOrder));
		long newSellPrice = this.getNewSellPrice(spreadViolation,
				currentMarketBuyPrice, currentMarketSellPrice);
		Order newSellOrder = new Order(transmissionDelay, dispatchTime, this.marketOrderLength, this.fixedOrderVolume, newSellPrice, Order.Type.MARKET, Order.BuySell.SELL, this, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
		this.submitOrder(newSellOrder);
	}

	private void updateBuySideOrderPrice(int dispatchTime, int transmissionDelay, long currentMarketBuyPrice, long currentMarketSellPrice, Order standingBuyOrder, boolean spreadViolation, Orderbook orderbook) {
		this.eventlog.logAgentAction(String.format("Agent %s updated his standing BUY SIDE order, id: %s", this.getID(), standingBuyOrder.getID()));
		this.cancelOrder(new OrderCancellation(dispatchTime, transmissionDelay,
				standingBuyOrder));
		long newBuyPrice = this.getNewSellPrice(spreadViolation,
				currentMarketBuyPrice, currentMarketSellPrice);
		Order newBuyOrder = new Order(transmissionDelay, dispatchTime, this.marketOrderLength, this.fixedOrderVolume, newBuyPrice, Order.Type.MARKET, Order.BuySell.BUY, this, orderbook, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
		this.submitOrder(newBuyOrder);
	}

	private long getNewSellPrice(boolean spreadViolation, long currentMarketBuyPrice, long currentMarketSellPrice) {
		long newSellPrice;
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

	private long getNewBuyPrice(boolean spreadViolation,
			long currentMarketBuyPrice, long currentMarketSellPrice) {
		long newBuyPrice;
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

	public long getWaitingTime() {
		return this.largestLatency;
	}

}
