package osh.driver.model;

/**
 * 
 * P(T_outdoor) = p1 * T_outdoor + p2<br>
 * -12 degree Celsius: 2050 W<br>
 * heating starts at about 18 degree Celsius
 * 
 * @author Sebastian Kochanneck, Ingo Mauser, Jan Mueller
 *
 */
public class ESHLThermalModel extends BuildingThermalModel {

	
	/**
	 * [W/K]
	 */
	private double p1 = -68.3333;
	
	/**
	 * [W]
	 */
	private double p2 = 1230.0;
	
	@Override
	public double calculateHeatingDemand(double outdoorTemperature) {
		return Math.max(0, p1 * outdoorTemperature + p2);
	}

	//FIXME: add cooling demand of ESHL
	@Override
	public double calculateCoolingDemand(double outdoorTemperature) {
		return 0;
	}
	
}
