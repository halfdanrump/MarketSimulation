package environment;

public class OrderCancellation extends Message{
	private Order order;
	private static int cancellationCount = 0;
//	private Orderbook orderbook;
//	private int newPrice;
//	private int newVolume;

//	public OrderCancellation(int arrivalTime, int dispatchTime, Order order, Orderbook orderbook, int newPrice, int newVolume) {
//		super(arrivalTime, dispatchTime);
//		this.order = order;
//		this.orderbook = orderbook;
//		this.newPrice = newPrice;
//		this.newVolume = newVolume;
//	}
	
	public OrderCancellation(int arrivalTime, int dispatchTime, Order order) {
		super(arrivalTime, dispatchTime);
		this.order = order;
		cancellationCount ++;
		World.addOrderCancellation(this);
	}
	

	public Order getOrder() {
		return order;
	}
	
	public static int getCancellationCount(){
		return cancellationCount;
	}
//
//	public Orderbook getOrderbook() {
//		return orderbook;
//	}
//
//	public int getUpdatedPrice() {
//		return newPrice;
//	}
//	
//	public int getUpdatedVolume(){
//		return newVolume;
//	}
	
	

}
