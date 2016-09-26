package constructsimulation.generation.device.batterystorage;

import java.util.UUID;

import osh.configuration.eal.EALConfiguration;
import osh.simulation.screenplay.ScreenplayType;

/**
 * 
 * @author mauser
 *
 */
public class AddBatteryStorageDevice {

	public static void addBatteryStorageDevice(
			EALConfiguration ealConfiguration,
			UUID batteryDeviceId,
			String batteryDeviceDriver,
			String batteryDeviceObserver,
			boolean isControllable,
			String batteryDeviceController,
			ScreenplayType screenplayType,
			int minChargingState,
			int maxChargingState,
			int minDischargePower,
			int maxDischargePower,
			int minChargePower,
			int maxChargePower,
			int minInverterPower,
			int maxInverterPower,
			String usedcommodities) {
		
		ealConfiguration.getAssignedDevices().add(
				CreateBatteryStorageDevice.createBatteryStorageDevice(
						batteryDeviceId,
						batteryDeviceDriver, 
						batteryDeviceObserver,
						isControllable,
						batteryDeviceController,
						screenplayType,
						minChargingState,
						maxChargingState,
						minDischargePower,
						maxDischargePower,
						minChargePower,
						maxChargePower,
						minInverterPower,
						maxInverterPower,
						usedcommodities
		));
	}
	
	
}
