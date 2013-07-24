package setup;

public interface Logging {

	boolean createTimeSpecificLogFolders = false;
	// boolean logOrderbookDetails = false;

	boolean fileLogging = true;
	boolean consoleLogging = false;

	/*
	 * WORLD. What kind of world events to log. A world event could be the
	 * cumulative actions of all agents in a round
	 */
	boolean logWorldEventsToFile = false;
	boolean logWorldEventsToConsole = false;
	boolean logWorldWarningsToFile = true;
	boolean logWorldWarningsToConsole = true;

	/*
	 * AGENT. What individual agent actions to log
	 */

	boolean logAgentActionsToFile = false;
	boolean logAgentActionsToConsole = false;

	boolean logAgentTradeDataToFile = false;
	boolean logAgentTradeDataToConsole = false;

	boolean logAgentRoundDataToFile = false;
	boolean logAgentRoundDataToConsole = false;

	/*
	 * STOCKS
	 */
	boolean logStockRoundDataToFile = false;
	boolean logStockRoundDataToConsole = false;

	boolean logStockTransactionDataToFile = false;
	boolean logStockTransactionDataToConsole = false;

	/*
	 * ORDERBOOK. What kind of orderbook events to log
	 */
	final boolean logOrderbookEventsToFile = false;
	final boolean logOrderbookEventsToConsole = false;
	final boolean logOrderbookOnelineEvents = false;
	boolean logOrderbookOnelineEventsToConsole = false;

	boolean logOrderbookOrderFlowToFile = false;
	boolean logOrderbookOrderFlowToConsole = false;

	// ////////////////////////////////////////////////
	// final boolean logOrderbookMatches = true; // Logs whenever there's a log
	// (including stylized traders)
	// final boolean logOrderbookTransactions = true; // Logs only when there's
	// a transaction, i.e. a match with an order placed by a HFT
	// final boolean logOrderbookOrderExpire = true;
	// final boolean logOrderbookAddOrder = true;
	// final boolean logOrderbookRemoveOrder = true;
	// final boolean logOrderbookUpdateOrderVolume = true;
	// final boolean logOrderbookProcessOrder = true;

	// String worldLogPath =
	// "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/";
	//
	// String stockDataFolder =
	// "/Users/halfdan/Dropbox/Waseda/Research/Simulation/stocks/";
	//
	// String orderbookDataFolder =
	// "/Users/halfdan/Dropbox/Waseda/Research/Simulation/orderbooks/";
	// String orderbookEventFolder =
	// "/Users/halfdan/Dropbox/Waseda/Research/Simulation/orderbooks/";
	//
	// String agentEventFolder =
	// "/Users/halfdan/Dropbox/Waseda/Research/Simulation/agents/";
	// String agentDataFolder =
	// "/Users/halfdan/Dropbox/Waseda/Research/Simulation/agents/";
	//

	// String generalLogFolder =
	// "/Users/halfdan/Dropbox/Waseda/Research/Simulation/logs/";

}
