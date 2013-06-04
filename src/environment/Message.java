package environment;



public abstract class Message{
	private int arrivalTime;
	private int dispatchTime;
	private Object sender;
	private Object recipient;
	private static int messageCount = 0;
	
	public Message(){
		
	}
	
	public Message(int arrivalTime, int dispatchTime){
		this.arrivalTime = arrivalTime;
		this.dispatchTime = dispatchTime;
		setMessageCount(getMessageCount() + 1);
	}
	
	
	
	public int getArrivalTime() {
		return arrivalTime;
	}
	public int getDispatchTime() {
		try{
			return dispatchTime;
		} catch(NullPointerException e){
			World.errorLog.logError("Tried to return dispatchTime, but the field has not been initialized. Returning 0.");
			return 0;
		}
	}
	
	public Object getSender() {
		return sender;
	}
	public Object getRecipient() {
		return recipient;
	}
	
	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public void setDispatchTime(int dispatchTime) {
		this.dispatchTime = dispatchTime;
	}
	public void setSender(Object sender) {
		this.sender = sender;
	}
	public void setRecipient(Object recipient) {
		this.recipient = recipient;
	}

	public static int getMessageCount() {
		return messageCount;
	}

	public static void setMessageCount(int messageCount) {
		Message.messageCount = messageCount;
	}
	
//	protected Object clone() CloneNotSupportedException{
//		return super.clone();
//	}
	
	
}
