package osh.core.oc;

import java.util.UUID;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;

/**
 * container class for the local observer and controller
 * 
 * @author Florian Allerding
 */
public class LocalOCUnit extends OCUnit {

	public LocalObserver localObserver;
	public LocalController localController;
	private DeviceTypes deviceType;
	private DeviceClassification deviceClassification;
	
	
	public LocalOCUnit(
			IOSHOC controllerbox, 
			UUID deviceID, 
			LocalObserver localObserver,
			LocalController localController){
		super(deviceID, controllerbox);
		
		//create local controller/observer  and assign to the OCUnit
		this.localObserver = localObserver;
		this.localController = localController;
		
		if (localObserver!=null){
			this.localObserver.assignLocalOCUnit(this);
		}
		if (localController!=null){
			this.localController.assignLocalOCUnit(this);
		}
	}

	
	public void setDeviceClassification(DeviceClassification deviceClassification) {
		this.deviceClassification = deviceClassification;
	}


	public DeviceClassification getDeviceClassification() {
		return deviceClassification;
	}


	public void setDeviceType(DeviceTypes deviceType) {
		this.deviceType = deviceType;
	}


	public DeviceTypes getDeviceType() {
		return deviceType;
	}

	@Override
	public String toString() {
		return "LocalUnit " + getUnitID();
	}
}
