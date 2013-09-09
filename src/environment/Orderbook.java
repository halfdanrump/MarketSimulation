package environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;


import Experiments.Experiment;
import agent.StylizedTrader;


import log.OrderbookLogger;
import utilities.OrderExpirationTimeComparator;
import utilities.orderPriceComparatorAscending;
import utilities.OrderPriceComparatorLowFirst;

public class Orderbook {
	private Experiment experiment;
	private Stock stock;
	private Market market;

	
	private ArrayList<Order> unprocessedOrders;
	private ArrayList<OrderCancellation> unprocessedOrderCancellations;
	private PriorityQueue<Order> unfilledBuyOrders;
	private PriorityQueue<Order> unfilledSellOrders;
	private PriorityQueue<Order> marketOrders;
//	private int[] bestSellPrice;
//	private int[] bestBuyPrice;
	private ArrayList<Long> localBestSellPriceAtEndOfRound;
	private ArrayList<Long> localBestBuyPriceAtEndOfRound;
	
	private long localLastTradedMarketOrderBuyPrice;
	private long localLastTradedMarketOrderSellPrice;

	public OrderbookLogger orderflowLog;
	public OrderbookLogger eventLog;
	public OrderbookLogger roundBasedLog;
	
	private int nTradesThisRound = 0;
	private long nBuyOrdersReceived;
	private long nSellOrdersReceived;

	// public Orderbook(){
	// initialize();
	// }



	public Orderbook(Experiment experiment, Stock stock, Market market) {
		this.experiment = experiment;
		initializeDataStructures();
		this.stock = stock;
		this.market = market;
		stock.registerWithMarket(market);
//		this.initializeBookWithRandomOrders();
		this.nBuyOrdersReceived = 0;
		this.nSellOrdersReceived = 0;
	}

	private void initializeDataStructures() {
		this.unprocessedOrders = new ArrayList<Order>();
		this.unprocessedOrderCancellations = new ArrayList<OrderCancellation>();
		this.unfilledBuyOrders = new PriorityQueue<Order>(10, Collections.reverseOrder(new orderPriceComparatorAscending()));
		this.unfilledSellOrders = new PriorityQueue<Order>(10, new OrderPriceComparatorLowFirst());
		this.marketOrders = new PriorityQueue<Order>(10, new OrderExpirationTimeComparator());
		this.localLastTradedMarketOrderBuyPrice = this.experiment.initialFundamentalPrice - Math.round(this.experiment.ob_startingSpread/2);
		this.localLastTradedMarketOrderSellPrice = this.experiment.initialFundamentalPrice + Math.round(this.experiment.ob_startingSpread/2);
		this.localBestBuyPriceAtEndOfRound = new ArrayList<Long>(Collections.nCopies(this.experiment.nTotalRounds, this.localLastTradedMarketOrderBuyPrice));
		this.localBestSellPriceAtEndOfRound = new ArrayList<Long>(Collections.nCopies(this.experiment.nTotalRounds, this.localLastTradedMarketOrderSellPrice));
	}
	
//	public void initializeBookWithRandomOrders() {
//		/*
//		 * Must be called AFTER the stocks and markets have been created.
//		 * Must be called AFTER the orderbook logger has been created.
//		 * Therefore the call to this function is placed in World.initializeEnvironment()
//		 */
//		long sellPrice;
//		Order buyOrder = StylizedTrader.submitRandomMarketBuyOrder(this, this.experiment);
////		this.receiveOrder(buyOrder);
//		while(true) {
//			sellPrice = StylizedTrader.getStylizedTraderEstimatedPrice(this.getStock(), this.experiment);
//			if(sellPrice > buyOrder.getPrice()) {
//				long volume = StylizedTrader.getStylizedTraderOrderVolume(this.experiment);
//				int now = this.experiment.getWorld().getCurrentRound();
//				new Order(now, now, 1000, volume, sellPrice, Order.Type.MARKET, Order.BuySell.SELL, null, this, Message.TransmissionType.WITH_TRANSMISSION_DELAY, this.experiment);
////				this.receiveOrder(order);
//				break;
//			}			
//		}
//		this.updateLocalBestPricesAtTheEndOfEachRound();
//		this.printOrderbook();
//		
//		this.localLastTradedMarketOrderBuyPrice = buyOrder.getPrice();
//		this.localLastTradedMarketOrderSellPrice = sellPrice;
//		this.experiment.getWorld().dispatchArrivingOrders();
//		
//	}
	
