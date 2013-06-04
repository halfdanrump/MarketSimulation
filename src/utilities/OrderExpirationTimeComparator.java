package utilities;
import environment.Order;

import java.util.Comparator;

public class OrderExpirationTimeComparator implements Comparator<Order> {

	@Override
	public int compare(Order order1, Order order2) {
		int i =  (int) (order1.getExpirationTime() - order2.getExpirationTime());
		return i;
	}


}
