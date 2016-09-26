package osh.datatypes.power;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public class PowerInterval {
	
	private double lowerLimit;
	private double upperLimit;
	
	private final double UNKNOWN_UPPERLIMIT = 43000;
	private final double UNKNOWN_LOWERLIMIT = -43000;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public PowerInterval() {
		this.upperLimit = UNKNOWN_UPPERLIMIT;
		this.lowerLimit = UNKNOWN_LOWERLIMIT;
	}
	
	/**
	 * CONSTRUCTOR
	 * @param powerUpperLimit
	 */
	public PowerInterval(double powerUpperLimit) {
		this.upperLimit = powerUpperLimit;
		this.lowerLimit = UNKNOWN_LOWERLIMIT;
	}
	
	/**
	 * CONSTRUCTOR
	 * @param powerUpperLimit
	 * @param powerLowerLimit
	 */
	public PowerInterval(
			double powerUpperLimit, 
			double powerLowerLimit) {
		this.upperLimit = powerUpperLimit;
		this.lowerLimit = powerLowerLimit;
	}
	

	public double[] getPowerLimits() {
		double[] activeLimits = new double[2];
		activeLimits[0] = this.upperLimit;
		activeLimits[1] = this.lowerLimit;
		return activeLimits;
	}
	
	public double getPowerUpperLimit() {
		return this.upperLimit;
	}
	
	public double getPowerLowerLimit() {
		return this.lowerLimit;
	}
	
	
	public boolean equals(PowerInterval other) {
		if (other == null) {
			return false;
		}
		if (this.lowerLimit == other.lowerLimit 
				&& this.upperLimit == other.upperLimit
				&& this.UNKNOWN_UPPERLIMIT == other.UNKNOWN_UPPERLIMIT
				&& this.UNKNOWN_LOWERLIMIT == other.UNKNOWN_LOWERLIMIT) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public PowerInterval clone() {
		return new PowerInterval(upperLimit, lowerLimit);
	}
	
	@Override
	public String toString() {
		return "uL=" + upperLimit + " lL=" + lowerLimit;
	}
	
}