	public void fillBookWithRandomOrders() {
		Random r = new Random();
		this.localLastTradedMarketOrderBuyPrice = this.experiment.initialFundamentalPrice - this.experiment.ob_startingSpread/2;
		this.localLastTradedMarketOrderSellPrice = this.experiment.initialFundamentalPrice + this.experiment.ob_startingSpread/2;
		/*
		 * Make random market sell orders
		 */
		for(int i = 0; i<this.experiment.ob_nStartOrders/2; i++) {
			long price = (long) (this.experiment.initialFundamentalPrice + this.experiment.ob_startingSpread/2 + Math.abs(r.nextGaussian() * this.experiment.ob_initialOrderStd));
			long volume = (long) (1 + Math.abs(this.experiment.ob_initialOrderVolumeMean + r.nextGaussian() * this.experiment.ob_initialOrderVolumeStd));
			new Order(0, 0, this.experiment.ob_orderExpirationTime, volume, price, Order.Type.MARKET, Order.BuySell.SELL, null, this, Message.TransmissionType.INSTANTANEOUS, this.experiment);
		}
		/*
		 * Make random market buy orders
		 */
		for(int i = 0; i<this.experiment.ob_nStartOrders/2; i++) {
			long price = (long) (this.experiment.initialFundamentalPrice - this.experiment.ob_startingSpread/2 - Math.abs(r.nextGaussian() * this.experiment.ob_initialOrderStd));
			long volume = (long) (1 + Math.abs(this.experiment.ob_initialOrderVolumeMean + r.nextGaussian() * this.experiment.ob_initialOrderVolumeStd));
			new Order(0, 0, this.experiment.ob_orderExpirationTime, volume, price, Order.Type.MARKET, Order.BuySell.BUY, null, this, Message.TransmissionType.INSTANTANEOUS, this.experiment);
		}
		this.experiment.getWorld().dispatchArrivingOrders();
		this.updateLocalBestPricesAtTheEndOfEachRound();
		this.printOrderbook();
		
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
			while (marketOrders.peek().getExpirationTime() == this.experiment.getWorld().getCurrentRound()) {
				Order order = marketOrders.peek();
				removeOrderFromBook(order);
				if(order.hasOwner()) {
					order.getOwner().removeOrderWhenExpired(order);
				}
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
		int now = this.experiment.getWorld().getCurrentRound();
		boolean wasEmpty = false;
		long price = 0l;
		int nInsertedOrders = 0;
		while(true) {
			/*
			 * This code is not very good
			 */
			Order.BuySell buysell = null;
			if(this.hasNoBuyOrders() & this.experiment.marketFillsEmptyBook) {
				buysell = Order.BuySell.BUY;
				price = this.localLastTradedMarketOrderBuyPrice;
				new Order(now, now, this.experiment.orderLengthWhenMarketFillsEmptyBook, this.experiment.orderVolumeWhenMarketFillsEmptyBook, price, this.experiment.orderTypeWhenMarketFillsEmptyBook, buysell, null, this, Message.TransmissionType.INSTANTANEOUS, this.experiment);
				wasEmpty = true;
				System.out.println("Market filled empty book!");
			}
			if(this.hasNoSellOrders() & this.experiment.marketFillsEmptyBook) {
				buysell = Order.BuySell.SELL;
				price = this.localLastTradedMarketOrderSellPrice;
				new Order(now, now, this.experiment.orderLengthWhenMarketFillsEmptyBook, this.experiment.orderVolumeWhenMarketFillsEmptyBook, price, this.experiment.orderTypeWhenMarketFillsEmptyBook, buysell, null, this, Message.TransmissionType.INSTANTANEOUS, this.experiment);
				wasEmpty = true;
				System.out.println("Market filled empty book!");
			}		
			if(wasEmpty) {
				nInsertedOrders++;
				this.orderflowLog.logEventNoMarketOrders(buysell);
				if(!this.experiment.marketFillsEmptyBook) {
					this.experiment.getWorld().errorLog.logError(String.format("Orderbook %s was empty on the %s side, but the market rules are such that the book will remain empty", this.getIdentifier(), buysell), this.experiment);
				} else {
					this.eventLog.logOnelineEvent(String.format("The market inserted a %s order at price %s into orderbook %s.", buysell, price, this.getIdentifier()));
				}
			}
			if(!wasEmpty) {
				break;
			} else if(wasEmpty & !(this.hasNoBuyOrders() | this.hasNoSellOrders())) {
				this.eventLog.logOnelineEvent(String.format("After detecting an empty orderbook, the market matched a total of %s orders in orderbook %s", nInsertedOrders, this.getIdentifier()));
				break;
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
			
//			/*
//			 * REMOVE THE FOLLOWING AFTER TESTING
//			 */
//			Orderbook o = newOrder.getOrderbook();
//			System.out.println(String.format("Spread after processing order is %s", this.getCurrentInRoundSpread()));
		}
		this.updateLocalBestPricesAtTheEndOfEachRound();
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
		if(newlyArrivedOrder.getBuySell() == Order.BuySell.BUY) {
			this.nBuyOrdersReceived++;
		}else {
			this.nSellOrdersReceived++;
		}

		this.orderflowLog.logEventProcessNewOrder(newlyArrivedOrder);
		while (true) {
			if (newlyArrivedOrder.getCurrentMarketSideVolume() == 0) {
				/*
				 * If the new order has been completely filled, don't add it to
				 * the book.
				 */
//				System.out.println(String.format("Volume of newly arrived order %s was zero", newlyArrivedOrder.getID()));
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
//				System.out.println(String.format("Executed trade with order %s at price %s", newlyArrivedOrder.getID(), tradePrice));
			}

		}
	}

	private long executeTrade(Order matchingOrder, Order newOrder) {
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
			this.experiment.getWorld().errorLog.logError("Order total exceeded long range...", this.experiment);
		} else {
			tradeTotal = tradeVolume * tradePrice;
		}
		/*
		 * Only issue a receipt if the trader was a "registered" trader (that
		 * is, a trader that we care about).
		 */
		if (!(matchingOrder.getOwner() == null)) {
			/*
			 * It can happen that a stylized trader submits an order which is matched by a standing order
			 * owned by an HFT. In this case the owner of the 
			 */
			receipt = new TransactionReceipt(matchingOrder, tradeVolume,tradePrice, tradeTotal, newOrder);
			this.orderflowLog.logEventTransaction(receipt);
		}
		if (!(newOrder.getOwner() == null)) {
			/*
			 * It can happen that a HFT submits a new order which is matched by a standing order
			 * owned by a stylized trader. In this case, the owner of the standing order is null.
			 */
			receipt = new TransactionReceipt(newOrder, tradeVolume, tradePrice, tradeTotal, matchingOrder);
			this.orderflowLog.logEventTransaction(receipt);
		}
		/*
		 * Log the event in the orderbook log
		 */
		this.orderflowLog.logEventUpdateOrderVolume(newOrder, tradeVolume);
		matchingOrder.updateMarketSideVolumeByDifference(-tradeVolume);
		this.orderflowLog.logEventUpdateOrderVolume(matchingOrder, tradeVolume);
		newOrder.updateMarketSideVolumeByDifference(-tradeVolume);
		/*
		 * Remove the order if there was a complete match (remaining order volume was zero)
		 */
		if (matchingOrder.getCurrentMarketSideVolume() == 0) {
			this.removeOrderFromBook(matchingOrder);
		}
		/*
		 * Update the last traded price
		 */
		this.updateLastTradedPrices(matchingOrder);		

		/*
		 * Log new stock price in the stock column logger
		 */
		if(newOrder.getStock() == matchingOrder.getStock()) {
			Stock stock = matchingOrder.getStock();
//			if(newOrder.getOwner() != null) {
//				if(matchingOrder.getOwner() != null)
//					System.out.println("hum");
//			}
			stock.incrementTradedVolume(tradeVolume, this.experiment.getCurrentRound());
			stock.transactionBasedDataLog.recordStockInformationAfterTransaction(tradePrice, this, newOrder, matchingOrder);
		} else {
			this.experiment.getWorld().errorLog.logError(String.format("Orders for different stocks were matching. Standing order stock: %s, new order stock: %s", matchingOrder.getStock().getID(), newOrder.getStock().getID()), this.experiment);
		}
		this.nTradesThisRound++;
//		this.printOrderbook();
		return tradePrice;
	}
	
	private void updateLastTradedPrices(Order lastTradedOrder) {
		if(lastTradedOrder.getBuySell() == Order.BuySell.BUY) {
			this.localLastTradedMarketOrderBuyPrice = lastTradedOrder.getPrice();
		} else if (lastTradedOrder.getBuySell() == Order.BuySell.SELL) {
			this.localLastTradedMarketOrderSellPrice = lastTradedOrder.getPrice();
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
//			this.experiment.getWorld().errorLog.logError("In orderbook %s: It should not happen that the orderbook was empty right after inserting an ")
//		}
		/*
		 * Update the global best buy/sell prices
		 */
		this.checkForAndHandleEmptyBook();
//		this.updateBestPricesAtTheEndOfEachRound();
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
			this.experiment.getWorld().errorLog.logError("Something is wrong. Shouldn't be asked to remove a limit order...", this.experiment);
		}
		
		/*
		 * First make sure that the orderbook is not empty, then update the global best buy/sell prices
		 */
		this.checkForAndHandleEmptyBook();
//		this.updateLastTradedPrices(order);
//		this.updateBestPricesAtTheEndOfEachRound();
		this.experiment.getWorld().destroyOrder();
	}

	private void updateLocalBestPricesAtTheEndOfEachRound() {
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
		int now = this.experiment.getWorld().getCurrentRound();
		try {
			if(!this.unfilledBuyOrders.isEmpty()) {
				this.localBestBuyPriceAtEndOfRound.set(now, this.unfilledBuyOrders.element().getPrice());
			} else {
				this.localBestBuyPriceAtEndOfRound.set(now, this.getLocalBestBuyPriceAtEndOfRound(now - 1));
			}
			
			
//			if(this.unfilledBuyOrders.isEmpty() | this.nTradesThisRound == 0) {
//				
////				System.out.println(String.format("Used last best buy: %s", this.lastTradedMarketOrderBuyPrice));
//			} else {
//				this.localBestBuyPriceAtEndOfRound.set(now, this.unfilledBuyOrders.element().getPrice());
////				System.out.println(String.format("Used current best buy: %s", this.unfilledBuyOrders.element().getPrice()));
//			}
			
			if(!this.unfilledSellOrders.isEmpty()) {
				this.localBestSellPriceAtEndOfRound.set(now, this.unfilledSellOrders.element().getPrice());
			} else {
				this.localBestSellPriceAtEndOfRound.set(now, this.getLocalBestSellPriceAtEndOfRound(now - 1));
//				System.out.println(String.format("Used current best sell: %s", this.unfilledSellOrders.element().getPrice()));
			}
			
			long spread = this.localBestSellPriceAtEndOfRound.get(now) - this.localBestBuyPriceAtEndOfRound.get(now);
			if(spread <= 0) {
				Exception e = new Exception();
				e.printStackTrace();
				this.printOrderbook();
				this.experiment.getWorld().errorLog.logError(String.format("Spread at orderbook %s was %s", this.getIdentifier(), spread), this.experiment);
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
	}
	
	public Long getLocalBestSellPriceAtEndOfRound(int time){
		if (time < 0) {
			time = 0;
		}
		return this.localBestSellPriceAtEndOfRound.get(time);
//		try {
//			if (this.localBestSellPriceAtEndOfRound.get(time) == Long.MAX_VALUE) {
//				throw new NoOrdersException(time, this, BuySell.SELL);
//			} else {
//				return this.localBestSellPriceAtEndOfRound.get(time);
//			}
//		} catch (IndexOutOfBoundsException e) {
//			this.experiment.getWorld().errorLog.logError(String.format("Cannot return ASK price at time %s. Needs initialization?"), this.experiment);
//			return 0l;
//		}
	}

	public Long getLocalBestBuyPriceAtEndOfRound(int time){
		if(time < 0) {
			time = 0;
		}
		return this.localBestBuyPriceAtEndOfRound.get(time);
		
//		try {
//			if (this.localBestBuyPriceAtEndOfRound.get(time) == 0) {
//				throw new NoOrdersException(time, this, BuySell.SELL);
//			} else {
//				return this.localBestBuyPriceAtEndOfRound.get(time);
//			}
//		} catch (IndexOutOfBoundsException e) {
//			this.experiment.getWorld().errorLog.logError(String.format("Cannot return BID price at time %s. Needs initialization?"), this.experiment);
//			return 0l;
//		}
	}

//	public World getWorld() {
//		return world;
//	}

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
	
	public void printOrderbook() {
		/*
		 * Make hashmap buyOrders with <price, number of orders>
		 * Iterate over unfilled buy orders
		 * 	if price in buyOrders.keys then buyOrders.get(price) =+ 1
		 */
		System.out.println(String.format("Orderbook in round %s", this.experiment.getWorld().getCurrentRound()));
		TreeMap<Long, Integer> buyOrders = new TreeMap<Long, Integer>();
		java.util.Iterator<Order> it = this.unfilledBuyOrders.iterator();
		while(it.hasNext()) {
			Order o = it.next();
			Long price = o.getPrice();
			if(buyOrders.containsKey(price)) {
				buyOrders.put(price, buyOrders.get(price) + 1);
			} else {
				buyOrders.put(price, 1);
			}
		}
		
		TreeMap<Long, Integer> sellOrders = new TreeMap<Long, Integer>();
		it = this.unfilledSellOrders.iterator();
		while(it.hasNext()) {
			Order o = it.next();
			Long price = o.getPrice();
			if(sellOrders.containsKey(price)) {
				sellOrders.put(price, sellOrders.get(price) + 1);
			} else {
				sellOrders.put(price, 1);
			}
		}
		
		/*
		 * Print sell orders in descending price order
		 */
		for(Long price:sellOrders.descendingKeySet()) {
			System.out.println(String.format("%s\t%s", sellOrders.get(price), price));
		}

		for(Long price:buyOrders.descendingKeySet()) {
			System.out.println(String.format("\t%s\t%s", price, buyOrders.get(price)));
		}
		System.out.println("");
		
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

	public String getSpreadForBestPricesAtEndOfRound(int round) {
		long spread = this.getLocalBestSellPriceAtEndOfRound(round) - this.getLocalBestBuyPriceAtEndOfRound(round);
		return String.valueOf(spread); 
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
	
	@SuppressWarnings("finally")
	public long getCurrentInRoundSpread() {
		long spread = 0;
		try {
			spread = this.unfilledSellOrders.element().getPrice() - this.unfilledBuyOrders.element().getPrice();
		} catch(NoSuchElementException e) {
			e.printStackTrace();
			spread = -1;
		} finally {
			if(spread <= 0) {
				Exception e = new Exception();
				e.printStackTrace();
			}
			return spread;
		}
		

		
	}

	public Experiment getExperiment() {
		return this.experiment;
	}
	
//	public long getGlobalLastTradedMarketOrderBuyPrice() {
//		return this.stock.getGlobalLastTradedMarketOrderBuyPrice();
//	}
//
//	public long getGlobalLastTradedMarketOrderSellPrice() {
//		return this.stock.getGlobalLastTradedMarketOrderSellPrice();
//	}
	
	
	public int getnTradesThisRound() {
		return nTradesThisRound;
	}

	public void setnTradesThisRound(int nTradesThisRound) {
		this.nTradesThisRound = nTradesThisRound;
	}
	
	public long getNReceivedBuyOrders() {
		return this.nBuyOrdersReceived;
	}
	
	public long getNReceivedSellOrders() {
		return this.nSellOrdersReceived;
	}

}
