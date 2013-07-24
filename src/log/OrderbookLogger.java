package log;

import setup.Logging;
import environment.Order;
import environment.Orderbook;
import environment.TransactionReceipt;
import environment.World;

public class OrderbookLogger extends Logger implements Logging{
	public enum Type{
		ORDER_FLOW_LOG,
		ORDERBOOK_ROUND_BASED_DATA,
		EVENT_LOG
	}
	
	private Orderbook orderbook;


	public OrderbookLogger(String directory, String logName, Orderbook orderbook, Type logType, Logger.Type type, boolean logToFile, boolean logToConsole) {
		super(directory, String.format("%s_orderbook%s", logName, orderbook.getIdentifier()), type, logToFile, logToConsole);
		if(logType == Type.ORDER_FLOW_LOG) {
			this.recordOrderFlowHeader();
		} else if(logType == Type.EVENT_LOG) {
			super.writeToFile(	"**********************************************\n" +
								"Misc events that happened in the orderbook\n" +
								"**********************************************\n\n");
		}
		else {
			World.errorLog.logError("Other types that ORDER_FLOW_LOG has not yet been implemented");
		}
		this.orderbook = orderbook;
	}
	
	private void recordOrderFlowHeader()	{
		String header =
				"LogNum\t" +
				"Time\t" +
				"Round\t" +
				"Event\t" +
				"ID\tSide\t" +
				"Type\t" +
				"Price\t" +
				"Volume\t" +
				"Total\t" +
				"Owner";
		
		if(Logging.logOrderbookEventsToConsole){
			super.writeToConsole(header);
		}
		if(this.logToFile){
			super.writeToFile(header);
		}
	}
	
	
	

	public void logEventAddOrder(Order order) {
		if (this.createLogString) {
			String line = super.getNewEntry() + "Add\t" + order.toStringForOrderbookLog();
			if(this.logToFile){
				super.writeToFile(line);
			}
			if(this.logToConsole) {
				this.writeToConsole(line);
			}
		}
	}

	public void logEventRemoveOrder(Order order) {
		if (this.createLogString) {
			String line = super.getNewEntry() + "Remove\t" + order.toStringForOrderbookLog();
			if(this.logToFile){
				super.writeToFile(line);
			}
			if(this.logToConsole){
				this.writeToConsole(line);
			}
		}
		
	}

	public void logEventUpdateOrderVolume(Order order, long tradeVolume) {
		if (this.createLogString) {
			String line = super.getNewEntry() + "UpdVol\t" + order.getID() + "\t\t\t\t" + -tradeVolume;
			if(this.logToFile){
				super.writeToFile(line);
			}
			if(this.logToConsole){
				this.writeToConsole(line);
			}
		}
	}

	public void logEventProcessNewOrder(Order order) {
		if (this.createLogString) {
			String line = super.getNewEntry() + "Process\t" + order.toStringForOrderbookLog();
			if(this.logToFile){
				super.writeToFile(line);
			}
			if(this.logToConsole){
				this.writeToConsole(line);
			}
		}
	}

	public void logEventMatch(Order newOrder, Order matchingOrder) {
		if (this.createLogString) {
			String line = super.getNewEntry() + "Match\t"
					+ matchingOrder.getID() + "," + newOrder.getID();
			try {
				line += String.format("\tOwner of standing order: %s ", matchingOrder.getOwner().getID());
			} catch(NullPointerException e) {
				
			}
			try {
				line += String.format("\tOwner of new order: %s ", newOrder.getOwner().getID());
			} catch(NullPointerException e) {
				
			}
			line += String.format("\tPrice: %s", matchingOrder.getPrice());
			if(this.logToFile){
				super.writeToFile(line);
			}
			if(this.logToConsole){
				this.writeToConsole(line);
			}
		}
	}

	public void logEventTransaction(TransactionReceipt receipt) {
		if (this.createLogString) {
			String line = super.getNewEntry() + "Transaction\t"
					+ receipt.toStringForLog();
			try {
				line += String.format("\tSent receipt to agent %s. Arrives in round %s", receipt.getOwnerOfFilledStandingOrder().getID(), receipt.getArrivalTime());
			} finally {
				if(this.logToFile){
					super.writeToFile(line);
				}
				if(this.logToConsole){
					this.writeToConsole(line);
				}	
			}
			
		}
	}

	public void logEventOrderExpired(Order order) {
		if (this.createLogString) {
			String line = super.getNewEntry() + "Expire\t" + order.getID();
			if(this.logToFile){
				super.writeToFile(line);
			}
			if(this.logToConsole){
				this.writeToConsole(line);
			}
		}
	}

	public void logEventNoMarketOrders(Order.BuySell buysell) {
		if(this.createLogString){
			String line = Logger.getNewEntry() + String.format("no%s",String.valueOf(buysell));
			if(this.logToFile){
				super.writeToFile(line);
			}
			if(Logging.logOrderbookEventsToConsole) {
				this.writeToConsole(line);
			}
		}
	}
	
	
	public void logOnelineEvent(String line) {
		line = super.getNewEntry() + line;
		if(this.logToFile){
			super.writeToFile(line);
		}
		if(this.logToConsole) {
			super.writeToConsole(line);
		}
	}

	// public void expireOrder(Order order){
	// if(Logging.logOrderbookOrderUpdates){
	// String line = super.getNewEntry() + "Found match\t";
	// super.writeToFile(line, this.file);
	// }
	// }

	// public void logTransaction(){
	//
	// }
}
