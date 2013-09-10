package environment;

import experiments.Experiment;

public abstract class Message{
	
	public enum TransmissionType{
		INSTANTANEOUS,
		WITH_TRANSMISSION_DELAY,
//		WITHIN_SAME_ROUND
	}
	
	private int arrivalTime;
	private int dispatchTime;
	private TransmissionType transmissionType;
	private static long messageCount = 0;
	Experiment experiment;

	public Message(int arrivalTime, int dispatchTime, TransmissionType transmissionType, Experiment experiment){
		this.experiment = experiment;
		this.transmissionType = transmissionType;
		if(transmissionType == TransmissionType.INSTANTANEOUS) {
			this.arrivalTime = this.experiment.getWorld().getCurrentRound();
			this.dispatchTime = this.experiment.getWorld().getCurrentRound();
		} else if (transmissionType == TransmissionType.WITH_TRANSMISSION_DELAY) {
			if(arrivalTime < dispatchTime) {
				this.experiment.getWorld().errorLog.logError("Message with arrival time before dispatch time was created", this.experiment);
			}else {
				this.arrivalTime = arrivalTime;
				this.dispatchTime = dispatchTime;
			}
		} else {
			experiment.getWorld().errorLog.logError(String.format("Received transmission type: %s", transmissionType), experiment);
		}
		setMessageCount(getMessageCount() + 1);
	}
	
	public long getArrivalTime() {
		return arrivalTime;
	}
	public int getDispatchTime() {
		return dispatchTime;
	}
	
	public TransmissionType getTransmissionType() {
		return this.transmissionType;
	}
	
	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public void setDispatchTime(int dispatchTime) {
		this.dispatchTime = dispatchTime;
	}

	public static long getMessageCount() {
		return messageCount;
	}

	public static void setMessageCount(long messageCount) {
		Message.messageCount = messageCount;
	}
	
//	protected Object clone() CloneNotSupportedException{
//		return super.clone();
//	}
	
	
}
