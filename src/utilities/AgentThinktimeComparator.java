package utilities;

import java.util.Comparator;

import agent.HFT;

public class AgentThinktimeComparator implements Comparator<HFT>{

	
	public int compare(HFT agent1, HFT agent2) {
		return (int) (agent1.getFinishedThinkingTime() - agent2.getFinishedThinkingTime());
	}

}
