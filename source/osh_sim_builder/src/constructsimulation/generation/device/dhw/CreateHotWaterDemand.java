package constructsimulation.generation.device.dhw;

import java.util.UUID;

import constructsimulation.generation.device.CreateDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class CreateHotWaterDemand {
	

	public static AssignedDevice createHotWaterTank(
			DeviceTypes deviceType,
			UUID uuid,
			String driverClassName,
			String localObserverClass,
			String localControllerClass,
			ScreenplayType screenplayType,
			String sourcefile,
			String usedcommodities) {
		
		AssignedDevice _assdev = CreateDevice.createDevice(
				deviceType, 
				DeviceClassification.HVAC, 
				uuid, 
				driverClassName, 
				localObserverClass, 
				false, 
				localControllerClass);
		
		// ### PARAMETERS ###
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"screenplaytype", 
					"String", 
					"" + screenplayType);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"sourcefile", 
					"String", 
					"" + sourcefile);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"usedcommodities", 
					"String", 
					usedcommodities);
			_assdev.getDriverParameters().add(cp);
		}		
		return _assdev;
	}
}
