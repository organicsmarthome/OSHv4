package osh.datatypes.cruisecontrol;

import java.util.UUID;

import osh.datatypes.cruisecontrol.OptimizedDataStorage.EqualData;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;


/**
 * 
 * @author Jan Mueller
 *
 */
public class GUIBatteryStorageStateExchange 
				extends BatteryStorageOCSX 
				implements EqualData<BatteryStorageOCSX> {

	private static final long serialVersionUID = 2308641394864672076L;


	public GUIBatteryStorageStateExchange(
			UUID sender, 
			long timestamp,
			double currentStateOfCharge, 
			double minStateOfCharge, 
			double maxStateOfCharge,
			UUID batteryId) {
		super(sender, timestamp, currentStateOfCharge, minStateOfCharge, maxStateOfCharge, batteryId);
	}

	
	public boolean equalData(GUIBatteryStorageStateExchange o) {
		return super.equalData(o);
	}
}
