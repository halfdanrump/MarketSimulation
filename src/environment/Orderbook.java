package environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Random;

import agent.StylizedTrader;

import environment.Order.BuySell;

import log.OrderbookLogger;
import setup.SimulationSetup;
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
//	private int[] bestSellPrice;
//	private int[] bestBuyPrice;
	private ArrayList<Long> bestSellPrice;
	private ArrayList<Long> bestBuyPrice;
	
	private long lastTradedMarketOrderBuyPrice;
	private long lastTradedMarketOrderSellPrice;

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
//		this.initializeBookWithRandomOrders();
	}

	private void initializeDataStructures() {
		this.unprocessedOrders = new ArrayList<Order>();
		this.unprocessedOrderCancellations = new ArrayList<OrderCancellation>();
		this.unfilledBuyOrders = new PriorityQueue<Order>(10, Collections.reverseOrder(new orderPriceComparatorAscending()));
		this.unfilledSellOrders = new PriorityQueue<Order>(10, new OrderPriceComparatorLowFirst());
		this.marketOrders = new PriorityQueue<Order>(10, new OrderExpirationTimeComparator());
		this.bestSellPrice = new ArrayList<Long>(SimulationSetup.nRounds); 
		this.bestBuyPrice = new ArrayList<Long>(SimulationSetup.nRounds);
		
	}
	
	public void initializeBookWithRandomOrders() {
		/*
		 * Must be called AFTER the stocks and markets have been created.
		 * Must be called AFTER the orderbook logger has been created.
		 * Therefore the call to this function is placed in World.initializeEnvironment()
		 */
		long sellPrice;
		Order buyOrder = StylizedTrader.submitRandomMarketBuyOrder(this);
//		this.receiveOrder(buyOrder);
		while(true) {
			sellPrice = StylizedTrader.getStylizedTraderEstimatedPrice(this.getStock());
			if(sellPrice > buyOrder.getPrice()) {
				long volume = StylizedTrader.getStylizedTraderOrderVolume();
				int now = World.getCurrentRound();
				new Order(now, now, 1000, volume, sellPrice, Order.Type.MARKET, Order.BuySell.SELL, null, this, Message.TransmissionType.WITH_TRANSMISSION_DELAY);
//				this.receiveOrder(order);
				break;
			}
			
		}
		this.lastTradedMarketOrderBuyPrice = buyOrder.getPrice();
		this.lastTradedMarketOrderSellPrice = sellPrice;
//		this.processAllNewOrders();
		World.dispatchArrivingOrders();
		
	}
	
	public void receiveOrder(Order order) {
		this.unprocessedOrders.add(order);
	}

	public void receieveOrderCancellation(OrderCancellation update) {
		this.unprocessedOrderCancellations.add(update);
	}

	public long expireOrders() {
		long nExpiredOrders = 0;
		try {
			while (marketOrders.peek().getExpirationTime() == World.getCurrentRound()) {
				removeOrderFromBook(marketOrders.peek());
				nExpiredOrders++;
			}
		} catch (NullPointerException e) {

		}
		return nExpiredOrders;
	}
	
	private void checkForAndHandleEmptyBook() {
		/*
		 * When the book is empty, the market rules determine what will happen
		 */
		int now = World.getCurrentRound();
		boolean wasEmpty = false;
		Order.BuySell buysell = null;
		if(this.hasNoBuyOrders() & MarketRules.marketFillsEmptyBook) {
			buysell = Order.BuySell.BUY;
			long price = this.getLastTradedMarketOrderBuyPrice();
			new Order(now, now, MarketRules.orderLengthWhenMarketFillsEmptyBook, MarketRules.orderVolumeWhenMarketFillsEmptyBook, price, MarketRules.orderTypeWhenMarketFillsEmptyBook, buysell, null, this, Message.TransmissionType.INSTANTANEOUS);
			wasEmpty = true;
		}
		if(this.hasNoSellOrders() & MarketRules.marketFillsEmptyBook) {
			buysell = Order.BuySell.SELL;
			long price = this.getLastTradedMarketOrderSellPrice();
			new Order(now, now, MarketRules.orderLengthWhenMarketFillsEmptyBook, MarketRules.orderVolumeWhenMarketFillsEmptyBook, price, MarketRules.orderTypeWhenMarketFillsEmptyBook, buysell, null, this, Message.TransmissionType.INSTANTANEOUS);
			wasEmpty = true;
		}		
		if(wasEmpty) {
			this.orderflowLog.logEventNoMarketOrders(buysell);
			if(!MarketRules.marketFillsEmptyBook) {
				World.errorLog.logError(String.format("Orderbook %s was empty on the %s side, but the market rules are such that the book will remain empty", this.getIdentifier(), buysell));
			}
		}
		
	}
	
