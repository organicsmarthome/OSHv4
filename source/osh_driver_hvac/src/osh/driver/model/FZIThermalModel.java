package osh.driver.model;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class FZIThermalModel extends BuildingThermalModel {

	// FIXME: add heating demand of HOLL
	@Override
	public double calculateHeatingDemand(double outdoorTemperature) {
		return 0;
	}

	@Override
	public double calculateCoolingDemand(double outdoorTemperature) {
		return Math.max(0, ((0.4415 * outdoorTemperature) - 9.6614) * 1000);
	}

}
