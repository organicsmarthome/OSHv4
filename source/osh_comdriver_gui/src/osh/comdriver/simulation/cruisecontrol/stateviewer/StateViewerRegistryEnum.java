package osh.comdriver.simulation.cruisecontrol.stateviewer;

/**
 * 
 * @author Till Schuberth
 *
 */
public enum StateViewerRegistryEnum {
	
	OC("OC Registry"), DRIVER("driver registry");
	
	private String str;
	
	private StateViewerRegistryEnum(String str) {
		this.str = str;
	}
	
	
	/**
	 * CONSTRUCTOR
	 * @param str
	 * @return
	 */
	public static StateViewerRegistryEnum findByString(String str) {
		if (str == null) return null;
		
		for (StateViewerRegistryEnum e : StateViewerRegistryEnum.values()) {
			if (e.str.equals(str)) return e;
		}
		
		return null;
	}

	@Override
	public String toString() {
		return str;
	}
	
}
