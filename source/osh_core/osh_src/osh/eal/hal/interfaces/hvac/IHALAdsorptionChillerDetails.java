package osh.eal.hal.interfaces.hvac;

/**
 * 
 * @author Ingo Mauser
 *
 */
public interface IHALAdsorptionChillerDetails {
	public boolean isRunning();
	public int getMinRuntimeRemaining();
}
