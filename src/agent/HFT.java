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

public abstract class HFT implements HighFrequencyTradingBehavior, Logging, MarketRules{
	public static int nHFTs = 0;
	protected int id;
	protected int wealth;
	protected int wakeupTime;
	protected int thinkingTime;
	protected HashMap<Orderbook, Order> standingBuyOrders;
	protected HashMap<Orderbook, Order> standingSellOrders;
	protected ArrayList<Orderbook> orderbooks;
	protected HashMap<Market, Integer> latencyToMarkets;
	protected ArrayList<TransactionReceipt> receipts;
	protected HashMap<Stock, Integer> ownedStocks;
	protected HashMap<Stock, Integer> stocksInStandingOrders;
	protected ArrayList<Market> markets;
	protected ArrayList<Stock> stocks;
	
	protected int nSubmittedBuyOrders;
	protected int nSubmittedSellOrders;
	protected int nSubmittedCancellations;
	protected int nFulfilledBuyOrders;
	protected int nFulfilledSellOrders;

	protected ArrayList<Order> orderHistory;
	
	protected abstract int getWaitingTime();
	public abstract void storeMarketInformation() throws NoOrdersException;
	public abstract boolean executeStrategyAndSubmit();

	
	public AgentLogger eventlog;
	public AgentLogger datalog;
	
	
	public HFT(int wealth, int[] stockIDs, int[] startingPortfolio, int[] marketIDs, int[] latencies) {
		this.wealth = wealth;
		this.wakeupTime = World.getCurrentRound();
		this.initialize(stockIDs, marketIDs, latencies);
		this.setPortfolio(startingPortfolio);
		World.addNewAgent(this);
	}
	
	public HFT(int[] stockIDs, int[] marketIDs, int[] latencies) {
		if(randomStartWealth) {
			this.wealth = getRandomInitialTraderWealth();
		} else {
			this.wealth = constantStartWealth;
		}
		this.wakeupTime = World.getCurrentRound();
		this.initialize(stockIDs, marketIDs, latencies);
		World.addNewAgent(this);
	}
	
	private void initialize(int[] stockIDs, int[] marketIDs, int[] marketLatencies){
		this.id = nHFTs;
		nHFTs++;
		this.nSubmittedBuyOrders = 0;
		this.nSubmittedSellOrders = 0;
		this.nFulfilledBuyOrders = 0;
		this.nFulfilledSellOrders = 0;
		this.receipts = new ArrayList<TransactionReceipt>();
		this.standingBuyOrders = new HashMap<Orderbook, Order>();		//At present, each agent can only maintain a single order on each side of the book
		this.standingSellOrders = new HashMap<Orderbook, Order>();
		this.orderbooks = new ArrayList<Orderbook>();
		this.ownedStocks = new HashMap<Stock, Integer>();
		this.latencyToMarkets = new HashMap<Market, Integer>();
		this.markets = new ArrayList<Market>();
		this.stocks = new ArrayList<Stock>();
		
		this.buildLatencyHashmap(marketIDs, marketLatencies);
		this.markets.addAll(this.latencyToMarkets.keySet());
		this.buildOrderbooksHashMap(marketIDs, stockIDs);
		this.buildStocksList(stockIDs);
		this.initializePortfolio();
		if(keepOrderHistory) {
			this.orderHistory = new ArrayList<Order>();
		}
	}
	
	private void buildLatencyHashmap(int[] marketIDs, int[] marketLatencies){
		/*
		 * Initalize latency HashMap
		 */
		if(!(marketIDs.length == marketLatencies.length)){
			World.errorLog.logError("Length of markets array and length of latenceis array must be the same.");
		}
		
		for(int i = 0; i<marketIDs.length; i++){
			this.latencyToMarkets.put(World.getMarketByNumber(i), marketLatencies[i]);
		}	
	}
	
	private void buildOrderbooksHashMap(int[] marketIDs, int[] stockIDs){
		for(int market:marketIDs){
			for(int stock:stockIDs){
				this.orderbooks.add(World.getOrderbookByNumbers(stock, market));
			}
		}
	}
	
	private void buildStocksList(int[] stockIDs){
		for(int i:stockIDs){
			Stock stock = World.getStockByNumber(i);
			if(!this.stocks.contains(stock)){
				this.stocks.add(stock);
			} else{
				World.errorLog.logError("Problem when building stocks ArrayList in HFT agent (buildStockList): Tried to add the same stock more than one.");
			}
		}
	}
	
	private void initializePortfolio(){
		/*
		 * Initializes portfolio with constant or random amount as specified in the interface.
		 */
		for(Stock stock:this.stocks){
			if(randomStartStockAmount){
				World.errorLog.logError("Random start stock amount not implemented yet!");
			} else{
				this.ownedStocks.put(stock, startStockAmount);				
			}
		}
	}
	
