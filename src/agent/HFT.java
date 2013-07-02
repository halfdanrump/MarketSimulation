package agent;

import environment.*;
import environment.Order.BuySell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import log.AgentLogger;
import log.Logging;

import setup.HighFrequencyTradingBehavior;
import setup.MarketRules;
import utilities.NoOrdersException;
import utilities.Utils;

public abstract class HFT implements HighFrequencyTradingBehavior, Logging, MarketRules, EntityThatCanSubmitOrders {
	public static long nHFTs = 0;
	protected long id;
	protected long cash;
	protected long wakeupTime;
	protected long thinkingTime;
	protected HashMap<Orderbook, Order> standingBuyOrders;
	protected HashMap<Orderbook, Order> standingSellOrders;
	protected ArrayList<Orderbook> orderbooks;
	protected HashMap<Market, Integer> latencyToMarkets;
	protected ArrayList<TransactionReceipt> receipts;
	protected HashMap<Stock, Long> ownedStocks;
	protected HashMap<Stock, Long> numberOfStocksInStandingSellOrders;
	private HashMap<Stock, Long> numberOfStocksInStandingBuyOrders;
	protected ArrayList<Market> markets;
	protected ArrayList<Stock> stocks;

	/*
	 * For experiments in which the agent is associated with a group
	 */
	protected int group;
	
	protected long nSubmittedBuyOrders;
	protected long nSubmittedSellOrders;
	protected long nSubmittedCancellations;
	protected long nReceivedBuyOrderReceipts;
	protected long nReceivedSellOrderReceipts;
	protected long nTimesBorrowedCash;
	protected long nTimesSoldStocksWhenAlreadyHadNegativeAmountOfStock;
	protected long nTimesAgentGotReceiptForOrderWhichIsNotInHisStandingOrderList;
	protected long nTimesAgentDidShortSelling;

	protected ArrayList<Order> orderHistory;

	protected abstract long getWaitingTime();

	public enum agentAction{
		SUBMIT_ORDER,
		CANCEL_ORDER,
		UPDATE_ORDER
	}
	
	public abstract void storeMarketInformation() throws NoOrdersException;

	public abstract boolean executeStrategyAndSubmit();

	public AgentLogger eventlog;
	public AgentLogger roundDatalog;
	public AgentLogger tradeLog;
	
	public HFT(long wealth, int[] stockIDs, int[] startingPortfolio, int[] marketIDs, int[] latencies, int group) {
		this.cash = wealth;
		this.wakeupTime = World.getCurrentRound();
		this.group = group;
		this.initialize(stockIDs, marketIDs, latencies);
		this.setPortfolio(startingPortfolio);
		World.addNewAgent(this);
	}

	public HFT(int[] stockIDs, int[] marketIDs, int[] latencies, int group) {
		this.group = group;
		if (randomStartWealth) {
			this.cash = getRandomInitialTraderWealth();
		} else {
			this.cash = constantStartWealth;
		}
		this.wakeupTime = World.getCurrentRound();
		this.initialize(stockIDs, marketIDs, latencies);
		World.addNewAgent(this);
	}

