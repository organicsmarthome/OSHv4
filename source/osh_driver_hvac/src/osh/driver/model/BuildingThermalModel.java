package osh.driver.model;

/**
 * 
 * @author Ingo Mauser
 *
 */
public abstract class BuildingThermalModel {

	public BuildingThermalModel() {
		super();
	}

	/**
	 * @param outdoorTemperature [degree Celsius]
	 * @return demand in [W]
	 */
	public abstract double calculateHeatingDemand(double outdoorTemperature);
	
	/**
	 * @param outdoorTemperature [degree Celsius]
	 * @return demand in [W]
	 */
	public abstract double calculateCoolingDemand(double outdoorTemperature);
	
}