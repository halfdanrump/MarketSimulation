package environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Random;

import agent.StylizedTrader;

import environment.Order.BuySell;

import log.OrderbookLogger;
import setup.Global;
import setup.MarketRules;
import utilities.NoOrdersException;
import utilities.OrderExpirationTimeComparator;
import utilities.orderPriceComparatorAscending;
import utilities.OrderPriceComparatorLowFirst;

public class Orderbook {
	private World world;
	private Stock stock;
	private Market market;

	private ArrayList<Order> unprocessedOrders;
	private ArrayList<OrderCancellation> unprocessedOrderCancellations;
	private PriorityQueue<Order> unfilledBuyOrders;
	private PriorityQueue<Order> unfilledSellOrders;
	private PriorityQueue<Order> marketOrders;
	private int[] bestSellPrice;
	private int[] bestBuyPrice;
	private int lastTradedMarketOrderBuyPrice;
	private int lastTradedMarketOrderSellPrice;

	public OrderbookLogger orderflowLog;

	// public Logger eventLog;

	// public Orderbook(){
	// initialize();
	// }

	public Orderbook(Stock stock, Market market) {
		initializeDataStructures();
		this.stock = stock;
		this.market = market;
		stock.registerWithMarket(market);
		this.initializeBookWithRandomOrders();
	}

	private void initializeDataStructures() {
		this.unprocessedOrders = new ArrayList<Order>();
		this.unprocessedOrderCancellations = new ArrayList<OrderCancellation>();
		this.unfilledBuyOrders = new PriorityQueue<Order>(10, Collections.reverseOrder(new orderPriceComparatorAscending()));
		this.unfilledSellOrders = new PriorityQueue<Order>(10, new OrderPriceComparatorLowFirst());
		this.marketOrders = new PriorityQueue<Order>(10, new OrderExpirationTimeComparator());
		this.bestSellPrice = new int[Global.nRounds]; // Best ASK price for each
		this.bestBuyPrice = new int[Global.nRounds]; // Best BID price for each
		
	}
	
	public void initializeBookWithRandomOrders() {
		int sellPrice;
		Order buyOrder = StylizedTrader.submitRandomMarketBuyOrder(this);
		System.out.println(buyOrder.getPrice());
		while(true) {
			sellPrice = StylizedTrader.getStylizedTraderEstimatedPrice(this.getStock());
			System.out.println(sellPrice);
			if(sellPrice > buyOrder.getPrice()) {
				int volume = StylizedTrader.getStylizedTraderOrderVolume();
				int now = World.getCurrentRound();
				Order order = new Order(now, now, 1000, volume, sellPrice, Order.Type.MARKET, Order.BuySell.SELL, null, this);
				this.receiveOrder(order);
				break;
			}
			
		}
		this.lastTradedMarketOrderBuyPrice = buyOrder.getPrice();
		this.lastTradedMarketOrderSellPrice = sellPrice;
		this.processAllNewOrders();
		
	}
	
	public void receiveOrder(Order order) {
		this.unprocessedOrders.add(order);
	}

	public void receieveOrderCancellation(OrderCancellation update) {
		this.unprocessedOrderCancellations.add(update);
	}

	public int expireOrders() {
		int nExpiredOrders = 0;
		try {
			while (marketOrders.peek().getExpirationTime() == World.getCurrentRound()) {
				removeOrderFromBook(marketOrders.peek());
				nExpiredOrders++;
			}
		} catch (NullPointerException e) {

		}
		return nExpiredOrders;
	}
	
	private void handleEmptyBook(Order.BuySell buysell) {
		/*
		 * When the book is empty, the market rules determine what will happen
		 */
		this.orderflowLog.logEventNoMarketOrders(Order.BuySell.SELL);
		if(MarketRules.marketFillsEmptyBook) {
			this.market.submitOrderAtLastTradedPrice(this, buysell);
		} else {
			World.errorLog.logError(String.format("Orderbook %s was empty on the %s side, but the market rules are such that the book will remain empty", this.getIdentifier(), buysell));
		}
	}

	// public void processAllNewOrderUpdates(){
	// /*
	// * Updating orders work by first removing the order form the orderbook,
	// then adding a "new" order
	// * with the updated price and volume. OrderUpdates are processed in random
	// order to ensure fairness.
	// */
	// Order oldOrder, updatedOrder;
	// OrderCancellation update;
	// int index;
	// Random random = new Random();
	// while(this.unprocessedOrderCancellations.size() > 0){
	// index = random.nextInt(this.unprocessedOrderCancellations.size());
	// update = this.unprocessedOrderCancellations.remove(index);
	// oldOrder = update.getOrder();
	// this.removeOrderFromBook(oldOrder);
	//
	// updatedOrder = Order.getOrderFromUpdate(update);
	//
	//
	// // newOrder = this.unprocessedOrders.remove(index);
	// this.processNewOrder(updatedOrder);
	// }
	// this.updateBestPrices();
	// }

