package constructsimulation.datatypes;

public enum AppliancesTypes {

	DUMB,
	DELAYABLE,
	INTERRUPTIBLE,
	HYBRID,
	HYBRID_DELAYABLE,
	HYBRID_INTERRUPTIBLE;


	private static final boolean[] dumbAppliances = {
			true,  true,  true,  true,  true, // available (IH, DW, OV, TD, WM)
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false
	};

	private static final boolean[] delayableAppliances = {
			true,  false, true,  false, false, // available (IH, DW, OV, TD, WM)
			false, true,  false, true,  true,
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false
	};

	private static final boolean[] interruptibleAppliances = {
			true,  false, true,  false, false, // available (IH, DW, OV, TD, WM)
			false, false, false, false, false,
			false, true,  false, true,  true,		
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false
	};

	private static final boolean[] hybridAppliances = {
			true,  false, true,  false, false, // available (IH, DW, OV, TD, WM)
			false, false, false, false, false,
			false, false, false, false, false,
			false, true,  false, true,  true,
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false
	};

	private static final boolean[] hybridDelayableAppliances = {
			true,  false, true,  false, false, // available (IH, DW, OV, TD, WM)
			false, false, false, false, false,
			false, false, false, false, false,
			false, false, false, false, false,
			false, true,  false, true,  true,			
			false, false, false, false, false,
			false, false, false, false, false
	};

//	private static final boolean[] genericAppliancesHybridInterruptibleAppliances = {
//			true,  false, true,  false, false, // available (IH, DW, OV, TD, WM)
//			false, false, false, false, false,
//			false, false, false, false, false,
//			false, false, false, false, false,
//			false, true,  false, true,  true,			
//			false, false, false, false, false			
//	};

	public boolean[] getDeviceValues() {
		switch(this) {
		case DELAYABLE:
			return delayableAppliances;
		case DUMB:
			return dumbAppliances;
		case HYBRID:
			return hybridAppliances;
		case HYBRID_DELAYABLE:
			return hybridDelayableAppliances;
		case HYBRID_INTERRUPTIBLE:
			throw new IllegalArgumentException("Is not working");			
		case INTERRUPTIBLE:
			return interruptibleAppliances;
		default:
			throw new IllegalArgumentException("Does not exists");		
		}
	}
}
