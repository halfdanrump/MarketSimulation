package log;

import java.util.ArrayList;
import java.util.Collection;

import environment.Order;
import environment.World;
import agent.HFT;

public class AgentLogger extends Logger {
	// private static OrderIDComparatorLowFirst orderIDComparator = new
	// OrderIDComparatorLowFirst();

	HFT agent;

	public AgentLogger(String directory, String identifier, HFT agent, boolean recordHeader) {
		super(directory, String.format("%s_agent_%s", identifier, agent.getID()));
		if(recordHeader) {
			this.recordDataHeader();
		}
		this.agent = agent;
	}

	public void recordDataHeader() {
		String header = "Round, " + "Agent wealth, " + "number of submitted orders, " + "number of cancellations, " + "number of fulfilled orders, " + "number of standing buy orders, " + "number of standing sell orders\n" + "Round\tWealth\tnSubOr\tnCan\tnFull\tNSBO\tNSSO";
		super.writeToFile(header);
	}

	public void recordDataEntry() {
		String entry = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s", World.getCurrentRound(), this.agent.getWealth(), agent.getnSubmittedOrders(), agent.getnOrderCancellations(), agent.getnFullfilledOrders(), agent.getnStandingBuyOrders(), agent.getnStandingSellOrders());
		super.writeToFile(entry);
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