	private void initialize(int[] stockIDs, int[] marketIDs, int[] marketLatencies) {
		this.id = nHFTs;
		nHFTs++;
		/*
		 * Variables for logging
		 */
		this.nSubmittedBuyOrders = 0;
		this.nSubmittedSellOrders = 0;
		this.nReceivedBuyOrderReceipts = 0;
		this.nReceivedSellOrderReceipts = 0;
		this.nTimesBorrowedCash = 0;
		this.nTimesSoldStocksWhenAlreadyHadNegativeAmountOfStock = 0;
		this.nTimesAgentGotReceiptForOrderWhichIsNotInHisStandingOrderList = 0;
		this.nTimesAgentDidShortSelling = 0;
		this.receipts = new ArrayList<TransactionReceipt>();

		/*
		 * At present, each agent can only maintain a single order on each side
		 * of the book
		 */
		this.standingBuyOrders = new HashMap<Orderbook, Order>();
		this.standingSellOrders = new HashMap<Orderbook, Order>();

		this.orderbooks = new ArrayList<Orderbook>();
		this.ownedStocks = new HashMap<Stock, Long>();
		this.latencyToMarkets = new HashMap<Market, Integer>();
		this.markets = new ArrayList<Market>();
		this.stocks = new ArrayList<Stock>();
		this.buildLatencyHashmap(marketIDs, marketLatencies);
		this.markets.addAll(this.latencyToMarkets.keySet());
		this.buildOrderbooksHashMap(marketIDs, stockIDs);
		this.buildStocksList(stockIDs);
		this.initializePortfolio();
		this.numberOfStocksInStandingSellOrders = new HashMap<Stock, Long>();
		this.numberOfStocksInStandingBuyOrders = new HashMap<Stock, Long>();
		for (Stock stock : this.stocks) {
			this.numberOfStocksInStandingBuyOrders.put(stock, 0l);
			this.numberOfStocksInStandingSellOrders.put(stock, 0l);
		}
		if (keepOrderHistory) {
			this.orderHistory = new ArrayList<Order>();
		}
	}

	private void buildLatencyHashmap(int[] marketIDs, int[] marketLatencies) {
		/*
		 * Initalize latency HashMap
		 */
		if (!(marketIDs.length == marketLatencies.length)) {
			World.errorLog.logError("Length of markets array and length of latenceis array must be the same.");
		}

		for (int i = 0; i < marketIDs.length; i++) {
			this.latencyToMarkets.put(World.getMarketByNumber(i), marketLatencies[i]);
		}
	}

	private void buildOrderbooksHashMap(int[] marketIDs, int[] stockIDs) {
		for (int market : marketIDs) {
			for (int stock : stockIDs) {
				this.orderbooks.add(World.getOrderbookByNumbers(stock, market));
			}
		}
	}

	private void buildStocksList(int[] stockIDs) {
		for (int i : stockIDs) {
			Stock stock = World.getStockByNumber(i);
			if (!this.stocks.contains(stock)) {
				this.stocks.add(stock);
			} else {
				World.errorLog.logError("Problem when building stocks ArrayList in HFT agent (buildStockList): Tried to add the same stock more than one.");
			}
		}
	}

	private void initializePortfolio() {
		/*
		 * Initializes portfolio with constant or random amount as specified in
		 * the interface.
		 */
		for (Stock stock : this.stocks) {
			if (HighFrequencyTradingBehavior.randomStartStockAmount) {
				World.errorLog.logError("Random start stock amount not implemented yet!");
			} else {
				this.ownedStocks.put(stock, HighFrequencyTradingBehavior.startStockAmount);
			}
		}
	}

	public void setPortfolio(int[] portfolio) {
		/*
		 * Used to give the agent a ustom portfolio
		 */
		if (portfolio.length != this.stocks.size()) {
			World.errorLog.logError("Error when creating custom specified portfolio. Size of new portfolio does not match size of agetn stock array.");
		}
	}

	// private void getRandomPortfolio(int[] stockIDs){
	// int[] portfolio = new int[stockIDs.length];
	// for(long i = 0; i < stockIDs.length; i++){
	// portfolio[i] = Utils.getNonNegativeGaussianInteger(, std)
	// }
	// }

	/*
	 * World calls this function when it's time for the agent to trade
	 */

	public void requestMarketInformation() {
		/*
		 * Updates the agents wakeup time, which depends on the agents strategy
		 * (that is, which markets he wants information from).
		 */
		// World.registerAgentAsWaiting(this);
		this.wakeupTime = World.getCurrentRound() + this.getWaitingTime();
	}