	public void setPortfolio(int[] portfolio){
		/*
		 * Used to give the agent a ustom portfolio
		 */
		if(portfolio.length != this.stocks.size()){
			World.errorLog.logError("Error when creating custom specified portfolio. Size of new portfolio does not match size of agetn stock array.");
		}
	}	
	
//	private void getRandomPortfolio(int[] stockIDs){
//		int[] portfolio = new int[stockIDs.length];
//		for(int i = 0; i < stockIDs.length; i++){
//	portfolio[i] = Utils.getNonNegativeGaussianInteger(, std)
//		}
//	}
	
	
	/*
	 * World calls this function when it's time for the agent to trade
	 */

	public void requestMarketInformation(){
		/*
		 * Updates the agents wakeup time, which depends on the agents strategy (that is, which markets he wants information from).
		 */
//		World.registerAgentAsWaiting(this);
		this.wakeupTime = World.getCurrentRound() + this.getWaitingTime();
	}
	
	public void receiveMarketInformation() throws NoOrdersException{
		/*
		 * Method that deals with flow
		 * -Update agent wakeup time by adding thinking time.
		 * -Read the received information and update internal data structures
		 */
		World.agentRequestMarketInformation(this);
		this.wakeupTime += this.thinkingTime;
		try{
			this.storeMarketInformation();
		} catch(NoOrdersException e){
			throw e;
		}
	}
	
	private void updatePortfolio(TransactionReceipt receipt) {
		Stock stock = receipt.getStock();
		int volumeChange = receipt.getSignedVolume();
		int newVolume = this.ownedStocks.get(stock) + volumeChange;

		if (newVolume < 0) {
			if(!(receipt.getOriginalOrder().getBuySell() == Order.BuySell.SELL)) {
				World.errorLog.logError("Error, because order volume was less than zero, but not a SELL order");
			}
			if(MarketRules.allowsShortSelling) {
				this.receipts.add(receipt);
				this.ownedStocks.put(stock, newVolume);
				World.warningLog.logOnelineEvent(String.format("Agent %s shorted a volume of %s of stock number %s", receipt.getOwner().getID(), receipt.getAbsoluteVolume(), receipt.getOriginalOrder().getStock().getID()));
			} else {
				World.ruleViolationsLog.logShortSelling(receipt);
				World.errorLog.logError("Short selling was not allowed by market rules, but happened. Handling of this situation is not implemented yet");
			}
		} else {
			this.receipts.add(receipt);
			this.ownedStocks.put(stock, newVolume);
		}
	}
	
	public void receiveTransactionReceipt(TransactionReceipt receipt){
		/*
		 * When the agent receives a receipt, he knows that his order has been filled, so he 
		 */
//		this.datalog.printOrderHistoryIdString();
		this.eventlog.logAgentAction(String.format("Agent %s received a receipt for a %s order, id: %s", this.id, receipt.getBuySell(), receipt.getOriginalOrder().getID()));
		this.updatePortfolio(receipt);
		this.updateWealth(receipt.getSignedTotal());
		
		if(receipt.getBuySell() == BuySell.BUY){
			this.nFulfilledBuyOrders += 1;
			if(this.standingBuyOrders.containsKey(receipt.getOrderbook())){
				this.updateStandingOrder(receipt);
			} else{
				this.dealWithOrderForRemovedOrder(receipt);
			}
			
		} else{
			this.nFulfilledSellOrders += 1;
			if(this.standingSellOrders.containsKey(receipt.getOrderbook())){
				this.updateStandingOrder(receipt);
			} else{
				this.dealWithOrderForRemovedOrder(receipt);
			}
		}
	}
	
	
	
	private void dealWithOrderForRemovedOrder(TransactionReceipt receipt) {
		if(this.orderHistory.contains(receipt.getOriginalOrder())) {
			/*
			 * The agent did submit the order at some point, but now it is no longer in his standing order list.
			 * A likely explanation (the only one that I've come up with so far) is that the order was filled after the agent issued a cancellation. 
			 */
			if(agentPaysWhenOrderIsFilledAfterSendingCancellation) {
				this.nFulfilledBuyOrders += 1; 
				this.eventlog.logAgentAction(String.format("Agent %s had to fullfill an already cancelled order (id: %s)", receipt.getOwner().getID(), receipt.getOriginalOrder().getID()));
			} else {
				World.errorLog.logError(String.format("Agent %s received a transaction receipt for an order that he cancelled (id: %s), but market side handling of this situation has not been implemented yet", this.getID(), receipt.getOriginalOrder().getID()));
			}
		} else {
			World.errorLog.logError(String.format("Agent %s received a transaction receipt for an order that he didn't submit (id: %s)", this.getID(), receipt.getOriginalOrder().getID()));
		}
//		World.errorLog.logError(String.format("Agent %s received a transaction receipt for a buy order, but the corresponding order was not in its standingBuyOrders list", this.getID()));
//		this.eventlog.printStandingOrders();
	}
	
	
	
