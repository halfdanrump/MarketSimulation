package log;

public interface Logging {
	
	boolean appendDateToFiles = false;
//	boolean logOrderbookDetails = false;
	
	boolean fileLogging = true;
	boolean consoleLogging = false;
	
	/*
	 * WORLD. What kind of world events to log. A world event could be the cumulative actions of all agents in a round 
	 */
	boolean logWorldEventsToFile = true;
	boolean logWorldEventsToConsole = true;
	boolean logWorldWarningsToFile = true;
	boolean logWorldWarningsToConsole = true;
	
	/*
	 * AGENT. What individual agent actions to log
	 */
	
	boolean logAgentActionsToFile = true;
	boolean logAgentActionsToConsole = true;
	boolean logAgentWarningsToFile = true;
	boolean logAgentWarningsToConsole = true;
	
	/*
	 * STOCKS
	 */
//	boolean logStockDataToFile = true;
//	boolean logStockDataToConsole = true;
	
	/*
	 * ORDERBOOK. What kind of orderbook events to log
	 */
	final boolean logOrderbookEventsToConsole = false;
	//////////////////////////////////////////////////
	final boolean logOrderbookMatches = true; // Logs whenever there's a log (including stylized traders)
	final boolean logOrderbookTransactions = true; // Logs only when there's a transaction, i.e. a match with an order placed by a HFT
	final boolean logOrderbookOrderExpire = true;
	final boolean logOrderbookAddOrder = true;
	final boolean logOrderbookRemoveOrder = true;
	final boolean logOrderbookUpdateOrderVolume = true;
	final boolean logOrderbookProcessOrder = true;
	
	String logFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/";
	
//	String worldLogPath = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/";
//	
//	String stockDataFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/stocks/";
//	
//	String orderbookDataFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/orderbooks/";
//	String orderbookEventFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/orderbooks/";
//	
//	String agentEventFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/agents/";
//	String agentDataFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/agents/";
//	
	
//	String generalLogFolder = "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/";

}
