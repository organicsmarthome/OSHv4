package constructsimulation.generation.device.pv;

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
public class CreatePvDevice {

	public static AssignedDevice createPvDevice(
			DeviceTypes deviceType,
			DeviceClassification classification,
			UUID deviceId, 
			
			String driverClassName,
			String localObserverClass,
			boolean isControllable,
			String localControllerClass, 
			
			String profileSource,
			
			ScreenplayType screenplayType,
			
			String nominalPower,
			String complexPowerMax,
			String cosPhiMax){
		
		AssignedDevice _assdev = CreateDevice.createDevice(
				deviceType, 
				classification, 
				deviceId, 
				driverClassName, 
				localObserverClass, 
				isControllable, 
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
					"profilesource", 
					"String", 
					profileSource);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"nominalpower", 
					"String", 
					nominalPower);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"complexpowermax", 
					"String", 
					complexPowerMax);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"cosphimax", 
					"String", 
					cosPhiMax);
			_assdev.getDriverParameters().add(cp);
		}
		
		return _assdev;
	}
	
}
