package utilities;


import environment.Orderbook;
import environment.World;

public class LogUtils {

	
	public static int getNumberOfOrdersInTransit(){
		return World.getOrdersInTransit().size();
	}
	
	public static int getNumberOfUnfilledBuyOrders(){
//		ArrayList<Orderbook> = World.getOrderbooks();
		int nOrders = 0;
		for(Orderbook orderbook:World.getOrderbooks()){
			nOrders += orderbook.getUnfilledBuyOrders().size();
		}
		return nOrders;
	}
	
	public static int getNumberOfUnfilledSellOrders(){
//		ArrayList<Orderbook> = World.getOrderbooks();
		int nOrders = 0;
		for(Orderbook orderbook:World.getOrderbooks()){
			nOrders += orderbook.getUnfilledBuyOrders().size();
		}
		return nOrders;
	}
	
	public static int getNumberOfUnfilledOrders(){
		return getNumberOfUnfilledBuyOrders() + getNumberOfUnfilledSellOrders();
	}
	

}
