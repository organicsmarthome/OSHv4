package constructsimulation.generation.device.chp;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import constructsimulation.generation.device.CreateDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import constructsimulation.generation.utility.AddAssignedDevice;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class CreateDachsDevice {
	
	public static AssignedDevice createDachsDevice(
			UUID uuid,
			String driverClassName,
			String localObserverClass,
			String localControllerClass,
			Map<String, String> dachsParams
			) {
		
		AssignedDevice _assdev = CreateDevice.createDevice(
				DeviceTypes.CHPPLANT, 
				DeviceClassification.CHPPLANT, 
				uuid, 
				driverClassName, 
				localObserverClass, 
				true, 
				localControllerClass);
		if (!dachsParams.containsKey("compressionType"))
			dachsParams.put("compressionType", AddAssignedDevice.compressionType.toString());
		if (!dachsParams.containsKey("compressionValue"))
			dachsParams.put("compressionValue", String.valueOf(AddAssignedDevice.compressionValue));
		
		// ### PARAMETERS ###
		
		for(Entry<String, String> en : dachsParams.entrySet()) {
			ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
					en.getKey(), 
					"String", 
					en.getValue());
			_assdev.getDriverParameters().add(cp);
		}		
		return _assdev;
	}

}
