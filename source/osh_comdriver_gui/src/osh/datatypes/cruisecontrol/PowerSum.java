package osh.datatypes.cruisecontrol;

import osh.datatypes.cruisecontrol.OptimizedDataStorage.EqualData;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class PowerSum implements EqualData<PowerSum> {

	public double posSum, negSum, sum;

	
	/**
	 * CONSTRUCTOR
	 */
	public PowerSum(double posSum, double negSum, double sum) {
		this.posSum = posSum;
		this.negSum = negSum;
		this.sum = sum;
	}

	public double getPosSum() {
		return posSum;
	}
	
	public double getNegSum() {
		return negSum;
	}
	
	public double getSum() {
		return sum;
	}
	
	@Override
	public boolean equalData(PowerSum o) {
		return (Math.abs(posSum - o.posSum) < 4 &&
				Math.abs(negSum - o.negSum) < 4 &&
				Math.abs(sum - o.sum) < 4);
			
	}
	
}
