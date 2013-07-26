package setup;

public interface SimulationSetup {

	/* Simulation parameters
	 * */
//	public final int nRounds = 10000;
//	
//	long nSlowTraderOrdersPerRounds = 10;

	// initial market setting
		
	
	/* Agent parameters
	 * */
//	public static final long nAgents = 100;
	
	
	/* Market parameters
	 * */
	public static final long nMarkets = 2;
	public static final String[] marketNames = {"Market1", "Market2"};
	public static final double[] marketAgentShare = {0.4, 0.6};
	
	/* Stock parameters
	 * */
	public static final long nStocks = 1; 
	public static final String[] stockIDs = {"Stock1"};
//	public final double[] stockAgentShare = {0.2, 0.8};
	public static final int[] initialFundamental = {100};

	/*
	 * Matrix specifying which stocks are traded on which markets.
	 * One row for each stock, one column for each market.
	 */
	public static final boolean[][] orderbookSpecification = 
		{{true, true},
		 {true, false},
		 {false, true}};
	
	/*
	 * Logging parameters
	 */
	

	
	
	/*
	 * Parameters for market rules
	 */

	
	
	/* Indices
	 * */
//	public Hashtable<String, Boolean> tradeHash = this.initalizeTradeHash();
//
//	/* Methods
//	 * */
//	protected final Hashtable initalizeTradeHash(){
//		Hashtable<String, Boolean> t = new Hashtable<String, Boolean>();
//		t.put("Stock1"+"Market1", true);
//		t.put("Stock2"+"Market1", true);
//		t.put("Stock1"+"Market2", true);
//		return t;
//	}


	
}


//STOCK ACTIONS
//-update fundamental price
//-add to market (only used when the world is being made)
//

//AGENT ACTIONS
//-Place order
//	-choose stock and market
//	-calculate price
//	-calculate volume
// Each agent has a dict with (String marketID, String stockID):Orderbook-ref pair for each stock that the agent trades. 

//ORDER
//An order has a pointer to the Orderbook that's it's target

//ORDERBOOK ACTIONS
//-Receive order; Add Order to a list of incoming orders
//-process order; Pop an order from the list and match it or put it in the book
//-remove expired orders; Go through the order book and remove all expired orders

//
//WORLD ACTIONs
//-increment time
//-receiveTransmittedOrder: receive Order object and add to list of floating orders
//-dispatchArrivingOrders: Go through list of floating orders and run market.receiveOrder for each arriving order
//-Create stock
//-Create agent
//-create market
//- Find best bid and ask for the current round and write it to the stock.
