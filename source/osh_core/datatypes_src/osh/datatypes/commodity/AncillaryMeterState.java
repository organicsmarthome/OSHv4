package osh.datatypes.commodity;

import java.util.Arrays;

import osh.esc.ArrayUtils;

/** Wrapper class for the state of the ancillary meter
 * 
 * @author Sebastian Kramer
 *
 */
public class AncillaryMeterState {
	
	private static final int enumValCount = AncillaryCommodity.values().length;
	private double[] powerStates = new double[enumValCount];
	
	public AncillaryMeterState() {
//		Arrays.fill(powerStates, 0.0);
	}
	
	public AncillaryMeterState(AncillaryMeterState other) {
//		powerStates = new double[AncillaryCommodity.values().length];
		powerStates = Arrays.copyOf(powerStates, enumValCount);
	}
	
	public double getPower(AncillaryCommodity ancillaryCommodity) {
		return powerStates[ancillaryCommodity.ordinal()];
	}
	
	public void setPower(AncillaryCommodity ancillaryCommodity, double power) {
		powerStates[ancillaryCommodity.ordinal()] = power;
	}
	
	public void clear() {
//		Arrays.fill(powerStates, 0.0);
		ArrayUtils.fillArrayDouble(powerStates, 0.0);
	}
	
	public double[] getAllPowerStates() {
		return powerStates;
	}
	
	public AncillaryMeterState clone() {
		return new AncillaryMeterState(this);
	}
}
