package constructsimulation.generation.device;

import java.util.UUID;

import osh.configuration.eal.AssignedBusDevice;
import osh.configuration.system.BusDeviceTypes;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class CreateBusDevice {

	public static AssignedBusDevice createBusDevice(
			BusDeviceTypes deviceType,
			UUID deviceId,
			String comDriverClassName,
			String comManagerClassName) {
		AssignedBusDevice busDevice = new AssignedBusDevice();
		busDevice.setBusDeviceID(deviceId.toString());
		busDevice.setBusDeviceType(deviceType);
		busDevice.setBusDriverClassName(comDriverClassName);
		busDevice.setBusManagerClassName(comManagerClassName);
		
		return busDevice;
	}
	
}
