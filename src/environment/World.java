package environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import log.WorldLogger;
import setup.SimulationSetup;
import setup.WorldObjectHandler;
import utilities.AgentWakeupComparator;
import utilities.MessageArrivalTimeComparator;
import utilities.NoOrdersException;
import utilities.StockMarketPair;
import agent.HFT;
import agent.StylizedTrader;

//import umontreal.iro.lecuyer.stochprocess.GeometricBrownianMotion;

public class World implements SimulationSetup {
	private enum roundPhases{
		MESSAGES_ARRIVE,
		STYLIZED_TRADING,
		HFT_TRADING,
		UPDATE_ORDERBOOKS,
		LOGGING;
	}
	
	/*
	 * References to stocks, markets and agents (indexed by IDs)
	 */
	private static HashMap<String, HFT> agentsByID = new HashMap<String, HFT>();
	private static HashMap<StockMarketPair, Orderbook> orderbooksByPair = new HashMap<StockMarketPair, Orderbook>();
	private static ArrayList<Stock> stocks = new ArrayList<Stock>();
	private static ArrayList<HFT> agents = new ArrayList<HFT>();
	private static ArrayList<Market> markets = new ArrayList<Market>();
	private static ArrayList<Orderbook> orderbooks = new ArrayList<Orderbook>();

	/*
	 * Priority queues for messages
	 */
	private static MessageArrivalTimeComparator messageArrivalTimeComparator = new MessageArrivalTimeComparator();
	private static PriorityQueue<Order> ordersInTransit = new PriorityQueue<Order>(100, messageArrivalTimeComparator);
	private static PriorityQueue<TransactionReceipt> receiptsInTransit = new PriorityQueue<TransactionReceipt>(100, messageArrivalTimeComparator);
	private static PriorityQueue<OrderCancellation> orderCancellationsInTransit = new PriorityQueue<OrderCancellation>(100, messageArrivalTimeComparator);

	/*
	 * Data structures for managing agents
	 */
	private static AgentWakeupComparator agentWakeupComparator = new AgentWakeupComparator();
	private static PriorityQueue<HFT> waitingAgents = new PriorityQueue<HFT>(10, agentWakeupComparator);
	private static PriorityQueue<HFT> thinkingAgents = new PriorityQueue<HFT>(10, agentWakeupComparator);
	private static ArrayList<HFT> freeAgents = new ArrayList<HFT>();
	
	/*
	 * Loggers
	 */
	public static WorldLogger warningLog;
	public static WorldLogger errorLog;
	public static WorldLogger eventLog;
	public static WorldLogger ruleViolationsLog;
	public static WorldLogger dataLog;

	/*
	 * Other internal variables
	 */
	public static final long creationTime = System.currentTimeMillis();
	public static long runTime = 0;
	private static int currentRound = 0;
	private roundPhases roundPhase;
	
	public static void setupEnvironment() {
		WorldObjectHandler.createStocks();
		WorldObjectHandler.createMarkets();
		WorldObjectHandler.createOrderbooks();
		WorldObjectHandler.createAgents();
		WorldObjectHandler.createObjectLoggers();
		WorldObjectHandler.initializeEmptyOrderbooksWithMarketOrders();
	}

	public static void executeInitalRounds(long nRounds) {
		for (long round = 0; round < nRounds; round++) {
			expireOrdersInAllOrderbooks();
			prepareNewRound();
			dispatchArrivingReceipts();
			dispatchOrderCancellations();
			processOrderCancellationsInAllOrderbooks();
			createSlowTraderOrders();
			dispatchArrivingOrders();
			processNewOrdersInAllOrderbooks();
			logRoundBasedData();
		}
	}

	public static void executeRound() {
		prepareNewRound();
		if(currentRound % 500 == 0) {
			System.out.println(String.format("Round %s", currentRound));
		}
		/*
		 * Increment time variable Update fundamental prices for each stock.
		 */
		
		/*
		 * Receipts from the previous rounds are dispatched. The order does not
		 * matter, so there's no need to randomize it.
		 */
		dispatchArrivingReceipts();
		/*
		 * Cancellations reach the order books and are stored there.
		 */
		dispatchOrderCancellations();

		dispatchArrivingOrders();
		/*
		 * The new cancellations are processed in random order.
		 */
		createSlowTraderOrders();
		/*
		 * HFT trade part.
		 */
		allAgentsRequestMarketInformation();
		allAgentsReceiveMarketInformation();
		agentsEvaluateMarketInformation();

		/*
		 * Arriving orders from HFTs and slow traders are transferred to the
		 * waiting lists in the order books
		 */
		processOrderCancellationsInAllOrderbooks();
		expireOrdersInAllOrderbooks();
		/*
		 * Iterates over orderbook and processes orders in random order.
		 */
		processNewOrdersInAllOrderbooks();

		updateGlobalKnowledge();
		
		logRoundBasedData();
	}

