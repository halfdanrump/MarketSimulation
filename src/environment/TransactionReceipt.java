package environment;

import agent.HFT;
import environment.Order.BuySell;
import environment.World;

public class TransactionReceipt extends Message{
	private static int receiptsCount = 0;
	private int id;
	private int volume;
	private int price;
	private int total;
	private HFT owner;
	private int dispatchTime, arrivalTime;
	private Order filledOrder;
	
	
	TransactionReceipt(Order order, int volume, int price, int total){
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
		
		this.dispatchTime = World.getCurrentRound();
		this.filledOrder = order;
		this.owner = order.getOwner();
		this.arrivalTime = World.getCurrentRound() + owner.getLatency(order.getMarket());
		this.initialize();
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
	
	public int getSignedTotal(){
		if(this.filledOrder.getBuySell() == Order.BuySell.BUY){
			return -1*Math.abs(total);
		} else{
			return Math.abs(total);
		}
		
	}
	
	public int getAbsoluteTotal(){
		return this.total;
	}

	public Stock getStock() {
		return this.filledOrder.getStock();
	}

	public int getSignedVolume() {
		if(this.filledOrder.getBuySell() == Order.BuySell.BUY){
			return Math.abs(volume);
		} else{
			return -1*Math.abs(volume);
		}
	}
	
	public int getAbsoluteVolume(){
		return this.volume;
	}

	public int getPrice() {
		return price;
	}

	public BuySell getBuySell() {
		return this.filledOrder.getBuySell();
	}

	public int getDispatchTime() {
		return dispatchTime;
	}

	public int getArrivalTime() {
		return arrivalTime;
	}
	
	public Order getOriginalOrder(){
		return this.filledOrder;
	}

	public Object getOrderbook() {
		return this.filledOrder.getOrderbook();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public static int getReceiptCount(){
		return receiptsCount;
	}
	
	
	
	
	
}
