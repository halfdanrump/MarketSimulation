package environment;

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
	
//	public Message(TransmissionType transmissionType){
//		/*
//		 * Constructor used for messages where the arrival time is given, or undefined. 	
//		 */
//		if(transmissionType == TransmissionType.WITH_TRANSMISSION_DELAY) {
//			World.errorLog.logError("Wrong constructor for Message type TransmissionType.WITH_TRANSMISSION_DELAY");
//		} else {
//			this.initializeWithInstantaneousTransmission();
//		}
//		setMessageCount(getMessageCount() + 1);
//	}
	
	public Message(int arrivalTime, int dispatchTime, TransmissionType transmissionType){
		if(transmissionType == TransmissionType.INSTANTANEOUS) {
			this.arrivalTime = World.getCurrentRound();
			this.dispatchTime = World.getCurrentRound();
//		} else if(transmissionType == TransmissionType.WITHIN_SAME_ROUND) {
//			this.initializeWithInstantaneousTransmission();
		} else if (transmissionType == TransmissionType.WITH_TRANSMISSION_DELAY) {
			if(arrivalTime < dispatchTime) {
				World.errorLog.logError("Message with arrival time before dispatch time was created");
			}else {
				this.arrivalTime = arrivalTime;
				this.dispatchTime = dispatchTime;
			}
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
