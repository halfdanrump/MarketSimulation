package log;

import environment.Order;
import environment.Orderbook;
import environment.TransactionReceipt;

public class OrderbookLogger extends Logger implements Logging{
	@SuppressWarnings("unused")
	private Orderbook orderbook;

	public OrderbookLogger(String directory, String logName, Orderbook orderbook, boolean recordHeader) {
		super(directory, String.format("%s_orderbook%s", logName, orderbook.getIdentifier()));
		if(recordHeader) {
			this.recordHeader();
		}
		this.orderbook = orderbook;
	}
	
	private void recordHeader()	{
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
		super.writeToFile(header);
	}
	
	
	

	public void logEventAddOrder(Order order) {
		if (Logging.logOrderbookAddOrder) {
			String line = super.getNewEntry() + "Add\t"
					+ order.toStringForOrderbookLog();
			super.writeToFile(line);
		}
	}

	public void logEventRemoveOrder(Order order) {
		if (Logging.logOrderbookRemoveOrder) {
			String line = super.getNewEntry() + "Remove\t" + order.toStringForOrderbookLog();
			super.writeToFile(line);
			if(Logging.logOrderbookEventsToConsole){
				this.writeToConsole(line);
			}
		}
		
	}

	public void logEventUpdateOrderVolume(Order order, int volChange) {
		if (Logging.logOrderbookUpdateOrderVolume) {
			String line = super.getNewEntry() + "UpdVol\t" + order.getID() + "\t\t\t\t" + -volChange;
			super.writeToFile(line);
			if(Logging.logOrderbookEventsToConsole){
				this.writeToConsole(line);
			}
		}
	}

	public void logEventProcessNewOrder(Order order) {
		if (Logging.logOrderbookProcessOrder) {
			String line = super.getNewEntry() + "Process\t" + order.toStringForOrderbookLog();
			super.writeToFile(line);
			if(Logging.logOrderbookEventsToConsole){
				this.writeToConsole(line);
			}
		}
	}

	public void logEventMatch(Order newOrder, Order matchingOrder) {
		if (Logging.logOrderbookMatches) {
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
			super.writeToFile(line);
			if(Logging.logOrderbookEventsToConsole){
				this.writeToConsole(line);
			}
		}
	}

	public void logEventTransaction(TransactionReceipt receipt) {
		if (Logging.logOrderbookTransactions) {
			String line = super.getNewEntry() + "Transaction\t"
					+ receipt.toStringForLog();
			try {
				line += String.format("\tSent receipt to agent %s. Arrives in round %s", receipt.getOwner().getID(), receipt.getArrivalTime());
			} finally {
				super.writeToFile(line);
				if(Logging.logOrderbookEventsToConsole){
					this.writeToConsole(line);
				}	
			}
			
		}
	}

	public void logEventOrderExpired(Order order) {
		if (Logging.logOrderbookOrderExpire) {
			String line = super.getNewEntry() + "Expire\t" + order.getID();
			super.writeToFile(line);
			if(Logging.logOrderbookEventsToConsole){
				this.writeToConsole(line);
			}
		}
	}

	public void logEventNoMarketOrders(Order.BuySell buysell) {
		String line = Logger.getNewEntry() + String.format("no%s",String.valueOf(buysell));
		super.writeToFile(line);
		if(Logging.logOrderbookEventsToConsole) {
			this.writeToConsole(line);
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
