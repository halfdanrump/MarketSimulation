package log;

import agent.HFT;
import environment.Order;
import environment.OrderCancellation;
import environment.Orderbook;
import environment.TransactionReceipt;
import environment.World;

public class WorldLogger extends Logger {
	private long nArrivingOrders;
	private long nArrivingOrderCancellations;
	private long nArrivingReceipts;
	private long nInformationRequests;
	private long nReceiveMarketInformation;
	private long nFinishedEvaluatingStrategy;
	private long nExpiredOrders;
	

	private long nReRequestionMarketInformation;

	public WorldLogger(String directory, String identifier, boolean recordHeader) {
		super(directory, identifier);
		if(recordHeader) {
			recordHeader();
		}
		// TODO Auto-generated constructor stub
	}
	
	public void recordHeader(){
		String header = "1) Round, \n" +
						"2) Total number of standing orders across all the orderbooks, \n" +
						"3) Total number of submitted orders, \n" +
						"4) Total number of submitted cancellations, \n" +
						"5) Total number of fulfilled orders, \n" +
						"6) Total wealth of all HFTs, \n" +
						"7) Number of orders that arrived this round, \n" +
						"8) Number of order cancellations that arrived this round, \n" +
						"9) Number of receipts that arrived this round\n" +
						"10) Number of information requests from agents, \n" +
						"11) Number of agents that received market information, \n" +
						"12) Number of agents that finished evaluating their strategy\n" +
						"13) Number of agents that re-requested informaton after receiving\n " +
						"14) Number of expired orders\n" +
						"1\t2\t3\t4\t5\t6\t7\t8\t9\t10\t11\t12\t13\t14";
		super.writeToFile(header);
	}
	
	public void recordEntry(){
		String entry = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
									World.getCurrentRound(),
									this.getTotalNStandingOrders(),
									Order.getOrderCount(),
									OrderCancellation.getCancellationCount(),
									TransactionReceipt.getReceiptCount(),
									this.getTotalWealth(),
									this.nArrivingOrders,
									this.nArrivingOrderCancellations,
									this.nArrivingReceipts,
									this.nInformationRequests,
									this.nReceiveMarketInformation,
									this.nFinishedEvaluatingStrategy,
									this.nReRequestionMarketInformation,
									this.nExpiredOrders
									);
		super.writeToFile(entry);
	}
	
	
	
	public void setnInformationRequests(long nInformationRequests) {
		this.nInformationRequests = nInformationRequests;
	}

	private long getTotalWealth(){
		long totalWealth = 0;
		for(HFT agent:World.getHFTAgents()){
			totalWealth += agent.getTotalWealth();
		}
		return totalWealth;
	}
	
	private long getTotalNStandingOrders(){
		long nOrders = 0;
		
		for(Orderbook orderbook:World.getOrderbooks()){
			nOrders += orderbook.getUnfilledBuyOrders().size();
			nOrders += orderbook.getUnfilledSellOrders().size();
		}
		return nOrders;
	}
	
	public void logOnelineEvent(String line) {
		line = super.getNewEntry() + line;
		if(logWorldEventsToFile) {
			super.writeToFile(line);
		}
		if(logWorldEventsToConsole) {
			super.writeToConsole(line);
		}
	}
	
	public void logOnelineWarning(String line) {
		Exception e = new Exception();
		line = super.getNewEntry() + line;
		if(logWorldWarningsToFile) {
			super.writeToFile(line + e.getStackTrace().toString());
		}
		if(logWorldWarningsToConsole) {
			super.writeToConsole(line + e.getStackTrace());
		}
	}

	public void setnArrivingOrders(long nArrivingOrders) {
		this.nArrivingOrders = nArrivingOrders;
	}

	public void setnArrivingOrderCancellations(long nArrivingOrderCancellations) {
		this.nArrivingOrderCancellations = nArrivingOrderCancellations;
	}
	
	public void setNInformationRequests(long nInformationRequests) {
		this.nInformationRequests = nInformationRequests;
	}

	public void setnReceiveMarketInformation(long nReceiveMarketInformation) {
		this.nReceiveMarketInformation = nReceiveMarketInformation;
	}

	public void setnReRequestionMarketInformation(long nReRequestionMarketInformation) {
		this.nReRequestionMarketInformation = nReRequestionMarketInformation;
	}
	
	public void setnFinishedEvaluatingStrategy(long nFinishedEvaluatingStrategy) {
		this.nFinishedEvaluatingStrategy = nFinishedEvaluatingStrategy;
	}	
	
	public void setnArrivingReceipts(long nArrivingReceipts) {
		this.nArrivingReceipts = nArrivingReceipts;
	}
	
	public void setnExpiredOrders(long nExpiredOrders) {
		this.nExpiredOrders = nExpiredOrders;
	}

}