	public void processAllNewOrders() {
		/*
		 * Methods that wraps around ProcessNewOrder and ensures that new orders
		 * are processed in random order.
		 */
		Order newOrder;
		int index;
		Random random = new Random();
		while (this.unprocessedOrders.size() > 0) {
			index = random.nextInt(this.unprocessedOrders.size());
			newOrder = this.unprocessedOrders.remove(index);
			this.processNewOrder(newOrder);
		}
		this.updateBestPrices();
	}

	private void processNewOrder(Order newOrder) {
		/*
		 * The methods processes a single new order. The new order is entered in
		 * the book if there is no price matching order. If there is a price
		 * matching order with a full or partial volume match, a transaction is
		 * executed. If the matching market order is smaller than the new order,
		 * it is removed
		 */
		Order matchingOrder;

		this.orderflowLog.logEventProcessNewOrder(newOrder);
		while (true) {
			if (newOrder.getOrderbookSideVolume() == 0) {
				/*
				 * If the new order has been completely filled, don't add it to
				 * the book.
				 */
				break;
			}
			matchingOrder = this.getMatchingOrder(newOrder);
			if (matchingOrder == null) {
				/*
				 * Add the order to the book if there is no (longer) any
				 * matching order, and proceed to the next order by breaking the
				 * while loop.
				 */
				this.addOrderToBook(newOrder);
				break;
			} else {
				this.executeTrade(matchingOrder, newOrder);
			}

		}
	}

	private void executeTrade(Order matchingOrder, Order newOrder) {
		/*
		 * Executes a trade between an existing market order (matchingOrder) and
		 * a new market or limit order (newOrder). Creates receipts for both
		 * sides of the transaction and pushes to world receipt queue.
		 */
		TransactionReceipt receipt;
		int tradeVolume = Math.min(matchingOrder.getOrderbookSideVolume(), newOrder.getOrderbookSideVolume());
		int tradePrice = this.determineTransactionPrice(matchingOrder, newOrder);
		int tradeTotal = tradeVolume * tradePrice;

		/*
		 * Update the last traded price
		 */
		this.updateLastTradedPrices(matchingOrder);
		
		/*
		 * Only issue a receipt if the trader was a "registered" trader (that
		 * is, a trader that we care about).
		 */
		if (!(matchingOrder.getOwner() == null)) {
			receipt = new TransactionReceipt(matchingOrder, tradeVolume,tradePrice, tradeTotal);
			this.orderflowLog.logEventTransaction(receipt);
		}
		if (!(newOrder.getOwner() == null)) {
			receipt = new TransactionReceipt(newOrder, tradeVolume, tradePrice, tradeTotal);
			this.orderflowLog.logEventTransaction(receipt);
		}

		/*
		 * Log the event in the orderbook log
		 */
		this.orderflowLog.logEventUpdateOrderVolume(newOrder, tradeVolume);
		matchingOrder.updateOrderbookSideVolumeByDifference(-tradeVolume);
		this.orderflowLog.logEventUpdateOrderVolume(matchingOrder, tradeVolume);
		newOrder.updateOrderbookSideVolumeByDifference(-tradeVolume);

		/*
		 * Remove the order if there was a complete match (remaining order volume was zero)
		 */
		if (matchingOrder.getOrderbookSideVolume() == 0) {
			this.removeOrderFromBook(matchingOrder);
		}
		
		/*
		 * Handle the situation where one side of the orderbook is empty
		 */
		if(this.unfilledBuyOrders.isEmpty()) {
			this.handleEmptyBook(Order.BuySell.BUY);
		}
		if(this.unfilledSellOrders.isEmpty()) {
			this.handleEmptyBook(Order.BuySell.SELL);
		}
		
		/*
		 * Log new stock price in the stock column logger
		 */
		if(newOrder.getStock() == matchingOrder.getStock()) {
			Stock stock = matchingOrder.getStock();
			stock.transactionBasedDataLog.recordStockInformationAfterTransaction(tradePrice, this);
		} else {
			World.errorLog.logError(String.format("Orders for different stocks were matching. Standing order stock: %s, new order stock: %s", matchingOrder.getStock().getID(), newOrder.getStock().getID()));
		}
	}
	
