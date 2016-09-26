package osh.hal.exchange;

import java.util.UUID;

import osh.cal.CALComExchange;
import osh.datatypes.cruisecontrol.OptimizedDataStorage.EqualData;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class WaterStorageSumDetailsComExchange extends CALComExchange implements EqualData<WaterStorageSumDetailsComExchange> {
	
	private int maxDeltaForEquality = 4;
	private double posSum, negSum, sum;
	

	/**
	 * CONSTRUCTOR
	 */
	public WaterStorageSumDetailsComExchange(UUID deviceID, Long timestamp, double posSum, double negSum, double sum) {
		super(deviceID, timestamp);
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
	public boolean equalData(WaterStorageSumDetailsComExchange o) {
		return (Math.abs(posSum - o.posSum) < maxDeltaForEquality &&
				Math.abs(negSum - o.negSum) < maxDeltaForEquality &&
				Math.abs(sum - o.sum) < maxDeltaForEquality);
			
	}
	
}
