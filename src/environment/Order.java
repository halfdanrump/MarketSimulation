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
	private int price;
	private int agentSideVolume;
	private int orderbookSideVolume;
	private int nRoundsInBook;
	private int expirationTime;
	private HFT owner;
	private int id;
	private static int orderCount = 0;
	
	
	public Order(int arrivalTime, int dispatchTime, int nRoundsInBook, int initialVolume, int price, Type type, BuySell buysell, HFT owner, Orderbook orderbook){
		/*
		 * Order submitted by HFTs
		 */
		super(arrivalTime, dispatchTime);
		this.owner = owner;
//		if(arrivalTime < dispatchTime){
//			Exception e = new InvalidOrderException(arrivalTime, dispatchTime)
//		}¹
		
		if(price <0 | price == Integer.MAX_VALUE | price == 0){
			new InvalidOrderException(initialVolume, price, owner, orderbook);
		} else{
			this.price = price;			
		}
		
		if(initialVolume < 0){
			new InvalidOrderException(initialVolume, price, owner, orderbook);
		} else{
			this.agentSideVolume = initialVolume;
			this.orderbookSideVolume = initialVolume;
		}
		
		if(arrivalTime < World.getCurrentRound()){
			World.warningLog.logOnelineWarning("New order was created. Arrival time was before current world time, so the order will never be used.");
		}
		
		
		this.nRoundsInBook = nRoundsInBook;
		this.type = type;
		this.buysell = buysell;
		this.orderbook = orderbook;
		this.expirationTime = World.getCurrentRound() + nRoundsInBook;
		this.id = Order.orderCount;
		Order.orderCount++;
		World.addNewOrder(this);
	}
	
//	public void setVolume(int newVolume){
//		if(newVolume > 0){
//			this.volume = newVolume;			
//		} else if (newVolume == 0){
//			System.out.println("Updated order volume to 0!");
//			System.exit(1);
//		}
//	}
	
	public void updateAgentSideVolumeByDifference(int volumeChange){
		this.agentSideVolume += volumeChange;
	}
	
	public void updateOrderbookSideVolumeByDifference(int volumeChange){
		this.orderbookSideVolume += volumeChange;
	}
	
	public Orderbook getOrderbook() {
		try{
			return orderbook;
		} catch(NullPointerException e){
			World.errorLog.logError("Order doesn't beint to any orderbook!");
			return null;
		}
	}

	public Type getType() {
		return this.type;
	}
	
	public BuySell getBuySell(){
		return this.buysell;
	}

	public int getPrice() {
		return this.price;
	}

	public int getAgentSideVolume() {
		return this.agentSideVolume;
	}
	
	public int getOrderbookSideVolume() {
		return this.orderbookSideVolume;
	}

	public int getExpirationTime() {
		return expirationTime;
	}

	@Override
	public String toString() {
		return "Order [orderbook=" + orderbook + ", buysell=" + buysell
				+ ", type=" + type + ", price=" + price + ", OSvolume=" + orderbookSideVolume 
				+ ", ASvolume=" + agentSideVolume  
				+ ", expirationTime=" + expirationTime + "]";
	}
	
	public String toStringForOrderbookLog(){
		return this.id  + "\t" +  buysell + "\t" + type + "\t" + price + "\t" +  orderbookSideVolume;
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
	
	public int getArrivalTime(){
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
	
	public int getNRoundsInBook(){
		return this.nRoundsInBook;
	}
	
	public static int getOrderCount(){
		return Order.orderCount;
	}
	
}