	private static void updateGlobalKnowledge() {
		/*
		 * This function contains calls that must be executed after all trading activity has ended for the round,
		 * but before round based logging is done.
		 * 
		 * Later, this method will contains calls for confirming the validity of trades, and other things.
		 * 
		 */
		World.runTime = System.currentTimeMillis() - World.creationTime;

		for(Stock stock:stocks) {
			stock.collectGlobalBestPricesAtEndOfRound();
		}
		
	}

	private static void logRoundBasedData() {
		World.logStockPrices();
		World.logAgentData();
		World.dataLog.recordEntry();
		
	}

	private static void logAgentData() {
		// TODO Auto-generated method stub
		for(HFT agent:World.agents){
			agent.roundDatalog.recordDataEntryAtEndOfRound();
		}
	}

	private static void logStockPrices() {
		for (Stock stock : stocks) {
			stock.roundBasedDatalog.recordEndRoundPriceInformation(World.currentRound);
		}
	}

	private static void createSlowTraderOrders() {
		for (long i = 0; i < SimulationSetup.nSlowTraderOrdersPerRounds; i++) {
			StylizedTrader.submitRandomOrder();
		}
//		System.out.println("k");
	}

	private static void updateFundamental() {
		for (Stock stock : World.stocks) {
			stock.updateFundamentalPrice();
		}
	}

	

	public static void prepareNewRound() {
		currentRound++;
		updateFundamental();
	}

	private static void dispatchOrderCancellations() {
		/*
		 * Transfer order cancellations from in transit to the target orderbook.
		 */
		long nArrivingCancellations = 0;
		while (true) {
			try {
				OrderCancellation cancellation = orderCancellationsInTransit
						.element();
				if (cancellation.getArrivalTime() == World.getCurrentRound()) {
					nArrivingCancellations++;
					orderCancellationsInTransit.remove();
					Orderbook targetOrderbook = cancellation.getOrder()
							.getOrderbook();
					targetOrderbook.receieveOrderCancellation(cancellation);
				} else {
					break;
				}
			} catch (NoSuchElementException e) {
				break;
			}
		}
		World.dataLog.setnArrivingOrderCancellations(nArrivingCancellations);
//		World.eventLog.logOnelineEvent(String.format(
//				"%s order cancellations reached orderbooks.", nArrivingCancellations));
	}

	private static void allAgentsRequestMarketInformation() {
		/*
		 * Loop through active agents and request information. Put them on the
		 * waiting queue with priority currentTime + agent.getWaitingTime();
		 * Remove them from active list.
		 */
		long nInformationRequests = 0;
		Iterator<HFT> iterator = freeAgents.iterator();
		while (iterator.hasNext()) {
			HFT agent = iterator.next();
			iterator.remove();
			agentRequestMarketInformation(agent);
			nInformationRequests++;
		}
		World.dataLog.setNInformationRequests(nInformationRequests);
//		World.eventLog.logOnelineEvent(String.format(
//				"%s agents requested market information", nInformationRequests));
		// try{
		//
		// for(HFT agent:freeAgents){
		// freeAgents.remove(agent);
		// agentRequestMarketInformation(agent);
		// i++;
		// }
		// World.eventLog.logGeneralEvent(String.format("%s agents requested market information",
		// i));
		// }catch(ConcurrentModificationException e){
		// e.printStackTrace();
		// }
		// agent.requestMarketInformation();

	}

	public static void agentRequestMarketInformation(HFT agent) {
		agent.requestMarketInformation();
		addAgentToWaitingQueue(agent);
	}

	private static void addAgentToWaitingQueue(HFT agent) {
		freeAgents.remove(agent);
		if (!waitingAgents.contains(agent)) {
			waitingAgents.add(agent);
		}
		thinkingAgents.remove(agent);
	}

	private static void addAgentToThinkingQueue(HFT agent) {
		freeAgents.remove(agent);
		waitingAgents.remove();
		if (!thinkingAgents.contains(agent)) {
			thinkingAgents.add(agent);
		}
	}

	private static void addAgentToFreeList(HFT agent) {
		if (!freeAgents.contains(agent)) {
			freeAgents.add(agent);
		}
		waitingAgents.remove(agent);
		thinkingAgents.remove(agent);
	}

	private static void allAgentsReceiveMarketInformation() {
		/*
		 * Loop through agents in priority queue with priority equals current
		 * time. Update priority by their thinking time.
		 */
		long nReceivedMarketInformation = 0;
		long nReRequest = 0;
		while (true) {
			try {
				HFT agent = waitingAgents.element();
				if (agent.getWakeupTime() == World.getCurrentRound()) {
					nReceivedMarketInformation++;
					boolean reRequest = agentReceiveMarketInformation(agent);
					if(reRequest) {
						nReRequest++;
					}
				} else {
					break;
				}
			} catch (NoSuchElementException e) {
				break;
			}
		}
		World.dataLog.setnReceiveMarketInformation(nReceivedMarketInformation);
		World.dataLog.setnReRequestionMarketInformation(nReRequest);
//		World.eventLog.logOnelineEvent(String.format(
//				"%s agents received market information.", nReceivedMarketInformation));
	}

