package osh.driver.simulation.batterylogic;

import java.util.List;

import osh.driver.simulation.batterystorage.BatteryStorage;
import osh.driver.simulation.inverter.InverterModel;

public abstract class IBatteryControlLogic {
	
	public abstract List<Integer> calculateChargePowers(
			int batteryMaxChargePower,
			int batteryMinChargePower,
			int batteryMaxDischargePower,
			int batteryMinDischargePower,
			List<Integer> listOfControls,
			Long referenceTime,
			Long now);
	
	public abstract void doStupidBMS(
			int currentPowerAtGridConnection, 
			BatteryStorage batteryModel,  
			InverterModel inverterModel,
			int stepSize,
			int OptimizedmaxChargePower,
			int OptimizedminChargePower,
			int OptimizedmaxDisChargePower,
			int OptimizedminDisChargePower);
	
	
	// ### helper methods ###
		protected static int calcMaxBatteryChargePower( double batteryMaxChargePower,  InverterModel inverterModel ){
			int inverterMaxChargePower = inverterModel.getMaxChargePower();
//			int batteryMaxChargePower = batteryModel.getMaxChargePower();
			int maxChargePower = (int) (Math.min(inverterMaxChargePower, batteryMaxChargePower));		
			return maxChargePower;
		}
		
		protected static int calcMinBatteryChargePower( double batteryMinChargePower,  InverterModel inverterModel  ){
			int inverterMinChargePower = inverterModel.getMinChargePower();
//			int batteryMinChargePower = batteryModel.getMinChargePower();
			int minChargePower = (int) (Math.max(inverterMinChargePower, batteryMinChargePower));	
			return minChargePower;
		}
		
		protected static int calcMaxDischargePower( double batteryMaxDischargePower,  InverterModel inverterModel ){
			int inverterMaxDischargePower = inverterModel.getMaxDischargePower();
//			int batteryMaxDischargePower = batteryModel.getMaxDischargePower();
			int maxDischargePower = (int) (Math.min(inverterMaxDischargePower, batteryMaxDischargePower));		
			return maxDischargePower;
		}
		
		protected static int calcMinDischargePower( double batteryMinDischargePower,  InverterModel inverterModel ){
			int inverterMinDishargePower = inverterModel.getMinDischargePower();
//			int batteryMinDischargePower = batteryModel.getMinDischargePower();
			int minChargePower = (int) (Math.max(inverterMinDishargePower, batteryMinDischargePower));	
			return minChargePower;
		}
		
}