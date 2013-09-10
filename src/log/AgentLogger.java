package log;

import java.util.ArrayList;
import java.util.Collection;

import setup.Logging;
import utilities.Utils;
import environment.Order;
import environment.Stock;
import environment.TransactionReceipt;
import experiments.Experiment;
import agent.HFT;

public class AgentLogger extends Logger {
	// private static OrderIDComparatorLowFirst orderIDComparator = new
	// OrderIDComparatorLowFirst();

	HFT agent;
	private Experiment experiment;
	
	public enum headerType{
		TRADE_DATA,
		ROUND_DATA,
		NO_HEADER
	}

	public AgentLogger(String directory, String identifier, HFT agent, Logger.Type type, AgentLogger.headerType headerType, boolean logToFile, boolean logToConsole, Experiment experiment) {
		super(directory, String.format("%s_agent_%s", identifier, agent.getID()), type, logToFile, logToConsole, experiment);
		this.agent = agent;
		this.experiment = experiment;
		if(headerType == AgentLogger.headerType.ROUND_DATA) {
			String header = "round, cash, portfolio, nSubOrders, nOrderCancellations, nReceivedBuyOrderReceipts, nReceivedSellOrderReceiptsn, nFulfilledOrders, " +
							"nStandingBuyOrders, nStandingSellOrders";
			for(Stock stock:this.agent.getActiveStocks()) {
				header += String.format(", Stock%s", stock.getID());
			}
			super.writeToFile(header);
		} else if(headerType == AgentLogger.headerType.TRADE_DATA) {
			super.writeToFile("round, stockID, marketID, tradedWith, buysell, price, volume, total, cashAfterTrade, borrowedCash, borrowedStock, roundSubmitted");
		}
	}

	public void logTradeData(TransactionReceipt receipt, long volume, long total, long borrowedCash, long borrowedStocks) {
		/*
		 * Records the following information just after the agent has received the transaction received
		 * and done the necessary actions to fulfill the contract. Note that the volume must be seperately 
		 * specified, since it is not necessarily the same as the volume in the receipt (if the agent for some
		 * reason cannot supply the full volume)
		 * 
		 * Data:
		 * 0-Current round
		 * 1-stock
		 * 2-market
		 * 3-traded with who (NA if trade with stylized trader)
		 * 4-buysell
		 * 5-price
		 * 6-volume
		 * 7-total
		 * 8-cash after trade
		 * 9-borrowed cash (0 if not borrowed any cash)
		 * 10-borrowedStocks
		 */
		if(this.createLogString){
			ArrayList<String> entry = new ArrayList<String>();
			Utils.initializeStringArrayWithEmptyStrings(entry, 11, "");
			entry.set(0, String.valueOf(this.experiment.getWorld().getCurrentRound()));
			entry.set(1, String.valueOf(receipt.getFilledOrder().getStock().getID()));
			entry.set(2, String.valueOf(receipt.getFilledOrder().getMarket().getID()));
			if(receipt.getFillingOrder().getOwner() == null) {
				entry.set(3, "NA");
			} else {
				entry.set(3, String.valueOf(receipt.getFillingOrder().getOwner().getID()));
			}
			entry.set(4,String.valueOf(receipt.getBuySell()));
			entry.set(5,String.valueOf(receipt.getPrice()));
			entry.set(6,String.valueOf(volume));
			entry.set(7,String.valueOf(total));
			entry.set(8,String.valueOf(this.agent.getCash()));
			entry.set(9,String.valueOf(borrowedCash));
			entry.set(10,  String.valueOf(borrowedStocks));
			
			String line = Utils.convertArrayListToString(entry); 			
			if(this.logToFile) {
				super.writeToFile(line);
			}
			if(this.logToConsole) {
				this.writeToConsole(line);
			}
		}
		
	}

	@SuppressWarnings("unused")
	public void recordDataEntryAtEndOfRound() {
		
		if(Logging.logAgentRoundDataToFile | Logging.logAgentRoundDataToConsole){
			String entry = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
					this.experiment.getWorld().getCurrentRound(), 
					this.agent.getCash(),
					this.agent.evaluatePortfolioValue(this.experiment.getWorld().getCurrentRound()),
					agent.getnSubmittedOrders(), 
					agent.getnOrderCancellations(),
					agent.getnReceivedBuyOrderReceipts(),
					agent.getnReceivedSellOrderReceipts(),
					agent.getnFullfilledOrders(), 
					agent.getnStandingBuyOrders(), 
					agent.getnStandingSellOrders())
					+ this.getCurrentPortfolioAsTabulatedString();
			if(Logging.logAgentRoundDataToFile) {
				super.writeToFile(entry);
			}
			if(Logging.logAgentRoundDataToConsole) {
				this.writeToConsole(entry);
			}
			
		}
		
	}
	
//	private String getPortfolioHeaderAsTabulatedString() {
//		String s = "";
//		for(Stock stock:this.agent.getActiveStocks()) {
//			s += String.format(", Stock%s", stock.getID());
//		}
//		return s;
//	}
	
	private String getCurrentPortfolioAsTabulatedString() {
		String s = "";
		for(Stock stock:this.agent.getActiveStocks()) {
			long amount = this.agent.getOwnedStocks().get(stock);
			s += String.format(", %s", amount);
		}
		return s;
	}
	
	public void logAgentAction(String line) {
		line = super.getNewEntry() + line;
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

//	public void printOrderHistoryIdString() {
//		ArrayList<Order> orderHistory = this.agent.getOrderHistory();
//		String IDs = "";
//		for (Order order : orderHistory) {
//			IDs += String.format("%s, ", order.getID());
//		}
//		System.out.println(IDs);
//	}

}
