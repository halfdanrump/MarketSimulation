package environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import log.WorldLogger;
import setup.SimulationSetup;
import utilities.AgentWakeupComparator;
import utilities.MessageArrivalTimeComparator;
import utilities.StockMarketPair;
import Experiments.Experiment;
import agent.HFT;
import agent.StylizedTrader;

//import umontreal.iro.lecuyer.stochprocess.GeometricBrownianMotion;

public class World implements SimulationSetup {
	private Experiment experiment;
	
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
	private HashMap<String, HFT> agentsByID;
	private HashMap<StockMarketPair, Orderbook> orderbooksByPair;
	private ArrayList<Stock> stocks;
	private ArrayList<HFT> agents;
	private ArrayList<Market> markets;
	private ArrayList<Orderbook> orderbooks;

	/*
	 * Priority queues for messages
	 */
	private MessageArrivalTimeComparator messageArrivalTimeComparator;
	private PriorityQueue<Order> ordersInTransit;
	private PriorityQueue<TransactionReceipt> receiptsInTransit;
	private PriorityQueue<OrderCancellation> orderCancellationsInTransit;

	/*
	 * Data structures for managing agents
	 */
	private AgentWakeupComparator agentWakeupComparator;
	private PriorityQueue<HFT> waitingAgents;
	private PriorityQueue<HFT> thinkingAgents;
	private ArrayList<HFT> freeAgents;
	
	/*
	 * Loggers
	 */
	public WorldLogger warningLog;
	public WorldLogger errorLog;
	public WorldLogger eventLog;
	public WorldLogger ruleViolationsLog;
	public WorldLogger dataLog;

	/*
	 * Other internal variables
	 */
	public long creationTime;
	public long runTime;
	private int currentRound;
	private roundPhases roundPhase;
	
	public World() {
		this.creationTime = System.currentTimeMillis();
		this.runTime = 0;
		this.currentRound = 0;
		
		this.agentsByID =  new HashMap<String, HFT>();
		this.orderbooksByPair =  new HashMap<StockMarketPair, Orderbook>();
		this.stocks =  new ArrayList<Stock>();
		this.agents =  new ArrayList<HFT>();
		this.markets =  new ArrayList<Market>();
		this.orderbooks =  new ArrayList<Orderbook>();
		this.messageArrivalTimeComparator = new MessageArrivalTimeComparator();
		this.ordersInTransit =  new PriorityQueue<Order>(100, messageArrivalTimeComparator);
		this.receiptsInTransit =  new PriorityQueue<TransactionReceipt>(100, messageArrivalTimeComparator);
		this.orderCancellationsInTransit =  new PriorityQueue<OrderCancellation>(100, messageArrivalTimeComparator);
		this.agentWakeupComparator = new AgentWakeupComparator();
		this.waitingAgents =  new PriorityQueue<HFT>(10, agentWakeupComparator);
		this.thinkingAgents =  new PriorityQueue<HFT>(10, agentWakeupComparator);
		this.freeAgents =  new ArrayList<HFT>();
	}

	public void executeInitalRounds(long nRounds, Experiment experiment) {
		for (long round = 0; round < nRounds; round++) {
			expireOrdersInAllOrderbooks();
			prepareNewRound();
			dispatchArrivingReceipts();
			dispatchOrderCancellations();
			processOrderCancellationsInAllOrderbooks();
			createSlowTraderOrders(experiment);
			dispatchArrivingOrders();
			processNewOrdersInAllOrderbooks();
			logRoundBasedData();
		}
	}