	private static boolean agentReceiveMarketInformation(HFT agent) {
		try {
			agent.receiveMarketInformation();
			addAgentToThinkingQueue(agent);
			return true;
		} catch (NoOrdersException e) {
			return false;
//			World.eventLog
//					.logOnelineEvent(String
//							.format("Agent %s did not evaluate strategy, but requested new market information",
//									agent.getID()));
		}
	}

	private static void agentsEvaluateMarketInformation() {
		/*
		 * Loop through agents in priority queue with priority equals current
		 * time. Agents sumbit orders Add agents on active list.
		 */
		long nFinishedEvaluating = 0;
		while (true) {
			try {
				HFT agent = thinkingAgents.element();
				if (agent.getWakeupTime() == World.getCurrentRound()) {
					nFinishedEvaluating++;
					agentEvaluateMarketInformation(agent);
				} else {
					break;
				}
			} catch (NoSuchElementException e) {
				break;
			}
		}
		World.dataLog.setnFinishedEvaluatingStrategy(nFinishedEvaluating);
//		World.eventLog.logOnelineEvent(String.format(
//				"%s agents finished evaluating their strategy.", i));
	}

	private static void agentEvaluateMarketInformation(HFT agent) {
		if (agent.executeStrategyAndSubmit()) {
			addAgentToFreeList(agent);
		} else {
			warningLog.logError(String.format("Agent %s failed to evaluate strategy", agent.getID()));
		}
	}

	public static void dispatchArrivingOrders() {
		/*
		 * When orders arrive to markets the object is cloned. This is necessary
		 * because there essentially exists (at least) two "versions" of an
		 * order: The agent's local copy and the market's local copy. These two
		 * are the same as long as nothing happens in the market, but as soon as
		 * the order is involved in a trade or an agent decides to update an
		 * order, the two are different for a period of time.
		 */
		long nArrivingOrders = 0;
		while (true) {
			try {
				Order order = ordersInTransit.element();
				if (order.getArrivalTime() == World.currentRound) {
					nArrivingOrders++;
					ordersInTransit.remove();
					order.getOrderbook().receiveOrder(order);
				} else if (order.getArrivalTime() < World.currentRound){
					World.warningLog.logOnelineWarning(String.format("Current round is %s but order with arrival time %s exist in queue.", currentRound, order.getArrivalTime()));
					Exception e = new Exception();
					e.printStackTrace();
				} else if(order.getArrivalTime() > World.currentRound) {
					World.eventLog.logOnelineEvent(String.format("After dispatching %s orders this round there were %s order still transit.", nArrivingOrders, ordersInTransit.size()));
					break;
				}
			} catch (NoSuchElementException e) {
				World.eventLog.logOnelineEvent(String.format("After dispatching %s orders this round there were no more orders in transit.", nArrivingOrders));
				break;
			}
		}
		World.dataLog.setnArrivingOrders(nArrivingOrders);
//		World.eventLog.logOnelineEvent(String.format(
//				"%s orders arrived to orderbooks.", i));

		//
		//
		// if(ordersInTransit.peek() != null){
		// try{
		// while(ordersInTransit.peek().getArrivalTime() == currentTime){
		// Order order = ordersInTransit.remove();
		// order.getOrderbook().receiveOrder(order);
		// //
		// WarningLogger.logWarning("Warning; The order was not cloned, so the orderbook and agent refers to the same Order object"
		// + Logger.getFormattedStackTrace());
		// // Order orderClone = Order.getClone(order);
		// // orderClone.getOrderbook().receiveOrder(orderClone);
		// }
		// } catch(NullPointerException e){
		// World.eventLog.logGeneralEvent("No orders arriving to orderbooks this round.");
		// }
		// } else{
		// World.eventLog.logGeneralEvent("No orders in transit this round.");
		// }
	}

	public static void dispatchArrivingReceipts() {
		long nArrivingReceipts = 0;
		while (true) {
			try {
				TransactionReceipt receipt = receiptsInTransit.element();
				if (receipt.getArrivalTime() == World.getCurrentRound()) {
					nArrivingReceipts++;
					receiptsInTransit.remove();
					HFT recipient = receipt.getOwnerOfFilledStandingOrder();
					recipient.receiveTransactionReceipt(receipt);
				} else {
					break;
				}
			} catch (NoSuchElementException e) {
				break;
			}
		}
		World.dataLog.setnArrivingReceipts(nArrivingReceipts);
//		World.eventLog..(String.format(
//				"%s receipts reached agents.", nArrivingReceipts));

		// try{
		// while(receiptsInTransit.peek().getArrivalTime() == currentTime){
		// TransactionReceipt receipt = receiptsInTransit.remove();
		// HFT trader = receipt.getOwner();
		// trader.updatePortfolio(receipt);
		// }
		// } catch(NullPointerException e){
		// World.eventLog.logGeneralEvent("No receipts in transit in this round");
		// }
	}

