package constructsimulation.generation.device.appliance;

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
public class CreateMieleDevice {

	public static AssignedDevice createMieleDevice(
			DeviceTypes deviceType,
			DeviceClassification classification,
			UUID deviceId, 
			
			String driverClassName,
			String localObserverClass,
			boolean isControllable,
			String localControllerClass, 
			
			String profileSource,	
			
			ScreenplayType screenplayType,
			Integer epsOptimizationObjective,
			
			Double averageYearlyRuns,
			String h0Filename,
			String deviceMax1stDof,
			String device2ndDof){
		
		AssignedDevice _assdev = CreateDevice.createDevice(
				deviceType, 
				classification, 
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
					"profilesource", 
					"String", 
					profileSource);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"epsoptimizationobjective", 
					"String", 
					"" + epsOptimizationObjective);
			_assdev.getDriverParameters().add(cp);
		}
		
		{
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					"randomdof", 
					"String", 
					"false");
			_assdev.getDriverParameters().add(cp);
		}
		
		
		if (device2ndDof != null) {
			{
				ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
						"device2nddof", 
						"String", 
						"" + device2ndDof);
				_assdev.getDriverParameters().add(cp);
			}
		}
		
		// dynamic screenplay
		if (screenplayType == ScreenplayType.DYNAMIC) {
			if (averageYearlyRuns != null) {
				{
					ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
							"averageyearlyruns", 
							"String", 
							"" + averageYearlyRuns);
					_assdev.getDriverParameters().add(cp);
				}
			}
			else {
				System.out.println("[ERROR] deviceId=" + deviceId.toString() + " : averageyearlyruns=null");
			}
			if (h0Filename != null) {
				{
					ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
							"h0filename", 
							"String", 
							h0Filename);
					_assdev.getDriverParameters().add(cp);
				}
			}
			else {
				System.out.println("[ERROR] deviceId=" + deviceId.toString() + " : h0Filename=null");
			}
			if (deviceMax1stDof != null) {
				{
					ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
							"devicemax1stdof", 
							"String", 
							deviceMax1stDof);
					_assdev.getDriverParameters().add(cp);
				}
			}
			else if (deviceMax1stDof == null) {
				System.out.println("[ERROR] deviceId=" + deviceId.toString() + " : devicemax1stdof=null");
			}
		}
		
		return _assdev;
	}
	
}