	private void updateLastTradedPrices(Order matchingOrder) {
		if(matchingOrder.getBuySell() == Order.BuySell.BUY) {
			this.lastTradedMarketOrderBuyPrice = matchingOrder.getPrice();
		} else if (matchingOrder.getBuySell() == Order.BuySell.SELL) {
			this.lastTradedMarketOrderSellPrice = matchingOrder.getPrice();
		}
	}

	private int determineTransactionPrice(Order orderInBooks, Order newOrder) {
		/*
		 * Return the transaction price. This price depends on the market rules.
		 * Currently, it simply returns the price of the order already in the
		 * book.
		 */
		return orderInBooks.getPrice();
	}

	private Order getMatchingOrder(Order newOrder) {
		/*
		 * Returns the order at the top of the buy/sell queue if the
		 */
		Order matchingOrder;

		if (newOrder.getBuySell() == Order.BuySell.BUY) {
			Order bestSellOrder = this.unfilledSellOrders.peek();
			if (bestSellOrder == null) {
				matchingOrder = null;
			} else {
				int spread = bestSellOrder.getPrice() - newOrder.getPrice();
				if (spread <= 0) {
					matchingOrder = bestSellOrder;
				} else {
					matchingOrder = null;
				}
			}
		} else {
			Order bestBuyOrder = this.unfilledBuyOrders.peek();
			if (bestBuyOrder == null) {
				matchingOrder = null;
			} else {
				int spread = newOrder.getPrice() - bestBuyOrder.getPrice();
				if (spread <= 0) {
					matchingOrder = bestBuyOrder;
				} else {
					matchingOrder = null;
				}
			}
		}
		if (!(null == matchingOrder)) {
			this.orderflowLog.logEventMatch(newOrder, matchingOrder);
		}
		return matchingOrder;
	}

	private void addOrderToBook(Order order) {
		if (order.getType() == Order.Type.MARKET) {
			this.marketOrders.add(order);
			this.orderflowLog.logEventAddOrder(order);
			if (order.getBuySell() == Order.BuySell.BUY) {
				this.unfilledBuyOrders.add(order);
			} else {
				this.unfilledSellOrders.add(order);
			}
		}
		/*
		 * Update the global best buy/sell prices
		 */
		this.updateBestPrices();
	}

	private void removeOrderFromBook(Order order) {
		this.orderflowLog.logEventRemoveOrder(order);
		if (order.getType() == Order.Type.MARKET) {
			this.marketOrders.remove(order);
			if (order.getBuySell() == Order.BuySell.BUY) {
				this.unfilledBuyOrders.remove(order);
			} else {
				this.unfilledSellOrders.remove(order);
			}
		} else {
			World.errorLog.logError("Something is wrong. Shouldn't be asked to remove a limit order...");
		}
		/*
		 * Update the global best buy/sell prices
		 */
		this.updateBestPrices();
		World.destroyOrder();
	}

	private void updateBestPrices() {
		/*
		 * Need to be processed when 
		 * 1) Orders are entered into the book
		 * 2) Orders are cancelled
		 * 3) A transaction occurs
		 * 
		 * The function records the prices of the order on the top of the buy/sell queues.
		 * If there is no order in the orderbook, the best prices are set to the last traded prices.
		 * These prices can be from the same round, or from previous rounds.
		 * NOTE: If MarketRules.marketFillsEmptyBook = true; then this should never happen.
		 */
		int now = World.getCurrentRound();
		if(this.unfilledBuyOrders.isEmpty()) {
			this.bestBuyPrice[now] = this.lastTradedMarketOrderBuyPrice;
		} else {
			this.bestBuyPrice[now] = this.unfilledBuyOrders.element().getPrice();
		}
		
		if(this.unfilledSellOrders.isEmpty()) {
			this.bestSellPrice[now] = this.lastTradedMarketOrderSellPrice;
		} else {
			this.bestSellPrice[now] = this.unfilledSellOrders.element().getPrice();
		}
		
		
		
		
//		int newBestBuyPrice = 0, newBestSellPrice = Integer.MAX_VALUE;
//		if (this.unfilledSellOrders.isEmpty()) {
//			this.bestSellPrice[now] = Integer.MAX_VALUE;
//			this.handleEmptyBook(Order.BuySell.SELL);
//		} else {
//			try {
//				newBestSellPrice = this.unfilledSellOrders.element().getPrice();
//				this.bestSellPrice[World.getCurrentRound()] = newBestSellPrice;
//			} catch (IndexOutOfBoundsException e) {
//				World.errorLog.logError("bestASK price has not been initialized!");
//			}
//		}
//
//		if (this.unfilledBuyOrders.isEmpty()) {
//			this.bestBuyPrice[now] = 0;
//			this.handleEmptyBook(Order.BuySell.BUY);
//		} else {
//			try {
//				// this.printBook();
//				newBestBuyPrice = this.unfilledBuyOrders.element().getPrice();
//				this.bestBuyPrice[World.getCurrentRound()] = newBestBuyPrice;
//
//			} catch (IndexOutOfBoundsException e) {
//				World.errorLog.logError("bestBID price has not been initialized!");
//			}
//		}

		// System.out.println(String.format("Round %s:\tnewBestBuyPrice: %s,\tnewBestSellPrice: %s",
		// World.getCurrentTime(), newBestBuyPrice, newBestSellPrice));
	}
	
