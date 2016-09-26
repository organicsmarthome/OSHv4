package constructMultiple;

public enum CHPConfiguration {
	
	NONE,
	DUMB,
	INTELLIGENT;
	
	public static String toShortName(CHPConfiguration config) {
		switch(config) {
		case NONE: return "nchp";
		case DUMB: return "chp";
		case INTELLIGENT: return "ichp";
		default: return null;
		}
	}

}
