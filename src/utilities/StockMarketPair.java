package utilities;

import environment.Market;
import environment.Stock;

public class StockMarketPair{

	private Object first;
	private Object second;
	
	public StockMarketPair(String stockID, String marketID){
		this.first = stockID;
		this.second = marketID;
	}
	
	public StockMarketPair(Stock stock, Market market){
		this.first = stock;
		this.second = market;
	}
	
	public Object getFirst(){
		return this.first;
	}
	
	public Object getSecond(){
		return this.second;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockMarketPair other = (StockMarketPair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}
	
	
	
	
	
	
}
