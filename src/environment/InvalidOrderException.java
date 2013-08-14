package environment;

import agent.HFT;

@SuppressWarnings("serial")
public class InvalidOrderException extends Exception {
	long price;
	long volume;
	HFT owner;
	Orderbook orderbook;
	
	public InvalidOrderException(long initialVolume, long price2, HFT owner, Orderbook orderbook){
		this.price = price2;
		this.volume = initialVolume;
		this.owner = owner;
		this.orderbook = orderbook;
		if(owner == null){
			orderbook.getExperiment().getWorld().errorLog.logError(String.format("Stylized trader submitted invalid order: %s", this.toStringWithoutOwner()), this.owner.getExperiment());
		} else{
			owner.eventlog.logError(String.format("Agent %s submitted invalid order %s", owner.getID(), this.toStringWithOwner()), this.owner.getExperiment());
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
