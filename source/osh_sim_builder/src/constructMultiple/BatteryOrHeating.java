package constructMultiple;

public enum BatteryOrHeating {
	
	NONE,
	BATTERY,
	INSERTHEATING;
	
	public static String toShortString(BatteryOrHeating elem) {
		switch (elem) {
		case BATTERY:
			return "bat";
		case INSERTHEATING:
			return "ihe";
		case NONE:
			return "nihebat";
		default:
			return null;			
		}
	}

}