	public void receiveMarketInformation() throws NoOrdersException {
		/*
		 * Method that deals with flow -Update agent wakeup time by adding
		 * thinking time. -Read the received information and update internal
		 * data structures
		 */
		World.agentRequestMarketInformation(this);
		this.wakeupTime += this.thinkingTime;
		try {
			this.storeMarketInformation();
		} catch (NoOrdersException e) {
			throw e;
		}
	}

//	private void updateKnowledgeAboutTotalAgentSideVolumeOfCurrentStandingOrders() {
//		long totalBuyVolume = 0l;
//		long totalSellVolume = 0l;
////		Iterator<Orderbook> buySideIterator = this.standingBuyOrders.keySet().iterator();
////		while(buySideIterator.hasNext()) {
////			totalBuyVolume += this.standingBuyOrders.get(buySideIterator.next()).getCurrentAgentSideVolume();
////		}
//		
//		/*
//		 * Setting all entries to zero
//		 */
//		Set<Stock> buyStocks= this.numberOfStocksInStandingBuyOrders.keySet();
//		for(Stock stock:buyStocks) {
//			this.numberOfStocksInStandingBuyOrders.put(stock, 0l);
//		}
//		Set<Stock> sellStocks = this.numberOfStocksInStandingSellOrders.keySet();
//		for(Stock stock:sellStocks) {
//			this.numberOfStocksInStandingSellOrders.put(stock, 0l);
//		}
//		
//		Collection<Order> standingBuyOrders = this.standingSellOrders.values();
//		for(Order buyOrder:standingBuyOrders) {
//			Stock stock = buyOrder.getStock();
//			totalSellVolume += buyOrder.getCurrentAgentSideVolume();
//		}
//		
//		Collection<Order> standingSellOrders = this.standingSellOrders.values();
//		for(Order sellOrder:standingSellOrders) {
//			totalSellVolume += sellOrder.getCurrentAgentSideVolume();
//		}
//		
//		this.numberOfStocksInStandingBuyOrders.put(stock, totalBuyVolume);
//		this.numberOfStocksInStandingSellOrders.put(stock, totalSellVolume);
//	}

	private void updatePortfolio(Stock stock, long volume, Order.BuySell buysell) {
		/*
		 * THis function does two things, because they must always be done at the same time. 
		 * Firstly, it updates the portfolio, that is the number of stocks that the agent owns
		 * Secondly, it updates his knowledge about how many stocks the agent is currently trying to sell or buy.
		 * That is, the sum of the volumes of his current standing orders.
		 * These two things must be done at the same time because, whenever there is a change in the agent's
		 * portfolio, this must be because a trade, involving one of his orders, was executed.
		 * There are special cases where the agent cannot fulfill the volume that is required 
		 * by the receipt. When that happens, the agent will only update his
		 * portfolio by the volume that he is able to pay, and the change of volume in his standing orders
		 * must also be the same.  
		 */
		
		long currentlyHolds = this.ownedStocks.get(stock);
		if(buysell == Order.BuySell.BUY) {
			this.ownedStocks.put(stock, currentlyHolds + volume);
		} else if (buysell == Order.BuySell.SELL) {
			this.ownedStocks.put(stock, currentlyHolds - volume);
		}
		this.updateNumberOfStocksInStandingOrders(stock, buysell, volume, HFT.agentAction.UPDATE_ORDER);
	}

