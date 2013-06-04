package utilities;

import java.util.Comparator;

import environment.Order;

public class OrderIDComparatorLowFirst implements Comparator<Order>{
	public int compare(Order order1, Order order2) {
		// TODO Auto-generated method stub
		
		return (int) order1.getID() - order2.getID(); 
	}
}