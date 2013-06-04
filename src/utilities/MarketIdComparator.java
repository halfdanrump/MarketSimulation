package utilities;

import java.util.Comparator;

import environment.Market;

public class MarketIdComparator implements Comparator<Market>{

	public MarketIdComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Market market1, Market market2) {
		// TODO Auto-generated method stub
		return (int) market1.getID() - market2.getID(); 
	}



}