	public void receiveTransactionReceipt(TransactionReceipt receipt) {
		this.eventlog.logAgentAction(String.format("Agent %s received a receipt for a %s order, id: %s", this.id, receipt.getBuySell(), receipt.getFilledOrder().getID()));

		Stock stock = receipt.getStock();
		BuySell buysell = receipt.getFilledOrder().getBuySell();
		long borrowedCash = 0l;
		long numberOfBorrowedStocks = 0l;
		long tradedVolume = 0l;
		long tradeTotal = 0;
		
		if (buysell == BuySell.BUY) {
			/*
			 * The agent is required to buy stocks, so we check to see if he has
			 * enough cash.
			 */
			if (this.standingBuyOrders.containsKey(receipt.getOrderbook())) {
				this.nReceivedBuyOrderReceipts += 1;
				this.updateStandingOrder(receipt);
			} else {
				this.dealWithOrderForRemovedOrder(receipt);
			}

			if (receipt.getTotal() > this.cash) {
				/*
				 * The agent has to borrow money
				 */
				if(MarketRules.agentMustBuyAllStocksAsSpecifiedInReceipt) {
					borrowedCash = Math.abs(this.cash - receipt.getTotal());
					this.borrowCash(borrowedCash);
					tradeTotal = receipt.getTotal();
					tradedVolume = receipt.getUnsignedVolume();
				} else {
					tradeTotal = this.cash;
					/*
					 * Calculate the maximum number of stocks that the agent can afford
					 */
					tradedVolume = (long) Math.floor((double ) tradeTotal / (double) receipt.getPrice());
					if(receipt.getFillingOrder().getOwner() != null) {
						World.errorLog.logError(String.format("Agent %s did only buy %s stocks for an order requiring him to sell %s stocks, but another transaction recept was issued to agent %s with the full amount",
															this.id, tradedVolume, receipt.getUnsignedVolume(), receipt.getFillingOrder().getOwner().getID()));
					} else {
						World.warningLog.logOnelineWarning(String.format("Agent %s did only buy %s stocks for an order requiring him to sell %s stocks, but the opposing trader was a Stylized trader",
															this.id, tradedVolume, receipt.getUnsignedVolume()));
					}
				}
			} else {
				/*
				 * The agent has enough cash to buy the whole volume without further ado
				 */
				tradeTotal = receipt.getTotal();
				tradedVolume = receipt.getUnsignedVolume();
			}
			
		} else if (buysell == Order.BuySell.SELL) {

			if (this.standingSellOrders.containsKey(receipt.getOrderbook())) {
				this.nReceivedSellOrderReceipts += 1;
				// if(this.ownedStocks.get(receipt.getStock() <= 0))
				this.updateStandingOrder(receipt);
			} else {
				this.dealWithOrderForRemovedOrder(receipt);
			}
			/*
			 * The agent is required to sell stocks, so we check to see if he has them.
			 */
			long currentlyHoldingNumberOfStocks = this.ownedStocks.get(stock);
			
			
			if(MarketRules.agentMustSellAllStocksAsSpecifiedInReceipt) {
				/*
				 * If the agent is required to fulfill the order, we have to check some things.
				 */
				if(receipt.getUnsignedVolume() > currentlyHoldingNumberOfStocks) {
					/*
					 * Agent has to borrow stocks
					 */
					numberOfBorrowedStocks = this.borrowStocks(stock, receipt.getUnsignedVolume() - currentlyHoldingNumberOfStocks);
					tradedVolume = currentlyHoldingNumberOfStocks + numberOfBorrowedStocks;
				} else {
					/*
					 * Agent does not have to borrow stocks, that is, he has enough to sell the full volume
					 */
					tradedVolume = receipt.getUnsignedVolume();
				}
				tradeTotal = receipt.getTotal();
			} else {
				/*
				 * If he is not required to sell the full amount, just sell as many as he has or as many as the receipt requires him to,
				 * whichever is the smallest volume.
				 */
				tradedVolume = Math.min(currentlyHoldingNumberOfStocks, receipt.getUnsignedVolume());
				World.errorLog.logError("TradeTotal must be assigned a value!");
				if(receipt.getFillingOrder().getOwner() != null) {
					World.errorLog.logError(String.format("Agent %s did only sell %s stocks for an order requiring him to sell %s stocks, but another transaction recept was issued to agent %s with the full amount",
														this.id, tradedVolume, receipt.getUnsignedVolume(), receipt.getFillingOrder().getOwner().getID()));
				} else {
					World.warningLog.logOnelineWarning(String.format("Agent %s did only sell %s stocks for an order requiring him to sell %s stocks, but the opposing trader was a Stylized trader",
														this.id, tradedVolume, receipt.getUnsignedVolume()));
				}
			}
			
			if(tradedVolume < receipt.getUnsignedVolume()) {
				/*
				 * Check for short selling. If shortselling is taking place and if it is allowed, do nothing apart from registering that it happened.
				 */
				if (!MarketRules.allowsShortSelling) {
					World.ruleViolationsLog.logShortSelling(receipt);
					World.errorLog.logError("Short selling was not allowed by market rules, but happened. Handling of this situation is not implemented yet");
				} else {
					this.nTimesAgentDidShortSelling ++;
					this.eventlog.logAgentAction("Agent did short-selling");
				}
			}
			
		} else {
			World.errorLog.logError("Order must be eiter BUY or SELL");
		}
		
		/*
		 * Check trade details and update internal state
		 */
		if(tradedVolume <= 0) {
			World.errorLog.logError("Agent tried to a sell a negative or zero amount of stocks...");
		}
		if(tradeTotal <= 0) {
			World.errorLog.logError("Agent tried to transact a negative or zero total");
		}
		
		this.updatePortfolio(stock, tradedVolume, buysell);
		this.updateCash(buysell, tradeTotal);
		
		this.tradeLog.logTradeData(receipt, tradedVolume, tradeTotal, borrowedCash, numberOfBorrowedStocks);
	}

