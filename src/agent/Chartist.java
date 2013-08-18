package agent;

import java.util.HashMap;
import java.util.LinkedList;

import Experiments.Experiment;
import environment.Market;
import environment.NoOrdersException;

public class Chartist extends HFT {
	private int movingAverageLength;
	private HashMap<Market, LinkedList<Integer>> knownMidSpreads;
	
	public Chartist(int movingAverageLength, int[] stockIDs, int[] marketIDs, int[] latencies, int group, Experiment experiment) {
		super(stockIDs, marketIDs, latencies, group, experiment);
		this.movingAverageLength = movingAverageLength;
		/*
		 * Initialize knownMidSpreads data structure
		 */
		this.knownMidSpreads = new HashMap<Market, LinkedList<Integer>>();
		for(int mID:marketIDs) {
			Market market = experiment.getWorld().getMarketByNumber(mID);
			this.knownMidSpreads.put(market, new LinkedList<Integer>());
		}
	}

	@Override
	protected long getWaitingTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void storeMarketInformation() throws NoOrdersException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean executeStrategyAndSubmit() {
		// TODO Auto-generated method stub
		return false;
	}

}
