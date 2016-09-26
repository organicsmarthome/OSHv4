package constructsimulation.generation.utility;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import constructsimulation.generation.device.CreateDevice;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.power.LoadProfileCompressionTypes;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class AddAssignedDevice {
	
	public static LoadProfileCompressionTypes compressionType;
	public static int compressionValue;
	
	public static void addAssignedDevice(EALConfiguration ealConfiguration,			
			UUID deviceUUID,
			
			String driverClassName,
			String localObserverClass,
			String localControllerClass,
			boolean isControllable,
			
			DeviceTypes deviceType,
			DeviceClassification deviceClassification,
			
			Map<String, String> params,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		
		params.put("compressionType", compressionType.toString());
		params.put("compressionValue", String.valueOf(compressionValue));
		
		AssignedDevice asDev = CreateDevice.createDevice(
				deviceType, 
				deviceClassification, 
				deviceUUID, 
				driverClassName, 
				localObserverClass, 
				isControllable, 
				localControllerClass
				);
		
		for (Entry<String, String> en : params.entrySet()) {
			ConfigurationParameter _param = new ConfigurationParameter();
			_param.setParameterName(en.getKey());
			_param.setParameterType("String");
			_param.setParameterValue(en.getValue());
			asDev.getDriverParameters().add(_param);
		}
		
		ealConfiguration.getAssignedDevices().add(asDev);
	}

	public static void addAssignedDevice (
			EALConfiguration ealConfiguration,			
			UUID deviceUUID,
			
			String driverClassName,
			String localObserverClass,
			String localControllerClass,
			boolean isControllable,
			
			DeviceTypes deviceType,
			DeviceClassification deviceClassification,
			
			Map<String, String> params) {
		
			addAssignedDevice(ealConfiguration, 
					deviceUUID, 
					driverClassName, 
					localObserverClass, 
					localControllerClass, 
					isControllable, 
					deviceType, 
					deviceClassification, 
					params, 
					AddAssignedDevice.compressionType, 
					AddAssignedDevice.compressionValue);
		}
}
