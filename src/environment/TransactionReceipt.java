package environment;

import agent.HFT;
import environment.Order.BuySell;
import environment.World;

public class TransactionReceipt extends Message{
	private static long receiptsCount = 0;
	private long id;
	private long volume;
	private long price;
	private long total;
	private HFT owner;
	private Order filledOrder;
	
	
	TransactionReceipt(Order order, long volume, long price, long total){
		super(calculateArrivalTime(order), World.getCurrentRound(), Message.TransmissionType.WITH_TRANSMISSION_DELAY);
		this.setId(receiptsCount);		
		receiptsCount++;
		this.volume = volume;
		this.price = price;
		this.total = total;
		
		if(this.volume < 0){
			World.errorLog.logError("Receipt volume must be positive!");
		}
		if(this.price < 0){
			Exception e = new Exception();
			e.printStackTrace();
			World.errorLog.logError("Receipt price must be positive!");
		}
		
		
		this.filledOrder = order;
		this.owner = order.getOwner();
		this.initialize();
	}
	
	private static int calculateArrivalTime(Order order) {
		int latency = order.getOwner().getLatency(order.getMarket());
		int arrivalTime = World.getCurrentRound() + latency;
		return arrivalTime;
	}
	
	private void initialize(){
		World.addTransactionReceipt(this);
	}
	
	
	
	public HFT getOwner(){
		return this.owner;
	}
	
	
	public String toStringForLog(){
		return this.filledOrder.getBuySell() + "\t\t" + price + "\t" + volume + "\t" + total;
	}
	
	
	public long getSignedTotal(){
		if(this.filledOrder.getBuySell() == Order.BuySell.BUY){
			return -1*Math.abs(total);
		} else{
			return Math.abs(total);
		}
		
	}
	
	
	public long getAbsoluteTotal(){
		return this.total;
	}

	
	public Stock getStock() {
		return this.filledOrder.getStock();
	}

	
	public long getSignedVolume() {
		if(this.filledOrder.getBuySell() == Order.BuySell.BUY){
			return Math.abs(volume);
		} else{
			return -1*Math.abs(volume);
		}
	}
	
	
	public long getUnsignedVolume() {
		if(this.volume < 0) {
			World.errorLog.logError("Transaction receipt had negative volume, but the stored volume should be unsigned, and hence always positive");
		}
		return this.volume;
	}
	

	public long getPrice() {
		return price;
	}
	

	public BuySell getBuySell() {
		return this.filledOrder.getBuySell();
	}

	
	public int getDispatchTime() {
		return super.getDispatchTime();
	}

	public long getArrivalTime() {
		return super.getArrivalTime();
	}
	
	public Order getFilledOrder(){
		return this.filledOrder;
	}

	public Object getOrderbook() {
		return this.filledOrder.getOrderbook();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public static long getReceiptCount(){
		return receiptsCount;
	}

	
	
	
	
	
	
}
