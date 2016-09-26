package constructsimulation.generation.device;

import java.util.UUID;

import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.AssignedLocalOCUnit;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class CreateDevice {

	public static AssignedDevice createDevice(
			DeviceTypes deviceType,
			DeviceClassification classification,
			UUID deviceId, 
			
			String driverClassName,
			String localObserverClass,
			boolean isControllable,
			String localControllerClass) {
		
		AssignedDevice _assdev = new AssignedDevice();
		
		_assdev.setDeviceType(deviceType);
		_assdev.setDeviceClassification(classification);
		_assdev.setDeviceID(deviceId.toString());
		_assdev.setDeviceDescription("");
		
		_assdev.setObservable(true); //always observable
		_assdev.setControllable(isControllable);
		_assdev.setDriverClassName(driverClassName);
		
		AssignedLocalOCUnit _lunit = new AssignedLocalOCUnit();
		_lunit.setLocalControllerClassName(localControllerClass);
		_lunit.setLocalObserverClassName(localObserverClass);
		_assdev.setAssignedLocalOCUnit(_lunit);
		
		return _assdev;
	}
	
}
