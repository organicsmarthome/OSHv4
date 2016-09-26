package osh.hal.interfaces.chp;

import osh.driver.chp.ChpOperationMode;

/**
 * 
 * @author Ingo Mauser
 *
 */
public interface IHALChpStaticDetails {
	public int getTypicalActivePower();
	public int getTypicalReactivePower();
	public int getTypicalGasPower();
	public int getTypicalThermalPower();
	
	public int getMinRuntime();
	public ChpOperationMode getOperationMode();
}
