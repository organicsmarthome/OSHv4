package constructMultiple;

public enum DeviceConfiguration {
	
	NORMAL,
	DELAYABLE,
	INTERRUPTIBLE,
	HYBRID,
	HYBRID_DELAYABLE,
	HYBRID_INTERRUPTIBLE,
	HYBRID_SINGLE;
	
	public boolean[] getApplianceValues() {
		switch (this) {
		case NORMAL: return normalConf;
		case DELAYABLE: return delayableConf;
		case INTERRUPTIBLE: return interrConf;
		case HYBRID: return hybrid;
		case HYBRID_DELAYABLE: return hybridDelayble;
		case HYBRID_INTERRUPTIBLE: return hybridInterr;
		case HYBRID_SINGLE: return hybridSingle;
		default: return null;
		}
	}
	
	public static boolean[] getAppliancesValues(DeviceConfiguration config) {
		switch (config) {
		case NORMAL: return normalConf;
		case DELAYABLE: return delayableConf;
		case INTERRUPTIBLE: return interrConf;
		case HYBRID: return hybrid;
		case HYBRID_DELAYABLE: return hybridDelayble;
		case HYBRID_INTERRUPTIBLE: return hybridInterr;
		case HYBRID_SINGLE: return hybridSingle;
		default: return null;
		}
	}
	
	public static String toShortName(DeviceConfiguration config) {
		switch (config) {
		case NORMAL: return "app";
		case DELAYABLE: return "dapp";
		case INTERRUPTIBLE: return "iapp";
		case HYBRID: return "happ";
		case HYBRID_DELAYABLE: return "hdapp";
		case HYBRID_INTERRUPTIBLE: return "hiapp";
		case HYBRID_SINGLE: return "hsapp";
		default: return null;
		}
	}
	
	//col:
	//  IH,    DW     OV     TD     WM
	
	//row:
	/*										normal
	 * 										delayable
	 * 										interruptible
	 * 										hybrid
	 * 										hybrid delayable
	 * 										hybrid interruptible
	 * 										hybrid single
	 */
	
	private static final boolean[] normalConf = {
		true,  true,  true,  true,  true,				
		false, false, false, false, false, 			
		false, false, false, false, false,
		false, false, false, false, false,
		false, false, false, false, false,
		false, false, false, false, false,
		false, false, false, false, false
	};
	
	//col:
	//  IH,    DW     OV     TD     WM
	
	//row:
	/*										normal
	 * 										delayable
	 * 										interruptible
	 * 										hybrid
	 * 										hybrid delayable
	 * 										hybrid interruptible
	 * 										hybrid single
	 */
	
	private static final boolean[] delayableConf = {
		true,  false, true,  false, false,				
		false, true,  false, true,  true, 			
		false, false, false, false, false,
		false, false, false, false, false,
		false, false, false, false, false,
		false, false, false, false, false,
		false, false, false, false, false,
	};
	
	//col:
	//  IH,    DW     OV     TD     WM
	
	//row:
	/*										normal
	 * 										delayable
	 * 										interruptible
	 * 										hybrid
	 * 										hybrid delayable
	 * 										hybrid interruptible
	 * 										hybrid single
	 */
	
	private static final boolean[] interrConf = {
		true,  false, true,  false,  false,				
		false, false, false, false, false, 			
		false, true,  false, true, true,
		false, false, false, false, false,
		false, false, false, false, false,
		false, false, false, false, false,
		false, false, false, false, false,
	};
	
	//col:
	//  IH,    DW     OV     TD     WM
	
	//row:
	/*										normal
	 * 										delayable
	 * 										interruptible
	 * 										hybrid
	 * 										hybrid delayable
	 * 										hybrid interruptible
	 * 										hybrid single
	 */
	
	private static final boolean[] hybrid = {
		false, false, false, false, false,			
		false, false, false, false, false, 			
		false, false, false, false, false,
		true,  true,  true,  true,  true,		
		false, false, false, false, false,
		false, false, false, false, false,
		false, false, false, false, false,
	};
	
	//col:
	//  IH,    DW     OV     TD     WM
	
	//row:
	/*										normal
	 * 										delayable
	 * 										interruptible
	 * 										hybrid
	 * 										hybrid delayable
	 * 										hybrid interruptible
	 * 										hybrid single
	 */
	private static final boolean[] hybridDelayble = {
		false, false, false, false, false,			
		false, false, false, false, false, 			
		false, false, false, false, false,
		true,  false, true,  false, false,		
		false, true,  false, true,  true,
		false, false, false, false, false,
		false, false, false, false, false,
	};
	
	//col:
	//  IH,    DW     OV     TD     WM
	
	//row:
	/*										normal
	 * 										delayable
	 * 										interruptible
	 * 										hybrid
	 * 										hybrid delayable
	 * 										hybrid interruptible
	 * 										hybrid single
	 */
	private static final boolean[] hybridInterr = {
		false, false, false, false, false,			
		false, false, false, false, false, 			
		false, false, false, false, false,
		true,  false, true,  false, false,	
		false, false, false, false, false,
		false, true,  false, true,  true,
		false, false, false, false, false,
	};
	
	//col:
	//  IH,    DW     OV     TD     WM
	
	//row:
	/*										normal
	 * 										delayable
	 * 										interruptible
	 * 										hybrid
	 * 										hybrid delayable
	 * 										hybrid interruptible
	 * 										hybrid single
	 */
	private static final boolean[] hybridSingle = {
		false, false, false, false, false,			
		false, false, false, false, false, 			
		false, false, false, false, false,
		false, false, false, false, false,	
		false, false, false, false, false,
		false, false, false, false, false,
		true,  true,  true,  true,  true,
	};
}
