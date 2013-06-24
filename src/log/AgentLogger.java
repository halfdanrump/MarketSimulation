package log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import environment.Order;
import environment.Stock;
import environment.World;
import agent.HFT;

public class AgentLogger extends Logger {
	// private static OrderIDComparatorLowFirst orderIDComparator = new
	// OrderIDComparatorLowFirst();

	HFT agent;

	public AgentLogger(String directory, String identifier, HFT agent, boolean recordHeader) {
		super(directory, String.format("%s_agent_%s", identifier, agent.getID()));
		this.agent = agent;
		if(recordHeader) {
			this.recordDataHeader();
		}
	}

	public void recordDataHeader() {
		String header = "Round, " + "Agent wealth, " + "number of submitted orders, " + "number of cancellations, " + "number of fulfilled orders, " + "number of standing buy orders, " + "number of standing sell orders\n" + "Round\tWealth\tnSubOr\tnCan\tnFull\tNSBO\tNSSO" 
						+ this.getPortfolioHeaderAsTabulatedString();
		super.writeToFile(header);
	}

	public void recordDataEntry() {
		String entry = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
									World.getCurrentRound(), 
									this.agent.getCash(),
									this.agent.getTotalWealth(),
									agent.getnSubmittedOrders(), 
									agent.getnOrderCancellations(), 
									agent.getnFullfilledOrders(), 
									agent.getnStandingBuyOrders(), 
									agent.getnStandingSellOrders())
									+ this.getCurrentPortfolioAsTabulatedString();
		super.writeToFile(entry);
	}
	
	private String getPortfolioHeaderAsTabulatedString() {
		String s = "";
		for(Stock stock:this.agent.getActiveStocks()) {
			s += String.format("\tStock%s", stock.getID());
		}
//		HashMap<Stock, Integer> ownedStocks = this.agent.getOwnedStocks();
//		Iterator iterator = ownedStocks.entrySet().iterator();
//		while(iterator.hasNext()) {
//			Map.Entry<Stock, Integer> pair = (Map.Entry<Stock, Integer>) iterator.next();
//			Stock stock = pair.getKey();
//			Integer amount = pair.getValue();
//			s += String.format("\t%s", amount);
//		}
		return s;
//		for() {
//			
//		}
	}
	
	private String getCurrentPortfolioAsTabulatedString() {
		String s = "";
		for(Stock stock:this.agent.getActiveStocks()) {
			long amount = this.agent.getOwnedStocks().get(stock);
			s += String.format("\t%s", amount);
		}
//		HashMap<Stock, Integer> ownedStocks = this.agent.getOwnedStocks();
//		Iterator iterator = ownedStocks.entrySet().iterator();
//		while(iterator.hasNext()) {
//			Map.Entry<Stock, Integer> pair = (Map.Entry<Stock, Integer>) iterator.next();
//			Stock stock = pair.getKey();
//			Integer amount = pair.getValue();
//			s += String.format("\t%s", amount);
//		}
		return s;
//		for() {
//			
//		}
	}

	public void logAgentAction(String line) {
		if (logAgentActionsToConsole) {
			super.writeToConsole(line);
		}
		if (logAgentActionsToFile) {
			super.writeToFile(line);
		}

	}

	public void printStandingOrders() {
		Collection<Order> standingBuyOrders = this.agent.getStandingBuyOrders().values();
		System.out.print(String.format("Agent %s standing BUY orders: ", this.agent.getID()));
		for (Order order : standingBuyOrders) {
			System.out.print(String.format("%s, ", order.getID()));
		}
		System.out.println("");
		Collection<Order> standingSellOrders = this.agent.getStandingSellOrders().values();
		System.out.print(String.format("Agent %s standing SELL orders: ", this.agent.getID()));
		for (Order order : standingSellOrders) {
			System.out.print(String.format("%s, ", order.getID()));
		}
		System.out.println("");
	}

	public void printOrderHistoryIdString() {
		ArrayList<Order> orderHistory = this.agent.getOrderHistory();
		String IDs = "";
		for (Order order : orderHistory) {
			IDs += String.format("%s, ", order.getID());
		}
		System.out.println(IDs);
	}

}