	public static void processNewOrdersInAllOrderbooks() {
		for (Orderbook orderbook : orderbooks) {
			orderbook.processAllNewOrders();
		}
	}

	public static void expireOrdersInAllOrderbooks() {
		long totalExpiredOrders = 0;
		for (Orderbook orderbook : orderbooks) {
			totalExpiredOrders += orderbook.expireOrders();
		}
		World.dataLog.setnExpiredOrders(totalExpiredOrders);
	}

	public static void processOrderCancellationsInAllOrderbooks() {
		for (Orderbook orderbook : orderbooks) {
			orderbook.processAllCancellations();
		}
	}

	public static void addMarket(Market market) {
		markets.add(market);
		// marketsByID.put(market.getID(), market);
	}

	public static void addStock(Stock stock) {
		stocks.add(stock);
	}

	public static void destroyOrder() {
		// World.warningLog.logWarning("WARNING: DESTROYING ORDERS NOT IMPLEMENTED YET!!");
	}

	public static void addTransactionReceipt(TransactionReceipt receipt) {
		receiptsInTransit.add(receipt);
	}

	public static void addNewOrder(Order order) {
//		Order orderOrderbookSide = orderAgentSide.getCopy();
		ordersInTransit.add(order);
	}

	public static void addOrderCancellation(OrderCancellation update) {
		orderCancellationsInTransit.add(update);
	}

	public static int getCurrentRound() {
		return currentRound;
	}

	public static void setTime(int time) {
		currentRound = time;
	}

	public static void executeNRounds(long N) {
		for (long round = 0; round < N; round++) {
			World.executeRound();
		}
	}

	public static Stock getStockByNumber(int index) {
		Stock stock = null;
		try {
			stock = stocks.get(index);
		} catch (IndexOutOfBoundsException e) {
			// e.printStackTrace();
			World.errorLog.logError(String.format(
					"Stock number %d has not been created.", index));
			System.exit(1);
		}
		return stock;

		// return stocksByID;
	}
	
	public static Market getMarketByNumber(int index) {
		Market market = null;
		try {
			market = markets.get(index);
		} catch (IndexOutOfBoundsException e) {
			World.errorLog.logError(String.format(
					"Market number %d has not been created.", index));
		}
		return market;
	}

	public static HashMap<String, HFT> getAgentByNumber() {
		return agentsByID;
	}

	public static Orderbook getOrderbookByPair(StockMarketPair pair) {
		return orderbooksByPair.get(pair);
	}

	public static Orderbook getOrderbookByObjects(Stock stock, Market market) {
		StockMarketPair pair = new StockMarketPair(stock, market);
		if (orderbooksByPair.containsKey(pair)) {
			return orderbooksByPair.get(pair);
		} else {
			World.errorLog
					.logError(String
							.format("Orderbook for stock %d and market %d has not been created yet!",
									stock, market));
			// System.exit(1);
			return null;
		}
	}

	public static Orderbook getOrderbookByNumbers(int stockID, int marketID) {
		StockMarketPair pair = new StockMarketPair(stocks.get(stockID), markets.get(marketID));
		if (orderbooksByPair.containsKey(pair)) {
			return orderbooksByPair.get(pair);
		} else {
			World.errorLog
					.logError(String
							.format("Orderbook for stock %d and market %d has not been created yet!",
									stockID, marketID));
			// System.exit(1);
			return null;
		}

	}

	public static void addNewAgent(HFT agent) {
		freeAgents.add(agent);
		agents.add(agent);
	}

	public static MessageArrivalTimeComparator getArrivalTimeComparator() {
		return messageArrivalTimeComparator;
	}

	public static PriorityQueue<Order> getOrdersInTransit() {
		return ordersInTransit;
	}

	public static PriorityQueue<TransactionReceipt> getReceiptsInTransit() {
		return receiptsInTransit;
	}

	public static ArrayList<Stock> getStocks() {
		return stocks;
	}

	public static ArrayList<HFT> getHFTAgents() {
		return agents;
	}

	public static ArrayList<Orderbook> getOrderbooks() {
		return orderbooks;
	}

	public static ArrayList<Market> getMarkets() {
		return markets;
	}

	public static HashMap<StockMarketPair, Orderbook> getOrderbooksByPair() {
		return orderbooksByPair;
	}

}