	public void executeRound(Experiment experiment) {
		prepareNewRound();
		if(currentRound % 1000 == 0) {
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
		createSlowTraderOrders(experiment);
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

	private void updateGlobalKnowledge() {
		/*
		 * This function contains calls that must be executed after all trading activity has ended for the round,
		 * but before round based logging is done.
		 * 
		 * Later, this method will contains calls for confirming the validity of trades, and other things.
		 * 
		 */
		this.runTime = System.currentTimeMillis() - this.creationTime;

		for(Stock stock:stocks) {
			stock.collectGlobalBestPricesAtEndOfRound();
		}
		
	}

	private void logRoundBasedData() {
		this.logStockPrices();
		this.logAgentData();
		this.dataLog.recordEntry();
		
	}

	private void logAgentData() {
		// TODO Auto-generated method stub
		for(HFT agent:this.agents){
			try{
				agent.roundDatalog.recordDataEntryAtEndOfRound();
			} catch(NullPointerException e){
				
			}
		}
	}

	private void logStockPrices() {
		for (Stock stock : this.stocks) {
			stock.roundBasedDatalog.recordEndRoundPriceInformation(this.currentRound);
		}
	}

	private void createSlowTraderOrders(Experiment experiment) {
		for (long i = 0; i < experiment.nSlowTraderOrdersPerRound; i++) {
			StylizedTrader.submitRandomOrder(experiment);
		}
//		System.out.println("k");
	}

	private void updateFundamental() {
		for (Stock stock : this.stocks) {
			stock.updateFundamentalPrice();
		}
	}

	

	public void prepareNewRound() {
		currentRound++;
		updateFundamental();
	}

	

	private void allAgentsRequestMarketInformation() {
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
		this.dataLog.setNInformationRequests(nInformationRequests);
//		this.eventLog.logOnelineEvent(String.format(
//				"%s agents requested market information", nInformationRequests));
		// try{
		//
		// for(HFT agent:freeAgents){
		// freeAgents.remove(agent);
		// agentRequestMarketInformation(agent);
		// i++;
		// }
		// this.eventLog.logGeneralEvent(String.format("%s agents requested market information",
		// i));
		// }catch(ConcurrentModificationException e){
		// e.printStackTrace();
		// }
		// agent.requestMarketInformation();

	}

	public void agentRequestMarketInformation(HFT agent) {
		agent.requestMarketInformation();
		addAgentToWaitingQueue(agent);
	}

	private void addAgentToWaitingQueue(HFT agent) {
		freeAgents.remove(agent);
		if (!waitingAgents.contains(agent)) {
			waitingAgents.add(agent);
		}
		thinkingAgents.remove(agent);
	}

	private void addAgentToThinkingQueue(HFT agent) {
		freeAgents.remove(agent);
		waitingAgents.remove();
		if (!thinkingAgents.contains(agent)) {
			thinkingAgents.add(agent);
		}
	}

	private void addAgentToFreeList(HFT agent) {
		if (!freeAgents.contains(agent)) {
			freeAgents.add(agent);
		}
		waitingAgents.remove(agent);
		thinkingAgents.remove(agent);
	}

	private void allAgentsReceiveMarketInformation() {
		/*
		 * Loop through agents in priority queue with priority equals current
		 * time. Update priority by their thinking time.
		 */
		long nReceivedMarketInformation = 0;
		long nReRequest = 0;
		while (true) {
			try {
				HFT agent = waitingAgents.element();
				if (agent.getWakeupTime() == this.getCurrentRound()) {
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
		this.dataLog.setnReceiveMarketInformation(nReceivedMarketInformation);
		this.dataLog.setnReRequestionMarketInformation(nReRequest);
//		this.eventLog.logOnelineEvent(String.format(
//				"%s agents received market information.", nReceivedMarketInformation));
	}

	private boolean agentReceiveMarketInformation(HFT agent) {
		try {
			agent.receiveMarketInformation();
			addAgentToThinkingQueue(agent);
			return true;
		} catch (NoOrdersException e) {
			return false;
//			this.eventLog
//					.logOnelineEvent(String
//							.format("Agent %s did not evaluate strategy, but requested new market information",
//									agent.getID()));
		}
	}

	private void agentsEvaluateMarketInformation() {
		/*
		 * Loop through agents in priority queue with priority equals current
		 * time. Agents sumbit orders Add agents on active list.
		 */
		long nFinishedEvaluating = 0;
		while (true) {
			try {
				HFT agent = thinkingAgents.element();
				if (agent.getWakeupTime() == this.getCurrentRound()) {
					nFinishedEvaluating++;
					agentEvaluateMarketInformation(agent);
				} else {
					break;
				}
			} catch (NoSuchElementException e) {
				break;
			}
		}
		this.dataLog.setnFinishedEvaluatingStrategy(nFinishedEvaluating);
//		this.eventLog.logOnelineEvent(String.format(
//				"%s agents finished evaluating their strategy.", i));
	}

	private void agentEvaluateMarketInformation(HFT agent) {
		if (agent.executeStrategyAndSubmit()) {
			addAgentToFreeList(agent);
		} else {
			warningLog.logError(String.format("Agent %s failed to evaluate strategy", agent.getID()), agent.getExperiment());
		}
	}

	public void dispatchArrivingOrders() {
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
				if (order.getArrivalTime() == this.currentRound) {
					nArrivingOrders++;
					ordersInTransit.remove();
					order.getOrderbook().receiveOrder(order);
				} else if (order.getArrivalTime() < this.currentRound){
					this.errorLog.logError(String.format("Current round is %s but order with arrival time %s exist in queue.", currentRound, order.getArrivalTime()), this.experiment);
					Exception e = new Exception();
					e.printStackTrace();
				} else if(order.getArrivalTime() > this.currentRound) {
					this.eventLog.logOnelineEvent(String.format("After dispatching %s orders this round there were %s order still transit.", nArrivingOrders, ordersInTransit.size()));
					break;
				}
			} catch (NoSuchElementException e) {
				this.eventLog.logOnelineEvent(String.format("After dispatching %s orders this round there were no more orders in transit.", nArrivingOrders));
				break;
			}
		}
		this.dataLog.setnArrivingOrders(nArrivingOrders);
	}
	
	private void dispatchOrderCancellations() {
		/*
		 * Transfer order cancellations from in transit to the target orderbook.
		 */
		long nArrivingCancellations = 0;
		while (true) {
			try {
				OrderCancellation cancellation = orderCancellationsInTransit.element();
				if (cancellation.getArrivalTime() == this.getCurrentRound()) {
					nArrivingCancellations++;
					orderCancellationsInTransit.remove();
					Orderbook targetOrderbook = cancellation.getOrder().getOrderbook();
					targetOrderbook.receieveOrderCancellation(cancellation);
				} else if (cancellation.getArrivalTime() < this.currentRound) {
					this.errorLog.logError(String.format("Current round is %s but cancellation with arrival time %s exist in queue.", currentRound, cancellation.getArrivalTime()), this.experiment);
				} else if (cancellation.getArrivalTime() > this.currentRound) {
					this.eventLog.logOnelineEvent(String.format("After dispatching %s cancellations this round there were stil %s cancellations in transit.", nArrivingCancellations, this.orderCancellationsInTransit.size()));
					break;
				}
			} catch (NoSuchElementException e) {
				this.eventLog.logOnelineEvent(String.format("After dispatching %s cancellations this round there were no more cancellations in transit.", nArrivingCancellations));
				break;
			}
		}
		this.dataLog.setnArrivingOrderCancellations(nArrivingCancellations);
	}

	public void dispatchArrivingReceipts() {
		long nArrivingReceipts = 0;
		while (true) {
			try {
				TransactionReceipt receipt = receiptsInTransit.element();
				if (receipt.getArrivalTime() == this.getCurrentRound()) {
					nArrivingReceipts++;
					receiptsInTransit.remove();
					HFT recipient = receipt.getOwnerOfFilledStandingOrder();
					recipient.receiveTransactionReceipt(receipt);
				} else if(receipt.getArrivalTime() < this.currentRound) {
					this.errorLog.logError(String.format("Current round is %s but receipt with arrival time %s exist in queue.", currentRound, receipt.getArrivalTime()), this.experiment);
				} else if(receipt.getArrivalTime() > this.currentRound) {
					this.eventLog.logOnelineEvent(String.format("After dispatching %s receipts this round there were stil %s receipts in transit.", nArrivingReceipts, this.receiptsInTransit.size()));
					break;
				}
			} catch (NoSuchElementException e) {
				this.eventLog.logOnelineEvent(String.format("After dispatching %s receipts this round there were no more receipts in transit.", nArrivingReceipts));
				break;
			}
		}
		this.dataLog.setnArrivingReceipts(nArrivingReceipts);
	}

	public void processNewOrdersInAllOrderbooks() {
		for (Orderbook orderbook : orderbooks) {
			orderbook.processAllNewOrders();
		}
	}

	public  void expireOrdersInAllOrderbooks() {
		long totalExpiredOrders = 0;
		for (Orderbook orderbook : orderbooks) {
			totalExpiredOrders += orderbook.expireOrders();
		}
		this.dataLog.setnExpiredOrders(totalExpiredOrders);
	}

	public void processOrderCancellationsInAllOrderbooks() {
		for (Orderbook orderbook : orderbooks) {
			orderbook.processAllCancellations();
		}
	}

	public void addMarket(Market market) {
		markets.add(market);
		// marketsByID.put(market.getID(), market);
	}

	public void addStock(Stock stock) {
		stocks.add(stock);
	}

	public void destroyOrder() {
		// this.warningLog.logWarning("WARNING: DESTROYING ORDERS NOT IMPLEMENTED YET!!");
	}

	public void addTransactionReceipt(TransactionReceipt receipt) {
		receiptsInTransit.add(receipt);
	}

	public void addNewOrder(Order order) {
//		Order orderOrderbookSide = orderAgentSide.getCopy();
		ordersInTransit.add(order);
	}

	public void addOrderCancellation(OrderCancellation update) {
		orderCancellationsInTransit.add(update);
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public void setTime(int time) {
		currentRound = time;
	}

	public void executeNRounds(Experiment experiment, long N) {
		for (long round = 0; round < N; round++) {
			if(round==5000) {
				System.out.println("Break time!");
			}
			this.executeRound(experiment);
		}
	}

	public Stock getStockByNumber(int index) {
		Stock stock = null;
		try {
			stock = stocks.get(index);
		} catch (IndexOutOfBoundsException e) {
			// e.printStackTrace();
			this.errorLog.logError(String.format(
					"Stock number %d has not been created.", index), null);
			System.exit(1);
		}
		return stock;

		// return stocksByID;
	}
	
	public Market getMarketByNumber(int index) {
		Market market = null;
		try {
			market = markets.get(index);
		} catch (IndexOutOfBoundsException e) {
			this.errorLog.logError(String.format(
					"Market number %d has not been created.", index), null);
		}
		return market;
	}

	public HashMap<String, HFT> getAgentByNumber() {
		return agentsByID;
	}

	public Orderbook getOrderbookByPair(StockMarketPair pair) {
		return orderbooksByPair.get(pair);
	}

	public Orderbook getOrderbookByObjects(Stock stock, Market market) {
		StockMarketPair pair = new StockMarketPair(stock, market);
		if (orderbooksByPair.containsKey(pair)) {
			return orderbooksByPair.get(pair);
		} else {
			this.errorLog
					.logError(String
							.format("Orderbook for stock %d and market %d has not been created yet!",
									stock, market), null);
			// System.exit(1);
			return null;
		}
	}

	public Orderbook getOrderbookByNumbers(int stockID, int marketID) {
		StockMarketPair pair = new StockMarketPair(stocks.get(stockID), markets.get(marketID));
		if (orderbooksByPair.containsKey(pair)) {
			return orderbooksByPair.get(pair);
		} else {
			this.errorLog
					.logError(String
							.format("Orderbook for stock %d and market %d has not been created yet!",
									stockID, marketID), null);
			// System.exit(1);
			return null;
		}

	}

	public void addNewAgent(HFT agent) {
		freeAgents.add(agent);
		agents.add(agent);
	}

	public MessageArrivalTimeComparator getArrivalTimeComparator() {
		return messageArrivalTimeComparator;
	}

	public PriorityQueue<Order> getOrdersInTransit() {
		return ordersInTransit;
	}

	public PriorityQueue<TransactionReceipt> getReceiptsInTransit() {
		return receiptsInTransit;
	}

	public ArrayList<Stock> getStocks() {
		return stocks;
	}

	public ArrayList<HFT> getHFTAgents() {
		return agents;
	}

	public ArrayList<Orderbook> getOrderbooks() {
		return orderbooks;
	}

	public ArrayList<Market> getMarkets() {
		return markets;
	}

	public HashMap<StockMarketPair, Orderbook> getOrderbooksByPair() {
		return orderbooksByPair;
	}

}
