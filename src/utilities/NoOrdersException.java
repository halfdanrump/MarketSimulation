package utilities;

import environment.Order.BuySell;
import environment.Orderbook;
import environment.World;

@SuppressWarnings("serial")
public class NoOrdersException extends Exception{
	Orderbook orderbook;
	int time;
	BuySell buysell;
	private static int count = 0; 
	
	public NoOrdersException(int time, Orderbook orderbook, BuySell buysell){
		count ++;
		this.time = time;
		this.orderbook = orderbook;
		this.buysell = buysell;
		World.warningLog.logOnelineWarning(String.format("Orderbook %s contained no %s order", orderbook.getIdentifier(), buysell));
	}
	
	public int getCount() {
		return count;
	}
}
