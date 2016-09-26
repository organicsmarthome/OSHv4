package constructsimulation.generation.utility;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import constructsimulation.generation.device.CreateComDevice;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.cal.CALConfiguration;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.ConfigurationParameter;
import osh.simulation.screenplay.ScreenplayType;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class AddAssignedComDevice {
	
	public static void addAssignedComDevice (
			CALConfiguration calConfiguration,
			
			UUID deviceUUID,
			
			ComDeviceTypes deviceType,
			
			String comDriverClass,
			String comManagerClass,
			
			ScreenplayType screenplayType,
			
			Map<String, String> params) {
		
			AssignedComDevice plsComDevice = CreateComDevice.createComDevice(
				deviceType, 
				deviceUUID, 
				comDriverClass, 
				comManagerClass);
			
			for (Entry<String, String> en : params.entrySet()) {
				ConfigurationParameter _param = new ConfigurationParameter();
				_param.setParameterName(en.getKey());
				_param.setParameterType("String");
				_param.setParameterValue(en.getValue());
				plsComDevice.getComDriverParameters().add(_param);
			}
			
			calConfiguration.getAssignedComDevices().add(plsComDevice);
		}		
}