	private long borrowStocks(Stock stock, long signedRequestedVolumeToBorrow) {
		/*
		 * At the moment, this function does nothing, but it is in here in case we need 
		 * to implement something to handle the situation in which an agent borrows stock.
		 * This could be that the rules don't allow an agent to borrow more than a certain amount, for instance.
		 */
		return signedRequestedVolumeToBorrow;
	}

	
	private void dealWithOrderForRemovedOrder(TransactionReceipt receipt) {
		if (this.orderHistory.contains(receipt.getFilledOrder())) {
			/*
			 * The agent did submit the order at some point, but now it is no
			 * longer in his standing order list. A likely explanation (the only
			 * one that I've come up with so far) is that the order was filled
			 * after the agent issued a cancellation.
			 */
			if (MarketRules.agentPaysWhenOrderIsFilledAfterSendingCancellation) {
				this.nTimesAgentGotReceiptForOrderWhichIsNotInHisStandingOrderList++;
				this.eventlog.logAgentAction(String.format("Agent %s had to fullfill an already cancelled order (id: %s)", receipt.getOwnerOfFilledStandingOrder().getID(), receipt.getFilledOrder().getID()));
			} else {
				World.errorLog.logError(String.format("Agent %s received a transaction receipt for an order that he cancelled (id: %s), but market side handling of this situation has not been implemented yet", this.getID(), receipt.getFilledOrder().getID()));
			}
		} else {
			World.errorLog.logError(String.format("Agent %s received a transaction receipt for an order that he didn't submit (id: %s)", this.getID(), receipt.getFilledOrder().getID()));
		}
	}

//	private void updateStockHoldings(Stock stock, BuySell buysell, long volume) {
//		/*
//		 * 
//		 */
//
//	}

