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
		super(order.getMarket().getExperiment().getWorld().getCurrentRound() + transmissionDelay + 1, dispatchTime, order.getTransmissionType(), order.getOwner().getExperiment());
		this.order = order;
		cancellationCount ++;
		order.getMarket().getExperiment().getWorld().addOrderCancellation(this);
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
