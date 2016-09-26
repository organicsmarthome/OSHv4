package constructsimulation.generation.device;

import java.util.UUID;

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
public class CreateBaseloadDevice {

	public static AssignedDevice createBaseloadDevice(
			DeviceTypes deviceType, 
			DeviceClassification classification,
			UUID deviceId, 
			
			String driverClassName,
			String localObserverClass,
			boolean isControllable,
			String localControllerClass,
			
			ScreenplayType screenplayType,
			
			int yearlyElectricityConsumption,
			String h0Filename,
			double baseloadCosPhi,
			boolean baseloadIsInductive,
			String usedcommodities){
		
		AssignedDevice _assdev = CreateDevice.createDevice(
				deviceType, 
				classification, 
				deviceId, 
				driverClassName, 
				localObserverClass, 
				isControllable, 
				localControllerClass);
		
		// ### PARAMERTERS ###
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"screenplaytype", 
					"String", 
					"" + screenplayType);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"h0filename", 
					"String", 
					h0Filename);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"baseloadyearlyconsumption", 
					"String", 
					"" + yearlyElectricityConsumption);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"baseloadcosphi", 
					"String", 
					"" + baseloadCosPhi);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"baseloadisinductive", 
					"String", 
					"" + baseloadIsInductive);
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
