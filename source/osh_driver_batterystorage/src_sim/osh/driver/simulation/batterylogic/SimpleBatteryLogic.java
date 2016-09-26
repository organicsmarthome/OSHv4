package osh.driver.simulation.batterylogic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import osh.driver.simulation.batterystorage.BatteryStorage;
import osh.driver.simulation.inverter.InverterModel;

/**
 * 
 * @author Jan Mueller, Matthias Maerz
 *
 */

public class SimpleBatteryLogic extends IBatteryControlLogic implements Serializable {
	
	private static final long serialVersionUID = -4660151032508909284L;
	
	@Override
	public List<Integer> calculateChargePowers(
			int batteryMaxChargePower,
			int batteryMinChargePower,
			int batteryMaxDischargePower,
			int batteryMinDischargePower,
			List<Integer> listOfControls,
			Long referenceTime,
			Long now) {
		
		ArrayList<Integer> listOfResutls = new ArrayList<Integer>();
		
		listOfResutls.add(batteryMaxChargePower);
		listOfResutls.add(batteryMinChargePower);
		listOfResutls.add(batteryMaxDischargePower);
		listOfResutls.add(batteryMinDischargePower);
			
		return listOfResutls;

	}	

	@Override
	public void doStupidBMS(
			int currentPowerAtGridConnection, 
			BatteryStorage batteryModel, 
			InverterModel inverterModel,
			int stepSize,
			int maxChargePower,
			int minChargePower,
			int maxDisChargePower,
			int minDisChargePower) {
		
		int oldPower = inverterModel.getActivePower();

		// change SOC with old power
		
		if (inverterModel.isOn()){
			int batteryPower = inverterModel.calcBatteryChargePower(-oldPower);
			batteryModel.changeSOCOverTime(batteryPower, stepSize);
		}
		
		// reduce SOC with standing loss
		batteryModel.reduceByStandingLoss(1);
		
		// CALCULATE NEW POWER
		int newActivePowerOutput = 0;
		
		// get available power
		//TODO fix
		int currentAvailablePower = currentPowerAtGridConnection - oldPower;
//		int currentAvailablePower = currentPowerAtGridConnection ;

		//TODO reactive power logic
		
		
		// ### Charge ### (i.e., net feed-in, without battery storage)
		if (currentAvailablePower < 0 ) {
			// !!!!!!!!!! chargePower is positive!
			// get charging power to battery for this power (efficiency...)
			double batteryChargePower = inverterModel.calcBatteryChargePower(currentAvailablePower);
			int minBatteryChargePower = calcMinBatteryChargePower(batteryModel.getMinChargePower(), inverterModel);
			int maxBatteryChargePower = calcMaxBatteryChargePower(batteryModel.getMaxChargePower(), inverterModel);
			
			//check for minimal charge power
			if (batteryChargePower < minBatteryChargePower) {		
				batteryChargePower = 0;
				newActivePowerOutput = inverterModel.getStandbyPower();
				inverterModel.switchOff();
			}
			else {
				inverterModel.switchOn();
				//limit maximal charge power
				batteryChargePower = Math.min(maxBatteryChargePower, batteryChargePower);
				newActivePowerOutput = inverterModel.calcActivePowerOutputChargingState(batteryChargePower);
				
				// check whether battery would be overcharged...
				double nextSOC =  batteryModel.getStateOfCharge() + batteryChargePower * stepSize;
				int maxSOC = batteryModel.getMaxChargeState();
				double soc = batteryModel.getStateOfCharge();
				if (nextSOC > maxSOC) {
					batteryChargePower = (maxSOC- soc) / stepSize;

					//check for minimal charge power
					if ((batteryChargePower < minBatteryChargePower) || batteryChargePower<0) {
						batteryChargePower = 0;
						newActivePowerOutput = inverterModel.getStandbyPower();
						inverterModel.switchOff();
					}
					else {
						newActivePowerOutput = inverterModel.calcActivePowerOutputChargingState(batteryChargePower);
					}
				}
			}
		}
		// ### Discharge ### (i.e., net consumption without battery storage)
		else if (currentAvailablePower > 0) {
			// !!!!!!!!!!!!!!! dischargePower is negative!
			double batteryDischargePower = inverterModel.calcBatteryChargePower(currentAvailablePower);
			int minBatteryDischargePower = calcMinDischargePower(batteryModel.getMinDischargePower(), inverterModel);
			int maxBatteryDischargePower = calcMaxDischargePower(batteryModel.getMaxDischargePower(), inverterModel);
			
			// below minimal discharge
			if (batteryDischargePower > maxBatteryDischargePower) {
				batteryDischargePower = 0;
				newActivePowerOutput = inverterModel.getStandbyPower();
				inverterModel.switchOff();
			}
			else {
				inverterModel.switchOn();
				//limit maximal discharge power
				batteryDischargePower = Math.max(minBatteryDischargePower, batteryDischargePower);
				newActivePowerOutput = inverterModel.calcActivePowerOutputChargingState(batteryDischargePower);
				
				// check whether battery would be undercharged...
				double nextSOC = batteryModel.getStateOfCharge() + batteryDischargePower * stepSize;
				int minSOC = batteryModel.getMinChargeState();
				double soc = batteryModel.getStateOfCharge();
				if (nextSOC < minSOC){
					batteryDischargePower = (minSOC-soc) / stepSize;
					// below minimal discharge
					if (batteryDischargePower > maxBatteryDischargePower || batteryDischargePower >0) {
						batteryDischargePower = 0;
						newActivePowerOutput = inverterModel.getStandbyPower();
						inverterModel.switchOff();
					}
					else {
						newActivePowerOutput = inverterModel.calcActivePowerOutputChargingState(batteryDischargePower);
					}
				}
			}
		}
		else {
			// currentAvailablePower == 0
			newActivePowerOutput = inverterModel.getStandbyPower();
		}
		inverterModel.setActivePower(newActivePowerOutput);	
	}

	
}