	private void updateStandingOrder(TransactionReceipt receipt) {
		/*
		 * When the agent receives a receipt for a filled order, he has to
		 * update his local list of submitted orders. If the receipt volume is
		 * less than the volume of the filled local order, he just subtracts the
		 * difference from the local order. If they are equal, he removes the
		 * local order. If it is more, he throws an error.
		 */

		long receiptVolume = receipt.getUnsignedVolume();
		long agentHoldingStockVolume = this.ownedStocks.get(receipt.getStock());

		if (receiptVolume > agentHoldingStockVolume) {
			/*
			 * This does not have to mean that the agent did short-selling on
			 * purpose, it's merely count many times he, for some reasons, sold more stocks than he owned
			 */
			this.nTimesSoldStocksWhenAlreadyHadNegativeAmountOfStock++;
		}

		Order standingOrder;
		if (receipt.getBuySell() == BuySell.BUY) {
			standingOrder = this.standingBuyOrders.get(receipt.getOrderbook());
			if (receipt.getUnsignedVolume() == standingOrder.getCurrentAgentSideVolume()) {
				this.standingBuyOrders.remove(receipt.getOrderbook());
			}
		} else {
			standingOrder = this.standingSellOrders.get(receipt.getOrderbook());
			if (receipt.getUnsignedVolume() == standingOrder.getCurrentAgentSideVolume()) {
				this.standingSellOrders.remove(receipt.getOrderbook());
			}
		}

		if (receipt.getUnsignedVolume() < standingOrder.getCurrentAgentSideVolume()) {
			standingOrder.updateAgentSideVolumeByDifference(receipt.getBuySell(), receipt.getUnsignedVolume());
		}

		if (receipt.getUnsignedVolume() > standingOrder.getCurrentAgentSideVolume()) {
			this.eventlog.logAgentAction(String.format("Agent received a transaction receipt for %s units of stock %s at market %s. \n" + "\t For market order, the current agent side volume was %s, and the market side volume was %s", receipt.getUnsignedVolume(), receipt.getStock().getID(), receipt.getFilledOrder().getMarket().getID(), receipt.getFilledOrder().getCurrentAgentSideVolume(), receipt.getFilledOrder().getCurrentMarketSideVolume()));
			if (!MarketRules.allowsShortSelling) {
				World.errorLog.logError("Shortselling was not allowed, but happeed when agent received a transaction receipt for more than ");
			}
		}
	}
	
//	public long getSumOfCashAndPortfolioValue(int round) {
//		
//	}

//	public long getTotalWealth() {
//		int now = World.getCurrentRound();
//		long totalWealth;
//		totalWealth = this.cash;
//		for (Stock stock : this.stocks) {
//			long stockPrice = stock.getStockValueAtRound(now);
//			long nStocks = this.ownedStocks.get(stock);
//			if(Long.MAX_VALUE / nStocks < stockPrice) {
//				World.errorLog.logError("Numeric overflow when calculating trader wealth");
//			} else {
//				totalWealth += nStocks * stockPrice;	
//			}
//			
//			
//		}
//		return totalWealth;
//	}
	
	public long evaluatePortfolioValue(int round) {
		long valueOfHeldStocks = 0;
		for (Stock stock : this.stocks) {
			long nStocks = this.ownedStocks.get(stock);
			
			if(nStocks == 0) {
				
			} else if(nStocks > 0){
				/*
				 * Agent could sell his stocks
				 */
				valueOfHeldStocks += nStocks * stock.getGlobalHighestSellPrice(round); 
			} else if(nStocks < 0) {
				valueOfHeldStocks += nStocks * stock.getGlobalLowestBuyPrice(round);
				this.eventlog.logAgentAction(String.format("Agent had negative amount of stock"));
				//				World.errorLog.logError("Agents are not allowed to hold negative amount of stock");
			}
			
//			long stockPrice = stock.get
//			if(Long.MAX_VALUE / nStocks < stockPrice) {
//				World.errorLog.logError("Numeric overflow when calculating trader wealth");
//			} else {
//				totalWealth += nStocks * stockPrice;	
//			}
			
			
		}
		return valueOfHeldStocks;
	}

	public long getCash() {
		return cash;
	}

	public void updateCash(Order.BuySell buysell, long amount) {
		if(amount <0) {
			World.errorLog.logError(String.format("argument 'amount' must bepositive, but was %s", amount));
		}
		if(buysell == Order.BuySell.BUY) {
			/*
			 * The agent is BUYING stocks, so the change in his cash holdings must be negative.
			 */
			this.cash -= amount;
		} else if (buysell == Order.BuySell.SELL){
			/*
			 * The agent is SELLING stocks, so the change in his cash holdings must be positive
			 */
			this.cash += amount;
		}
		if (this.cash < 0) {
			World.errorLog.logError("Agent has negative cash, but he did not use the borrow function");
		}
	}

