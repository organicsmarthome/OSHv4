package osh.datatypes.hal.interfaces;

import java.util.HashMap;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public interface ITemperatureDetails {
	public double getTemperature();
	public HashMap<String, Double> getAuxiliaryTemperatures();
}
