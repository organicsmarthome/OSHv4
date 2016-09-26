package constructMultiple;

public enum PLSType {

	FULL,
	HALF_POS,
	HALF_NEG,
	NONE;
	
	public static String toShortString(PLSType elem) {
		switch (elem) {
		case FULL:
			return "PLS";
		case HALF_POS:
			return "posPLS";
		case HALF_NEG:
			return "negPLS";
		case NONE:
			return "noPLS";
		default:
			return null;			
		}
	}
}