	@Deprecated
	public void printBook() {
		System.out.println("Printing details for orderbook"
				+ this.getIdentifier());
		if (this.unfilledBuyOrders.size() == 0
				&& this.unfilledSellOrders.size() == 0) {
			System.out.println("Order book is empty");
		} else {
			for (Order o : this.unfilledSellOrders) {
				System.out.format("Sell order price: %d, orderbook side volume: %s\n",o.getPrice(), o.getOrderbookSideVolume());
			}
			for (Order o : this.unfilledBuyOrders) {System.out.format("Buy order price: %d, orderbook side volume: %s\n",o.getPrice(), o.getOrderbookSideVolume());
			}

		}

	}
	

	// public int getSpread(){
	//
	// }

	public int getBestSellPrice(int time) throws NoOrdersException {
		try {
			if (bestSellPrice[time] == Integer.MAX_VALUE) {
				throw new NoOrdersException(time, this, BuySell.SELL);
			} else {
				return bestSellPrice[time];
			}
		} catch (IndexOutOfBoundsException e) {
			World.errorLog.logError(String.format("Cannot return ASK price at time %s. Needs initialization?"));
			return 0;
		}
	}

	public int getBestBuyPrice(int time) throws NoOrdersException {
		try {
			if (bestBuyPrice[time] == 0) {
				throw new NoOrdersException(time, this, BuySell.SELL);
			} else {
				return bestBuyPrice[time];
			}
		} catch (IndexOutOfBoundsException e) {
			World.errorLog.logError(String.format("Cannot return BID price at time %s. Needs initialization?"));
			return 0;
		}
	}

	public World getWorld() {
		return world;
	}

	public Stock getStock() {
		return stock;
	}

	public Market getMarket() {
		return market;
	}

	public ArrayList<Order> getUnprocessedOrders() {
		return unprocessedOrders;
	}

	public PriorityQueue<Order> getUnfilledBuyOrders() {
		return unfilledBuyOrders;
	}

	public PriorityQueue<Order> getUnfilledSellOrders() {
		return unfilledSellOrders;
	}

	public String getIdentifier() {
		return String.format("(%s,%s)", this.getStock().getID(), this.getMarket().getID());
	}

//	public String getDescription() {
//		return String.format("[stock: %s, market: %s]", stock.getID(), market.getID());
//	}

	public void printDetails(String ID) {
		System.out.println(ID + "; Trades stock " + this.stock.getID() + " on market " + this.market.getID());
	}

	public void processAllCancellations() {
		OrderCancellation cancellation;
		int index;
		Random random = new Random();
		while (this.unprocessedOrderCancellations.size() > 0) {
			index = random.nextInt(this.unprocessedOrderCancellations.size());
			cancellation = this.unprocessedOrderCancellations.remove(index);
			this.removeOrderFromBook(cancellation.getOrder());
		}
	}

	@SuppressWarnings("finally")
	public String getSpread(int round) {
		String spread = null;
		try {
			int s = this.getBestSellPrice(round) - this.getBestBuyPrice(round);
			spread = String.valueOf(s);
		} catch (NoOrdersException e) {
			spread = "NaN";
		} finally {
			return spread;
		}
	}

	@Deprecated
	public boolean isNoSellOrders() {
		if (this.unfilledSellOrders.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	@Deprecated
	public boolean hasNoBuyOrders() {
		if (this.unfilledBuyOrders.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getLastTradedMarketOrderBuyPrice() {
		return this.lastTradedMarketOrderBuyPrice;
	}
	
	public int getLastTradedMarketOrderSellPrice() {
		return this.lastTradedMarketOrderSellPrice;
	}

}