	private void updateStandingOrder(TransactionReceipt receipt){
		/*
		 * When the agent receives a receipt for a filled order, he has to update his local list of submitted orders.
		 * If the receipt volume is less than the volume of the filled local order, he just subtracts the difference from the local order.
		 * If they are equal, he removes the local order.
		 * If it is more, he throws an error.   
		 */
		Order standingOrder;
		if(receipt.getBuySell() == BuySell.BUY){
			standingOrder = this.standingBuyOrders.get(receipt.getOrderbook());
			if(receipt.getAbsoluteVolume() == standingOrder.getAgentSideVolume()){
				this.standingBuyOrders.remove(receipt.getOrderbook());
			}
		} else{
			standingOrder = this.standingSellOrders.get(receipt.getOrderbook());
			if(receipt.getAbsoluteVolume() == standingOrder.getAgentSideVolume()){
				this.standingSellOrders.remove(receipt.getOrderbook());
			}
		}
		
		if(receipt.getAbsoluteVolume() < standingOrder.getAgentSideVolume()){
			standingOrder.updateAgentSideVolumeByDifference(receipt.getSignedVolume());
		} else if(receipt.getAbsoluteVolume() > standingOrder.getAgentSideVolume()){
			World.errorLog.logError("Agent received a receipt for a buy order with a larger volume than the one he placed...");
		}
			
	}
	
	public int getWealth() {
		return wealth;
	}

	public void updateWealth(int amount) {
		this.wealth += amount;
	}

	public int getID() {
		return id;
	}

	public int getLatency(Market market) {
		return this.latencyToMarkets.get(market);
	}

	public ArrayList<Orderbook> getOrderbooks() {
		return this.orderbooks;
	}

	public int getWakeupTime(){
		return this.wakeupTime;
	}
	
	public int getArrivalTimeToMarket(Market market){
		return this.getLatency(market) + World.getCurrentRound();
	}
	
	public static int getRandomInitialTraderWealth(){
		Random r = new Random();
		int w = 0;
		while (w <= 0){
			w = (int) Math.rint(r.nextGaussian()*wealthStd + wealthMean);
		}
		return w;
	}

	public void hibernate(){
		this.wakeupTime = World.getCurrentRound() + emptyOrderbookWaitTime;
	}
	
	protected void submitOrder(Order order){
		if(order.getBuySell() == Order.BuySell.BUY){
			this.standingBuyOrders.put(order.getOrderbook(), order);
			this.nSubmittedBuyOrders += 1;
		} else{
			this.standingSellOrders.put(order.getOrderbook(), order);
			this.nSubmittedSellOrders += 1; 
		}
		if(keepOrderHistory) {
			this.orderHistory.add(order);
		}
	}
	
	public static HashMap<Market, Integer> getRandomLatencyHashMap(Market[] markets){
		HashMap<Market, Integer> latencyMap = new HashMap<Market, Integer>();
		
		for(Market market:markets){
			int latency = Utils.getRandomUniformInteger(minimumLatency, maximumLatency); 
			latencyMap.put(market, latency);
		}
		return latencyMap;
	}
	
	
	
	protected void cancelOrder(OrderCancellation cancellation){
		this.nSubmittedCancellations += 1;
	}
	
	public int getnSubmittedBuyOrders() {
		return nSubmittedBuyOrders;
	}
	public int getnSubmittedSellOrders() {
		return nSubmittedSellOrders;
	}
	public int getnFulfilledBuyOrders() {
		return nFulfilledBuyOrders;
	}
	public int getnFulfilledSellOrders() {
		return nFulfilledSellOrders;
	}
	
	public int getnFullfilledOrders(){
		return this.nFulfilledBuyOrders + this.nFulfilledSellOrders;
	}
	
	public int getnSubmittedOrders(){
		return this.nSubmittedBuyOrders + this.nSubmittedSellOrders;
	}
	
	public int getnStandingBuyOrders(){
		return this.standingBuyOrders.size();
	}
	
	public int getnStandingSellOrders(){
		return this.standingSellOrders.size();
	}
	
	public int getnOrderCancellations(){
		return this.nSubmittedCancellations;
	}
	public HashMap<Orderbook, Order> getStandingBuyOrders() {
		return standingBuyOrders;
	}
	public HashMap<Orderbook, Order> getStandingSellOrders() {
		return standingSellOrders;
	}
	
	public ArrayList<Order> getOrderHistory(){
		return this.orderHistory;
	}
	
	
}
