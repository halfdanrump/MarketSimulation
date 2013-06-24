package environment;

public class OrderCancellation extends Message{
	private Order order;
	private static long cancellationCount = 0;
//	private Orderbook orderbook;
//	private long newPrice;
//	private long newVolume;

//	public OrderCancellation(long arrivalTime, long dispatchTime, Order order, Orderbook orderbook, long newPrice, long newVolume) {
//		super(arrivalTime, dispatchTime);
//		this.order = order;
//		this.orderbook = orderbook;
//		this.newPrice = newPrice;
//		this.newVolume = newVolume;
//	}
	
	public OrderCancellation(int dispatchTime, int transmissionDelay, Order order) {
		super(World.getCurrentRound() + transmissionDelay + 1, dispatchTime, order.getTransmissionType());
		this.order = order;
		cancellationCount ++;
		World.addOrderCancellation(this);
	}
	

	public Order getOrder() {
		return order;
	}
	
	public static long getCancellationCount(){
		return cancellationCount;
	}
//
//	public Orderbook getOrderbook() {
//		return orderbook;
//	}
//
//	public long getUpdatedPrice() {
//		return newPrice;
//	}
//	
//	public long getUpdatedVolume(){
//		return newVolume;
//	}
	
	

}
