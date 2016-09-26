package osh.core.oc;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.GAConfiguration;
import osh.core.interfaces.IOSHOC;
import osh.esc.OCEnergySimulationCore;

/**
 * Superclass for the global controller unit
 *
 * @author Florian Allerding
 */
public abstract class GlobalController extends Controller {

	private GlobalOCUnit assignedOCUnit;
	
	protected OSHParameterCollection configurationParameters;
	protected GAConfiguration gaConfiguration;
	protected OCEnergySimulationCore ocESC;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public GlobalController(IOSHOC controllerbox, OSHParameterCollection configurationParameters, 
			GAConfiguration gaConfiguration, OCEnergySimulationCore ocESC) {
		super(controllerbox);
		this.configurationParameters = configurationParameters;
		this.gaConfiguration = gaConfiguration;
		this.ocESC = ocESC;
	}

	
	protected void assignControllerBox(GlobalOCUnit assignedOCUnit){
		this.assignedOCUnit = assignedOCUnit;
	}
	
	/**
	 * get the local o/c-unit to which thing controller belongs...
	 */
	public final GlobalOCUnit getAssignedOCUnit() {
		return assignedOCUnit;
	}
	
	/**
	 * get a local controller unit from a specific local o/c-unit
	 */
	public LocalController getLocalController(UUID deviceID) {
		LocalOCUnit _localOC = assignedOCUnit.getLocalUnits().get(deviceID);
		
		if( _localOC != null )
			return _localOC.localController;
		else
			return null;
	}
	
	/**
	 * return the according global observer unit
	 */
	public GlobalObserver getGlobalObserver(){
		return this.assignedOCUnit.getObserver();
	}
	
}
