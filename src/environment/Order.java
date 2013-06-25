package environment;

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
	
	
	public Order(int transmissionDelay, int dispatchTime, int nRoundsInBook, long initialVolume, long price, Type type, BuySell buysell, HFT owner, Orderbook orderbook, TransmissionType transmissionType){
		/*
		 * Order submitted by HFTs
		 */
		super(World.getCurrentRound() + transmissionDelay + 1, dispatchTime, transmissionType);
//		Exception e = new Exception();
//		e.printStackTrace();
		this.owner = owner;
//		if(arrivalTime < dispatchTime){
//			Exception e = new InvalidOrderException(arrivalTime, dispatchTime)
//		}
		if(price <= 0 | price == Integer.MAX_VALUE){
			new InvalidOrderException(initialVolume, price, owner, orderbook);
		} else{
			this.price = price;			
		}
		if(initialVolume <= 0){
			new InvalidOrderException(initialVolume, price, owner, orderbook);
		} else{
			this.currentAgentSideVolume = initialVolume;
			this.currentOrderbookSideVolume = initialVolume;
		}
		this.nRoundsInBook = nRoundsInBook;
//		Exception e = new Exception();
//		e.printStackTrace();
		this.type = type;
		this.buysell = buysell;
		this.orderbook = orderbook;
		this.expirationTime = World.getCurrentRound() + nRoundsInBook;
		this.id = Order.orderCount;
		Order.orderCount++;
		if(transmissionType == TransmissionType.WITH_TRANSMISSION_DELAY) {
			if(super.getArrivalTime() < World.getCurrentRound()){
				World.warningLog.logOnelineWarning("New order was created. Arrival time was before current world time, so the order will never be used.");
			}
			World.addNewOrder(this);
		} else if(transmissionType == TransmissionType.INSTANTANEOUS) {
			orderbook.processNewlyArrivedNewOrder(this);
		} else {
			World.errorLog.logError("Order without specified TransmissionType was submitted");
		}
		
		
	}
	
	public void updateAgentSideVolumeByDifference(long volumeChange){
		this.currentAgentSideVolume += volumeChange;
	}
	
	public void updateOrderbookSideVolumeByDifference(long tradeVolume){
		this.currentOrderbookSideVolume += tradeVolume;
	}
	
	public Orderbook getOrderbook() {
		try{
			return orderbook;
		} catch(NullPointerException e){
			World.errorLog.logError("Order doesn't belong to any orderbook!");
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
			World.errorLog.logError("Tried to return owner of Order, but owner has not been set. Returning null.");
			return null;
		}
	}
	
	public Stock getStock(){
		return this.orderbook.getStock();
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
	
	public long getInitialVolume() {
		return this.initialVolume;
	}
	
}
