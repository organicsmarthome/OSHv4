package constructsimulation.generation.device.appliance;

import java.util.UUID;

import constructsimulation.generation.device.CreateDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

public class AddLiebherrFreezer {
	
	public static void addLiebherrFreezer(
			EALConfiguration ealConfiguration,
			
			UUID deviceId,
			ScreenplayType screenplayType,
			
			String driverClassName,
			String localObserverClass,
			boolean isControllable,
			String localControllerClass,
			String comPort) {
	
	
		AssignedDevice _assdev = CreateDevice.createDevice(
				DeviceTypes.FREEZER, 
				DeviceClassification.APPLIANCE, 
				deviceId, 
				driverClassName, 
				localObserverClass, 
				isControllable, 
				localControllerClass);
		
		// add parameters for driver
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"screenplaytype", 
					"String", 
					"" + screenplayType);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"comport", 
					"String", 
					"" + comPort);
			_assdev.getDriverParameters().add(cp);
		}
		
		// other parameters...
		
		ealConfiguration.getAssignedDevices().add(_assdev);
	
	}

}
