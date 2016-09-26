package constructsimulation.generation.device.appliance;

import java.util.UUID;

import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class AddGenericApplianceDevice {

	public static void addGenericApplianceDevice(
			EALConfiguration ealConfiguration,
			
			DeviceTypes deviceType,
			DeviceClassification classification,
			UUID deviceId, 
			
			String driverClassName,
			String localObserverClass,
			boolean isControllable,
			String localControllerClass, 
			
			ScreenplayType screenplayType,
			String deviceMax1stDof,
			String device2ndDof,
			double averageyearlyruns,
			String h0Filename,
			String h0Classname,
			String probabilityfilename,
			String configurationShares,
			String profileSource,	
			String usedcommodities) {
		
		AssignedDevice genericAppliance = CreateGenericAppliance.createGenericApplianceDevice(
				deviceType, 
				classification, deviceId, 
				driverClassName, 
				localObserverClass, 
				isControllable, 
				localControllerClass, 
				screenplayType,
				deviceMax1stDof,
				device2ndDof,
				averageyearlyruns,
				h0Filename,
				h0Classname,
				probabilityfilename,
				configurationShares,
				profileSource,	
				usedcommodities);
		ealConfiguration.getAssignedDevices().add(genericAppliance);
		
	}
}
