package constructsimulation.generation.device.chp;

import java.util.Map;
import java.util.UUID;

import osh.configuration.eal.EALConfiguration;


/**
 * 
 * @author mauser
 *
 */
public class AddDachsDevice {

	public static void addDachsDevice(
			EALConfiguration ealConfiguration,
			UUID dachsDeviceId,
			String dachsDeviceDriver,
			String dachsDeviceObserver,
			String dachsDeviceController,
			Map<String, String> dachsParams) {		
		
		ealConfiguration.getAssignedDevices().add(				
				CreateDachsDevice.createDachsDevice(
						dachsDeviceId,
						dachsDeviceDriver, 
						dachsDeviceObserver, 
						dachsDeviceController,
						dachsParams
		));
	}
	
	
}
