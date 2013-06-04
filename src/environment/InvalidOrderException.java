package environment;

import agent.HFT;

@SuppressWarnings("serial")
public class InvalidOrderException extends Exception {
	int price;
	int volume;
	HFT owner;
	Orderbook orderbook;
	
	public InvalidOrderException(int volume, int price, HFT owner, Orderbook orderbook){
		this.price = price;
		this.volume = volume;
		this.owner = owner;
		this.orderbook = orderbook;
		if(owner == null){
			World.errorLog.logError(String.format("Stylized trader submitted invalid order: %s", this.toStringWithoutOwner()));
		} else{
			owner.eventlog.logError(String.format("Agent %s submitted invalid order %s", owner.getID(), this.toStringWithOwner()));
		}
	}
	
	public String toStringWithOwner() {
		return "InvalidOrderException [price=" + price + ", volume=" + volume
				+ ", owner=" + owner.getID() + ", orderbook=" + orderbook.getIdentifier() + "]";
	}
	
	public String toStringWithoutOwner() {
		return "InvalidOrderException [price=" + price + ", volume=" + volume
				+ ", orderbook=" + orderbook.getIdentifier() + "]";
	}
	
	
	
	
}
