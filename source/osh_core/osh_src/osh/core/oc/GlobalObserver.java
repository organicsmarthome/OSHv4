package osh.core.oc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSHOC;

/**
 * represents the observer of the global o/c-unit
 * 
 * @author Florian Allerding
 */
public abstract class GlobalObserver extends Observer {
	
	private GlobalOCUnit assignedOCUnit;
	
	protected OSHParameterCollection configurationParameters;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public GlobalObserver(IOSHOC controllerbox, OSHParameterCollection configurationParameters){
		super(controllerbox);
		this.configurationParameters = configurationParameters;
	}
	
	/**
	 * assign the controllerbox o/c-unit
	 */
	protected void assignControllerBox(GlobalOCUnit assignedOCUnit){
		this.assignedOCUnit = assignedOCUnit;
	}

	/**
	 * gets a local observer based on it's id
	 * @param deviceID
	 * @return
	 */
	public LocalObserver getLocalObserver(UUID deviceID){
		
		LocalOCUnit _localOC = assignedOCUnit.getLocalUnits().get(deviceID);
		if( _localOC != null )
			return _localOC.localObserver;
		else
			return null;
	}	
	
	/**
	 * Returns all assigned local Observer
	 * @return
	 */
	public ArrayList<LocalObserver> getAllLocalObservers(){
		
		ArrayList<LocalObserver> _localObserver = new ArrayList<LocalObserver>();
		Collection<LocalOCUnit> _ocCollection = assignedOCUnit.getLocalUnits().values();
		ArrayList<LocalOCUnit>  _localOCUnits = new ArrayList<LocalOCUnit>();
		_localOCUnits.addAll(_ocCollection);
		
		for (int i = 0; i < _localOCUnits.size(); i++){
			_localObserver.add(_localOCUnits.get(i).localObserver);
		}
		
		return _localObserver;
	}
	
	/**
	 * returns the list of all ids form the assignd devices/local units
	 * @return
	 */
	public ArrayList<UUID> getAssigndDeviceIDs(){
		ArrayList<UUID> _devIDs = new ArrayList<UUID>();
		
		for (int i = 0; i < _devIDs.size(); i++){
			_devIDs.add((UUID) assignedOCUnit.getLocalUnits().keySet().toArray()[i]);
		}
		return _devIDs;
	}
	
	
	public GlobalOCUnit getAssignedOCUnit() {
		return assignedOCUnit;
	}
	
}
