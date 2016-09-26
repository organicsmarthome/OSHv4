package constructsimulation.datatypes;

/**
 * 
 * @author Ingo Mauser, Till Schuberth
 *
 */
public class CALConfigurationWrapper {
	
	public boolean showGui;
	public String guiComDriverClassName;
	public String guiComManagerClassName;
	

	
	
	public CALConfigurationWrapper(boolean showGui, String guiComDriverClassName, String guiComManagerClassName) {
		this.showGui = showGui;
		this.guiComDriverClassName = guiComDriverClassName;
		this.guiComManagerClassName = guiComManagerClassName;
	}
}
