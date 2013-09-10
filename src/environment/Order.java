package environment;

import experiments.Experiment;
import agent.HFT;

public class Order extends Message{
	
	public enum Type {
		MARKET,
		LIMIT;
	}
	
	public enum BuySell {
		SELL,
		BUY,
	}
	
	private Orderbook orderbook;
	private BuySell buysell;
	private Type type;
	private long price;
	private long initialVolume;
	private long currentAgentSideVolume;
	private long currentOrderbookSideVolume;
	private long nRoundsInBook;
	private long expirationTime;
	private HFT owner;
	private int id;
	private static int orderCount = 0;
	private Experiment experiment;
	
	public Order(int transmissionDelay, int dispatchTime, int nRoundsInBook, long initialVolume, long price, Type type, BuySell buysell, HFT owner, Orderbook orderbook, TransmissionType transmissionType, Experiment experiment){
		/*
		 * Order submitted by HFTs
		 */
		super(experiment.getWorld().getCurrentRound() + transmissionDelay + 1, dispatchTime, transmissionType, experiment);
		this.id = Order.orderCount;
		Order.orderCount++;
		this.experiment = experiment;
		this.owner = owner;
//		if(dispatchTime < dispatchTime + transmissionDelay){
//			Exception e = new InvalidOrderException(arrivalTime, dispatchTime)
//		}
		
		/*
		 * Dealing with order price
		 */
		if(price <= 0 | price == Long.MAX_VALUE){
			new InvalidOrderException(initialVolume, price, owner, orderbook);
		} else{
			this.price = price;			
		}
		
		/*
		 * Dealing with order volume
		 */
		this.initialVolume = initialVolume;
		if(initialVolume <= 0){
			new InvalidOrderException(initialVolume, price, owner, orderbook);
		} else{
			this.currentAgentSideVolume = initialVolume;
			this.currentOrderbookSideVolume = initialVolume;
		}
		
		this.type = type;
		this.buysell = buysell;
		this.orderbook = orderbook;

		/*
		 * Dealing with temporal aspects
		 */
		if(transmissionDelay < 0) {
			this.experiment.getWorld().errorLog.logError("Invalid order", this.owner.getExperiment());
		}
		this.nRoundsInBook = nRoundsInBook;
		this.expirationTime = this.experiment.getWorld().getCurrentRound() + nRoundsInBook;
		if(transmissionType == TransmissionType.WITH_TRANSMISSION_DELAY) {
			if(super.getArrivalTime() < this.experiment.getWorld().getCurrentRound()){
				this.experiment.getWorld().warningLog.logOnelineWarning("New order was created. Arrival time was before current world time, so the order will never be used.");
			}
			this.experiment.getWorld().addNewOrder(this);
		} else if(transmissionType == TransmissionType.INSTANTANEOUS) {
			orderbook.processNewlyArrivedNewOrder(this);
		} else {
			this.experiment.getWorld().errorLog.logError("Order without specified TransmissionType was submitted", this.owner.getExperiment());
		}
	}
	
	public void updateAgentSideVolumeByDifference(Order.BuySell buysell, long volumeChange){
		if(buysell == Order.BuySell.BUY) {
			this.currentAgentSideVolume += volumeChange;
		} else if(buysell == Order.BuySell.SELL) {
			this.currentAgentSideVolume -= volumeChange;
		}
		this.currentAgentSideVolume += volumeChange;
	}
	
	public void updateMarketSideVolumeByDifference(long tradeVolume){
		this.currentOrderbookSideVolume += tradeVolume;
	}
	
	public Orderbook getOrderbook() {
		try{
			return orderbook;
		} catch(NullPointerException e){
			this.experiment.getWorld().errorLog.logError("Order doesn't belong to any orderbook!", this.owner.getExperiment());
			return null;
		}
	}

	public Type getType() {
		return this.type;
	}
	
	public BuySell getBuySell(){
		return this.buysell;
	}

	public long getPrice() {
		return this.price;
	}

	public long getCurrentAgentSideVolume() {
		return this.currentAgentSideVolume;
	}
	
	public long getCurrentMarketSideVolume() {
		return this.currentOrderbookSideVolume;
	}

	public long getExpirationTime() {
		return expirationTime;
	}

	@Override
	public String toString() {
		return "Order [orderbook=" + orderbook + ", buysell=" + buysell
				+ ", type=" + type + ", price=" + price + ", OSvolume=" + currentOrderbookSideVolume 
				+ ", ASvolume=" + currentAgentSideVolume  
				+ ", expirationTime=" + expirationTime + "]";
	}
	
	public String toStringForOrderbookLog(){
		return this.id  + "\t" +  buysell + "\t" + type + "\t" + price + "\t" +  currentOrderbookSideVolume;
	}
	

	public HFT getOwner() {
		try{
			return owner;
		} catch (NullPointerException e){
			this.experiment.getWorld().errorLog.logError("Tried to return owner of Order, but owner has not been set. Returning null.", this.owner.getExperiment());
			return null;
		}
	}
	
	public Stock getStock(){
		return this.orderbook.getStock();
	}
	
	public String getOwnerID() {
		if(owner == null) {
			return "NA";
		} else {
			return String.valueOf(this.owner.getID());
		}
	}
	
	public Market getMarket(){
		return this.orderbook.getMarket();
	}
	
	public long getArrivalTime(){
		return super.getArrivalTime();
	}
	
	public int getDispatchTime(){
		return super.getDispatchTime();
	}

	public void setOwner(HFT owner) {
		this.owner = owner;
	}
	
	public int getID(){
		return this.id;
	}
	
	public Order getCopy(){
		return this;
	}
	
	public long getNRoundsInBook(){
		return this.nRoundsInBook;
	}
	
	public static long getOrderCount(){
		return Order.orderCount;
	}
	
	public TransmissionType getTransmissionType() {
		return super.getTransmissionType();
	}
	
	public long getUnsignedInitialVolume() {
		return this.initialVolume;
	}
	
	public Experiment getExperiment() {
		return this.experiment;
	}

	public void printOrderDetails() {
		System.out.println(String.format("Owner: %s, price: %s, buysell: %s, volume: %s",  this.owner, this.price, this.buysell, this.initialVolume));
		
	}

	public boolean hasOwner() {
		// TODO Auto-generated method stub
		if (this.owner == null){
			return false;
		} else {
			return true;
		}
	}
	
}