//	public void instantaneouslyInsertOrderAtLastTradedPriceIntoOrderbook(Order.BuySell buysell) {
//		long now = World.getCurrentRound();
//		if(buysell == Order.BuySell.BUY) {
//			long price = this.getLastTradedMarketOrderBuyPrice();
//			new Order(now, now, MarketRules.orderLengthWhenMarketFillsEmptyBook, MarketRules.orderVolumeWhenMarketFillsEmptyBook, price, MarketRules.orderTypeWhenMarketFillsEmptyBook, buysell, null, this, Message.TransmissionType.INSTANTANEOUS);
//		} else {
//			long price = this.getLastTradedMarketOrderSellPrice();
//			new Order(now, now, MarketRules.orderLengthWhenMarketFillsEmptyBook, MarketRules.orderVolumeWhenMarketFillsEmptyBook, price, MarketRules.orderTypeWhenMarketFillsEmptyBook, buysell, null, this, Message.TransmissionType.INSTANTANEOUS);
//
//		}
//	}



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
			this.processNewlyArrivedNewOrder(newOrder);
		}
		this.updateBestPricesAtTheEndOfEachRound();
	}

	public void processNewlyArrivedNewOrder(Order newlyArrivedOrder) {
		/*
		 * The methods processes a single new order. The new order is entered in
		 * the book if there is no price matching order. If there is a price
		 * matching order with a full or partial volume match, a transaction is
		 * executed. If the matching market order is smaller than the new order,
		 * it is removed
		 */
		Order matchingOrder;

		this.orderflowLog.logEventProcessNewOrder(newlyArrivedOrder);
		while (true) {
			if (newlyArrivedOrder.getCurrentMarketSideVolume() == 0) {
				/*
				 * If the new order has been completely filled, don't add it to
				 * the book.
				 */
				break;
			}
			matchingOrder = this.getMatchingOrder(newlyArrivedOrder);
			if (matchingOrder == null) {
				/*
				 * Add the order to the book if there is no (longer) any
				 * matching order, and proceed to the next order by breaking the
				 * while loop.
				 */
				this.addOrderToBook(newlyArrivedOrder);
				break;
			} else {
				this.executeTrade(matchingOrder, newlyArrivedOrder);
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
		long tradeVolume = Math.min(matchingOrder.getCurrentMarketSideVolume(), newOrder.getCurrentMarketSideVolume());
		long tradePrice = this.determineTransactionPrice(matchingOrder, newOrder);
		long tradeTotal = 0l;
		if(tradeVolume * tradePrice > Long.MAX_VALUE) {
			World.errorLog.logError("Order total exceeded long range...");
		} else {
			tradeTotal = tradeVolume * tradePrice;
		}
		
		
		
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
		if (matchingOrder.getCurrentMarketSideVolume() == 0) {
			this.removeOrderFromBook(matchingOrder);
		}
		
		/*
		 * Handle the situation where one side of the orderbook is empty
		 */
//		this.checkForAndHandleEmptyBook(buysell)
//		if(this.unfilledBuyOrders.isEmpty()) {
//			this.checkForAndHandleEmptyBook(Order.BuySell.BUY);
//		}
//		if(this.unfilledSellOrders.isEmpty()) {
//			this.checkForAndHandleEmptyBook(Order.BuySell.SELL);
//		}
		
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

	private long determineTransactionPrice(Order orderInBooks, Order newOrder) {
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
				long spread = bestSellOrder.getPrice() - newOrder.getPrice();
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
				long spread = newOrder.getPrice() - bestBuyOrder.getPrice();
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
		
//		if(this.hasNoBuyOrders()) {
//			this.instantaneouslyInsertOrderAtLastTradedPriceIntoOrderbook(Order.BuySell.BUY);
//		} else if(this.hasNoSellOrders()) {
//			this.instantaneouslyInsertOrderAtLastTradedPriceIntoOrderbook(Order.BuySell.SELL);
//		}
//		if(this.hasNoBuyOrders() | this.hasNoSellOrders()) {
//			World.errorLog.logError("In orderbook %s: It should not happen that the orderbook was empty right after inserting an ")
//		}
		/*
		 * Update the global best buy/sell prices
		 */
		this.checkForAndHandleEmptyBook();
		this.updateBestPricesAtTheEndOfEachRound();
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
		 * First make sure that the orderbook is not empty, then update the global best buy/sell prices
		 */
		this.checkForAndHandleEmptyBook();
		this.updateBestPricesAtTheEndOfEachRound();
		World.destroyOrder();
	}

	private void updateBestPricesAtTheEndOfEachRound() {
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
		try {
			if(this.unfilledBuyOrders.isEmpty()) {
				this.bestBuyPrice.add(this.lastTradedMarketOrderBuyPrice);
				System.out.println(String.format("Used last best buy: %s", this.lastTradedMarketOrderBuyPrice));
			} else {
				this.bestBuyPrice.add(this.unfilledBuyOrders.element().getPrice());
				System.out.println(String.format("Used current best buy: %s", this.unfilledBuyOrders.element().getPrice()));
			}
			
			if(this.unfilledSellOrders.isEmpty()) {
				this.bestSellPrice.add(this.lastTradedMarketOrderSellPrice);
				System.out.println(String.format("Used last best sell: %s", this.lastTradedMarketOrderSellPrice));
			} else {
				this.bestSellPrice.add(this.unfilledSellOrders.element().getPrice());
				System.out.println(String.format("Used current best sell: %s", this.unfilledSellOrders.element().getPrice()));
			}
			
			long spread = this.bestSellPrice.get(now) - this.bestBuyPrice.get(now);
			if(spread <= 0) {
				Exception e = new Exception();
				e.printStackTrace();
				World.errorLog.logError(String.format("Spread at orderbook %s was %s", this.getIdentifier(), spread));
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
//		long newBestBuyPrice = 0, newBestSellPrice = Integer.MAX_VALUE;
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
				System.out.format("Sell order price: %d, orderbook side volume: %s\n",o.getPrice(), o.getCurrentMarketSideVolume());
			}
			for (Order o : this.unfilledBuyOrders) {System.out.format("Buy order price: %d, orderbook side volume: %s\n",o.getPrice(), o.getCurrentMarketSideVolume());
			}

		}

	}
	

	// public long getSpread(){
	//
	// }

	public Long getBestSellPrice(int time) throws NoOrdersException {
		try {
			if (this.bestSellPrice.get(time) == Long.MAX_VALUE) {
				throw new NoOrdersException(time, this, BuySell.SELL);
			} else {
				return this.bestSellPrice.get(time);
			}
		} catch (IndexOutOfBoundsException e) {
			World.errorLog.logError(String.format("Cannot return ASK price at time %s. Needs initialization?"));
			return 0l;
		}
	}

	public Long getBestBuyPrice(int time) throws NoOrdersException {
		try {
			if (this.bestBuyPrice.get(time) == 0) {
				throw new NoOrdersException(time, this, BuySell.SELL);
			} else {
				return this.bestBuyPrice.get(time);
			}
		} catch (IndexOutOfBoundsException e) {
			World.errorLog.logError(String.format("Cannot return BID price at time %s. Needs initialization?"));
			return 0l;
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
			long s = this.getBestSellPrice(round) - this.getBestBuyPrice(round);
			spread = String.valueOf(s);
		} catch (NoOrdersException e) {
			spread = "NaN";
		} finally {
			return spread;
		}
	}

	public boolean hasNoSellOrders() {
		if (this.unfilledSellOrders.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	
	public boolean hasNoBuyOrders() {
		if (this.unfilledBuyOrders.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	
	public long getLastTradedMarketOrderBuyPrice() {
		return this.lastTradedMarketOrderBuyPrice;
	}
	
	public long getLastTradedMarketOrderSellPrice() {
		return this.lastTradedMarketOrderSellPrice;
	}

}
