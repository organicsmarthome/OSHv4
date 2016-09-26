package osh.driver.thermal;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class SimpleColdWaterTank extends SimpleWaterTank {

	private static final long serialVersionUID = 838005750616214765L;

	
	/**
	 * CONSTRUCTOR
	 */
	public SimpleColdWaterTank(
			double tankCapacity,
			Double tankDiameter,
			Double startTemperatur, 
			Double ambientTemperature) {
		super(
				// FZI HoLL (2 tanks), empirical
				8 * -1.0 * (12 + 5.93 * Math.pow(tankCapacity, 0.4)),
				tankCapacity, 
				tankDiameter, 
				startTemperatur, 
				ambientTemperature);
	}

}
