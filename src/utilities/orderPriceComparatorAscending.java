package utilities;

import java.util.Comparator;

import environment.Order;

public class orderPriceComparatorAscending implements Comparator<Order> {
	public int compare(Order order1, Order order2) {
		return (int) Math.signum(order1.getPrice() - order2.getPrice()); 
	}
}
