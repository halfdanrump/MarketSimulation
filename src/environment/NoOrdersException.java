package environment;

import environment.Order.BuySell;


@SuppressWarnings("serial")
public class NoOrdersException extends Exception{
	Orderbook orderbook;
	long time;
	BuySell buysell;
	private static long count = 0; 
	
	public NoOrdersException(long time, Orderbook orderbook, BuySell buysell){
		count ++;
		this.time = time;
		this.orderbook = orderbook;
		this.buysell = buysell;
		World.warningLog.logOnelineWarning(String.format("Orderbook %s contained no %s order", orderbook.getIdentifier(), buysell));
	}
	
	public long getCount() {
		return count;
	}
}
