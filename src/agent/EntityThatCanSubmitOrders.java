package agent;

public interface EntityThatCanSubmitOrders {
	public enum Type{
		HFT,
		MARKET,
		STYLIZED_TRADER
	}
}
