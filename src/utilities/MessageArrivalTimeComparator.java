package utilities;

import java.util.Comparator;

import environment.Message;

public class MessageArrivalTimeComparator implements Comparator<Message> {

	@Override
	public int compare(Message m1, Message m2) {
		return (int) (m1.getArrivalTime() - m2.getArrivalTime());
	}
}

