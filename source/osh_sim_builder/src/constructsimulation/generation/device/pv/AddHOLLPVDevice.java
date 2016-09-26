package constructsimulation.generation.device.pv;

import java.util.UUID;

import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

public class AddHOLLPVDevice {

	public static void addRealPvDevice(
			EALConfiguration ealConfiguration,
			
			DeviceTypes deviceType,
			DeviceClassification classification,
			UUID deviceId, 
			
			String driverClassName,
			String localObserverClass,
			boolean isControllable,
			String localControllerClass,
			
			ScreenplayType screenplayType,
			
			String nominalPower,
			String complexpowermax,
			String cosphimax,
			String usedcommodities) {
		
		
		AssignedDevice device = CreateHOLLPvDevice.createRealPvDevice(
				deviceType, 
				classification, 
				deviceId, 
				driverClassName, 
				localObserverClass, 
				isControllable, 
				localControllerClass, 
				screenplayType,
				nominalPower,
				complexpowermax,
				cosphimax,
				usedcommodities);
		
		ealConfiguration.getAssignedDevices().add(device);
		
	}
	
}
