package constructsimulation.generation.device.batterystorage;

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
public class CreateBatteryStorageDevice {
	
	public static AssignedDevice createBatteryStorageDevice(
			UUID uuid,
			String driverClassName,
			String localObserverClass,
			boolean isControllable,
			String localControllerClass,
			
			ScreenplayType screenplayType,
			
			int minChargingState,
			int maxChargingState,
			int minDischargePower,
			int maxDischargePower,
			int minChargePower,
			int maxChargePower,
			int minInverterPower,
			int maxInverterPower,
			String usedcommodities){
		
		AssignedDevice _assdev = CreateDevice.createDevice(
				DeviceTypes.BATTERYSTORAGE, 
				DeviceClassification.BATTERYSTORAGE, 
				uuid, 
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
					"minChargingState", 
					"String", 
					"" + minChargingState);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"maxChargingState", 
					"String", 
					"" + maxChargingState);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"minDischargingPower", 
					"String", 
					"" + minDischargePower);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"maxDischargingPower", 
					"String", 
					"" + maxDischargePower);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"minChargingPower", 
					"String", 
					"" + minChargePower);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"maxChargingPower", 
					"String", 
					"" + maxChargePower);
			_assdev.getDriverParameters().add(cp);
		}
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"minInverterPower", 
					"String", 
					"" + minInverterPower);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"maxInverterPower", 
					"String", 
					"" + maxInverterPower);
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
