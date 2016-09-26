package constructsimulation.generation.device.dhw;

import java.util.UUID;

import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class AddHotWaterDemand {

	public static void addWaterHeatingDevice(
			EALConfiguration ealConfiguration,
			
			DeviceTypes deviceType,
			UUID deviceId, 
			
			String driverClassName,
			String localObserverClass,
			String localControllerClass,
			
			ScreenplayType screenplayType,
			
			String sourcefile,
			String usedcommodities) {
		
		
		AssignedDevice device = CreateHotWaterDemand.createHotWaterTank(
				deviceType,
				deviceId,
				driverClassName, 
				localObserverClass, 
				localControllerClass,
				screenplayType,
				sourcefile,
				usedcommodities);
		
		ealConfiguration.getAssignedDevices().add(device);
		
	}
	
}
