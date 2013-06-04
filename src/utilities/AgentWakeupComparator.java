package utilities;

import java.util.Comparator;

import agent.HFT;

public class AgentWakeupComparator implements Comparator<HFT> {

	@Override
	public int compare(HFT agent1, HFT agent2) {
		return (int) (agent1.getWakeupTime() - agent2.getWakeupTime());
	}

}
