package osh.hal.interfaces.chp;

/**
 * 
 * @author Ingo Mauser
 *
 */
public interface IHALChpDetails {
	public boolean isRunning();
	
	public boolean isHeatingRequest();
	public boolean isElectricityRequest();
	
	public int getMinRuntimeRemaining();
}
