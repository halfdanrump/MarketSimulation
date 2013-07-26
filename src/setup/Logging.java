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

	boolean logStockTransactionDataToFile = true;
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


}
