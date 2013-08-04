package environment;

import Experiments.Experiment;
import agent.HFT;
import environment.Order.BuySell;
import environment.World;

public class TransactionReceipt extends Message{
	private static long receiptsCount = 0;
	private long id;
	private long volume;
	private long price;
	private long total;
	private Order filledOrder;
	private Order fillingOrder;
	private Experiment experiment;
	
	
	TransactionReceipt(Order order, long volume, long price, long total, Order fillingOrder){
		super(calculateArrivalTime(order, order.getExperiment()), order.getOwner().getExperiment().getWorld().getCurrentRound(), Message.TransmissionType.WITH_TRANSMISSION_DELAY, order.getExperiment());
		this.setId(receiptsCount);		
		receiptsCount++;
		this.volume = volume;
		this.price = price;
		this.total = total;
		this.experiment = order.getOwner().getExperiment();
		
		if(this.volume < 0){
			this.experiment.getWorld().errorLog.logError("Receipt volume must be positive!", this.experiment);
		}
		if(this.price < 0){
			Exception e = new Exception();
			e.printStackTrace();
			this.experiment.getWorld().errorLog.logError("Receipt price must be positive!", this.experiment);
		}
		this.filledOrder = order;
		this.initialize();
		/*
		 * The transaction receipt is issued to the agent who owns the order which was filled. 
		 * The filling order is the order on the other side of the transaction
		 */
		this.fillingOrder = fillingOrder;
	}
	
	private static int calculateArrivalTime(Order order, Experiment experiment) {
		int latency = order.getOwner().getLatency(order.getMarket());
		int arrivalTime = experiment.getWorld().getCurrentRound() + latency;
		return arrivalTime;
	}
	
	private void initialize(){
		this.experiment.getWorld().addTransactionReceipt(this);
	}
	
	
	
	public HFT getOwnerOfFilledStandingOrder(){
		return this.filledOrder.getOwner();
	}
	
	
	
	
	public String toStringForLog(){
		return this.filledOrder.getBuySell() + "\t\t" + price + "\t" + volume + "\t" + total;
	}
	
	
//	public long getSignedTotal(){
//		if(this.filledOrder.getBuySell() == Order.BuySell.BUY){
//			return -1*Math.abs(total);
//		} else{
//			return Math.abs(total);
//		}
//		
//	}
//	
	
//	public long getAbsoluteTotal(){
//		return this.total;
//	}
	
	public long getTotal() {
		return this.total;
	}

	
	public Stock getStock() {
		return this.filledOrder.getStock();
	}

	
//	public long getSignedVolume() {
//		if(this.filledOrder.getBuySell() == Order.BuySell.BUY){
//			return Math.abs(volume);
//		} else{
//			return -1*Math.abs(volume);
//		}
//	}
	
	
	public long getUnsignedVolume() {
		if(this.volume < 0) {
			this.experiment.getWorld().errorLog.logError("Transaction receipt had negative volume, but the stored volume should be unsigned, and hence always positive", this.experiment);
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

	public Order getFillingOrder() {
		return fillingOrder;
	}

	public Experiment getExperiment() {
		return this.experiment;
	}
	
	
	
	
	
}
