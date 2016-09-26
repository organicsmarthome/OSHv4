package osh.driver.thermal;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class SimpleWaterTank extends WaterTank {
	
	private static final long serialVersionUID = -754345613886808973L;

	/** specific heat capacity [J / (kg * K)] */
	private final double avgThermalCapacityOfWater = 4190.0;

	/** tank capacity [liters] */
	private double tankCapacity;
	
	/** average temperature [°C] */
	private double waterTemperature;
	
	/** ambient air temperature [°C] */
	private double ambientTemperature;
	
	/** correction factor with <br>
	 * dT_reference = waterTemperature - ambientTemperature = 40K<br>
	 * loss = -1.0 * (12 + 5.93 * Math.pow(tankCapacity, 0.4)) [W] */
	private double normalizedEnergyLoss;
	
	private double thermalCapacityOfTank;
	
	// currently unused	
	private double tankHeight;
	private double tankDiameter;
	@SuppressWarnings("unused")
	private double tankSurface;
	
	
	/**
	 * CONSTRCUTOR
	 * @param tankCapacity [liters]
	 * @param tankDiameter [m] (default: 0.5)
	 * @param startTemperatur [°C] (default: 50°C)
	 * @param ambientTemperature [°C] (default: 20°C)
	 */
	public SimpleWaterTank(
			double normalizedEnergyLoss,
			double tankCapacity, 
			Double tankDiameter, 
			Double startTemperatur, 
			Double ambientTemperature) {
		// currently relevant parameters
		
		if (tankDiameter == null) {
			tankDiameter = 0.5;
		}
		this.tankDiameter = tankDiameter;
		
		this.tankCapacity = tankCapacity;
		
		// variable energy loss (different insulations)
		this.normalizedEnergyLoss = normalizedEnergyLoss;
		
		if (startTemperatur == null) {
			startTemperatur = 50.0;
		}
		this.waterTemperature = startTemperatur;
		
		if (ambientTemperature == null) {
			ambientTemperature = 20.0;
		}
		this.ambientTemperature = ambientTemperature;
		
		// Geometry: cylinder
		// (currently not relevant)
		this.tankDiameter = tankDiameter;
		this.tankHeight = tankCapacity / 1000.0 / (Math.PI * (tankDiameter / 2.0) * (tankDiameter / 2.0));
		this.tankSurface = tankDiameter * Math.PI * this.tankHeight + 2 * 0.5 * tankDiameter * Math.PI;
		
		this.thermalCapacityOfTank = this.avgThermalCapacityOfWater * this.tankCapacity;
	}
	
	
	public void reduceByStandingHeatLoss(long seconds) {
		addEnergy(calcStandingHeatLoss(seconds));
	}
	
	/**
	 * standing loss (DE: Verlustleistung) respective standing gain (DE: Erwärmung)
	 * @param seconds
	 * @return [Ws]
	 */
	private double calcStandingHeatLoss(long seconds) {
		double lossCorrectionFactor = (waterTemperature - ambientTemperature) / 40;
		return normalizedEnergyLoss * lossCorrectionFactor * seconds; //[Ws]
	}
	
	/**
	 * positive value: add energy to water<br>
	 * negative value: remove energy from water
	 * @param power [W]
	 * @param seconds [s]
	 */
	public void addPowerOverTime(double power, long seconds, Double reflowTemperature, Double massFlow) {
		this.addEnergy(power * seconds);
	}
	
	/**
	 * positive value: add energy to water<br>
	 * negative value: remove energy from water
	 * @param energy [Ws] (positive value: add energy to water)
	 */
	public void addEnergy(double energy) {
		this.waterTemperature = this.waterTemperature + (energy / thermalCapacityOfTank);
	}
	
	/**
	 * 
	 * @param oldTemperature [°C]
	 * @param newTemperature [°C]
	 * @param timeDifference [s]
	 * @return [W]
	 */
	public double calculatePowerDrawOff(double oldTemperature, double newTemperature, long timeDifference) {
		double deltaTheta = newTemperature - oldTemperature;
		double energy = deltaTheta * avgThermalCapacityOfWater * tankCapacity;
		double power = energy / timeDifference;
		return power;
	}
	
	/**
	 * 
	 * @param oldTemperature [°C]
	 * @param newTemperature [°C]
	 * @return [J]
	 */
	public double calculateEnergyDrawOff(double oldTemperature, double newTemperature) {
		double deltaTheta = newTemperature - oldTemperature;
		double energy = deltaTheta * avgThermalCapacityOfWater * tankCapacity;
		return energy;
	}
	
	public double setCurrentWaterTemperature(double temperature) {
		return waterTemperature= temperature;
	}
	
	public double getCurrentWaterTemperature() {
		return waterTemperature;
	}
	
	public double getTankCapacity() {
		return tankCapacity;
	}
	
	public double getAmbientTemperature() {
		return ambientTemperature;
	}
	
	public double getTankDiameter() {
		return tankDiameter;
	}
	
}
