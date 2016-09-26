package osh.core.oc;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;

/**
 * container class for the virtual O/C-unit. it represents the central controlling unit
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class GlobalOCUnit extends OCUnit {

	private GlobalObserver observer;
	private GlobalController controller;
	private HashMap<UUID, LocalOCUnit> localUnits;
	

	/**
	 * CONSTRUCTOR
	 */
	public GlobalOCUnit(
			UUID unitID, 
			IOSHOC controllerbox, 
			GlobalObserver globalObserver, 
			GlobalController globalController){
		super(unitID, controllerbox);
		
		this.localUnits =  new HashMap<UUID, LocalOCUnit>();
		this.observer = globalObserver;
		this.controller = globalController;
		
		this.observer.assignControllerBox(this);
		this.controller.assignControllerBox(this);
	}
	
	
	public GlobalObserver getObserver() {
		return observer;
	}

	public Controller getController() {
		return controller;
	}
	
	public void registerLocalUnit(LocalOCUnit localunit) throws OSHException{
		
		// put and check if it already exists
		LocalOCUnit old;
		if ((old = localUnits.put(localunit.getUnitID(), localunit)) != null) {
			throw new OSHException("UUID " + localunit.getUnitID() + " already registered!" + old.toString());
		}
	}
	
	protected HashMap<UUID, LocalOCUnit> getLocalUnits() {
		return localUnits;
	}

	public UUID[] getLocalUnitsUUIDs() {
		UUID[] uuids = new UUID[localUnits.size()];
		int i = 0;
		for (Entry<UUID, LocalOCUnit> localUnit : localUnits.entrySet()) {
			uuids[i] = localUnit.getKey();
			i++;
		}
		
		return uuids;
	}
	
}