	public void borrowCash(long positiveAmount) {
		if (positiveAmount < 0) {
			World.errorLog.logError("An agent tried to borrow a negative amount of money. Amount must be positive");
		} else {
			this.nTimesBorrowedCash++;
			/*
			 * Call update cash with SELL (meaning that the agent GETS money
			 */
			this.updateCash(Order.BuySell.SELL, positiveAmount);
			this.eventlog.logAgentAction(String.format("Agent %s borrowed %s cash", this.id, positiveAmount));
		}
	}

	public long getID() {
		return id;
	}

	public int getLatency(Market market) {
		return this.latencyToMarkets.get(market);
	}

	public ArrayList<Orderbook> getOrderbooks() {
		return this.orderbooks;
	}

	public long getWakeupTime() {
		return this.wakeupTime;
	}

	public long getArrivalTimeToMarket(Market market) {
		return this.getLatency(market) + World.getCurrentRound();
	}

	public static long getRandomInitialTraderWealth() {
		Random r = new Random();
		long w = 0;
		while (w <= 0) {
			w = (int) Math.rint(r.nextGaussian() * wealthStd + wealthMean);
		}
		return w;
	}

	public void hibernate() {
		this.wakeupTime = World.getCurrentRound() + emptyOrderbookWaitTime;
	}
	
	protected void updateNumberOfStocksInStandingOrders(Stock stock, Order.BuySell buysell, long volume, HFT.agentAction action) {
		/*
		 *  Implements agent keeping track of how much he is currently selling of each stock.
			-If he cancels an order, then subtract, no matter if buy or sell.
			-If he created an order, then add
			-If he updates an order, this is because a trade involving one of his standing orders was executed. 
			 Then, no matter if he buys or sells, the volume of stocks in his standing orders is decreased. Hence, subtract the amount that he ends up trading.


		 */
		if(action == HFT.agentAction.CANCEL_ORDER) {
			volume = -Math.abs(volume);
		} else if(action == HFT.agentAction.UPDATE_ORDER) {
			volume = -Math.abs(volume);
		} else if(action == HFT.agentAction.SUBMIT_ORDER) {
			volume = Math.abs(volume);
		}
		
		if(buysell == Order.BuySell.BUY) {
			long nCurrentStocksInBuyOrders = this.numberOfStocksInStandingBuyOrders.get(stock);
			nCurrentStocksInBuyOrders += volume;
			this.numberOfStocksInStandingBuyOrders.put(stock, nCurrentStocksInBuyOrders);
		} else if(buysell == Order.BuySell.SELL) {
			long nCurrentStocksInSellOrders = this.numberOfStocksInStandingSellOrders.get(stock);
			nCurrentStocksInSellOrders += volume;
			this.numberOfStocksInStandingSellOrders.put(stock, nCurrentStocksInSellOrders);
		}
		
	}

	public HashMap<Stock, Long> getNumberOfStocksInStandingSellOrders() {
		return numberOfStocksInStandingSellOrders;
	}

	public HashMap<Stock, Long> getNumberOfStocksInStandingBuyOrders() {
		return numberOfStocksInStandingBuyOrders;
	}

