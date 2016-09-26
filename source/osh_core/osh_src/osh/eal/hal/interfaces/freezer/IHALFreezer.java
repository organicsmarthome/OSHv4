package osh.eal.hal.interfaces.freezer;

/**
 * 
 * @author Ingo Mauser
 *
 */
public interface IHALFreezer {
	
	public double getSetTemperature();
	
	public double getMinTemperature();
	
	public double getMaxTemperature();

	public double getCurrentTemperature();

	public int getControlSignal();

	public int getControlSignalCorrectionValue();
	
	public long getLastControlSignalCorrectionValueChange();
	
	public int getTicksSinceLastCooling();

	public boolean getCoolingOn();

	public boolean getFanOn();
	
	public boolean getSuperFrostOn();
	
	public int getSuperFrostTicks();

	public boolean getNoFrostOn();
}