	protected void submitOrder(Order order) {
		if (order.getBuySell() == Order.BuySell.BUY) {
			this.standingBuyOrders.put(order.getOrderbook(), order);
//			Stock stock = order.getStock();
//			long orderVolume = order.getUnsignedInitialVolume();
//			long newOwnedAmount = this.numberOfStocksInStandingBuyOrders.get(stock) + orderVolume;
//			this.numberOfStocksInStandingBuyOrders.put(order.getStock(), newOwnedAmount);
			this.nSubmittedBuyOrders += 1;
		} else {
			this.standingSellOrders.put(order.getOrderbook(), order);
//			long newOwnedAmount = this.numberOfStocksInStandingSellOrders.get(order.getStock()) + order.getUnsignedInitialVolume();
//			this.numberOfStocksInStandingSellOrders.put(order.getStock(), newOwnedAmount);
			this.nSubmittedSellOrders += 1;
		}
		if (keepOrderHistory) {
			this.orderHistory.add(order);
		}
		/*
		 * Verify that the volume is the same as the initial volume
		 */
		this.updateNumberOfStocksInStandingOrders(order.getStock(), order.getBuySell(), order.getCurrentAgentSideVolume(), HFT.agentAction.SUBMIT_ORDER);
	}

	public static HashMap<Market, Integer> getRandomLatencyHashMap(Market[] markets) {
		HashMap<Market, Integer> latencyMap = new HashMap<Market, Integer>();

		for (Market market : markets) {
			int latency = Utils.getRandomUniformInteger(minimumLatency, maximumLatency);
			latencyMap.put(market, latency);
		}
		return latencyMap;
	}

	protected void cancelOrder(OrderCancellation cancellation) {
		Order order = cancellation.getOrder();
//		if(order.getBuySell() == Order.BuySell.BUY) {
//			long nCurrentStocksInBuyOrders = this.numberOfStocksInStandingBuyOrders.get(order.getStock());
//			nCurrentStocksInBuyOrders -= order.getCurrentAgentSideVolume();
//			this.numberOfStocksInStandingBuyOrders.put(order.getStock(), nCurrentStocksInBuyOrders);
//		} else if(order.getBuySell() == Order.BuySell.SELL) {
//			long nCurrentStocksInSellOrders = this.numberOfStocksInStandingSellOrders.get(order.getStock());
//			nCurrentStocksInSellOrders -= order.getCurrentAgentSideVolume();
//			this.numberOfStocksInStandingSellOrders.put(order.getStock(), nCurrentStocksInSellOrders);
//		}
		this.updateNumberOfStocksInStandingOrders(order.getStock(), order.getBuySell(), order.getCurrentAgentSideVolume(), HFT.agentAction.CANCEL_ORDER);
		this.nSubmittedCancellations += 1;
	}
	
	public long getTotalAgentWorth() {
		return this.cash + this.evaluatePortfolioValue(World.getCurrentRound());
	}

	public long getnSubmittedBuyOrders() {
		return nSubmittedBuyOrders;
	}

	public long getnSubmittedSellOrders() {
		return nSubmittedSellOrders;
	}

	public long getnReceivedBuyOrderReceipts() {
		return nReceivedBuyOrderReceipts;
	}

	public long getnReceivedSellOrderReceipts() {
		return nReceivedSellOrderReceipts;
	}

	public long getnFullfilledOrders() {
//		return this.nReceivedBuyOrderReceipts + this.nReceivedSellOrderReceipts;
		return 0;
	}

	public long getnSubmittedOrders() {
		return this.nSubmittedBuyOrders + this.nSubmittedSellOrders;
	}

	public long getnStandingBuyOrders() {
		return this.standingBuyOrders.size();
	}

	public long getnStandingSellOrders() {
		return this.standingSellOrders.size();
	}

	public long getnOrderCancellations() {
		return this.nSubmittedCancellations;
	}

	public HashMap<Orderbook, Order> getStandingBuyOrders() {
		return standingBuyOrders;
	}

	public HashMap<Orderbook, Order> getStandingSellOrders() {
		return standingSellOrders;
	}

	public ArrayList<Order> getOrderHistory() {
		return this.orderHistory;
	}

	public HashMap<Stock, Long> getOwnedStocks() {
		return this.ownedStocks;
	}

	public ArrayList<Stock> getActiveStocks() {
		return this.stocks;
	}

	public int getTransmissionDelayToMarket(Market market) {
		return this.latencyToMarkets.get(market);
	}

}
