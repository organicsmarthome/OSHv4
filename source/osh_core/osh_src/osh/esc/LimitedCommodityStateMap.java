package osh.esc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import osh.datatypes.commodity.Commodity;

public class LimitedCommodityStateMap implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9167758959788863484L;

	private double[] powers;
	
	/*
	 * 0 = voltage
	 */
	private double[][] addElectrical;
	
	/*
	 * 0 = temperature
	 * 1 = mass flow
	 */
	private double[][] addThermal;
	
	private static final byte thermalTempIndex = 0;
	
	private static final Commodity[] allCommodities = Commodity.values();
	private static final int commodityCount = Commodity.values().length;
	
	private int[] ordinalToPowerMap = new int[commodityCount];
	private int[] ordinalToElecMap = new int[commodityCount];
	private int[] ordinalToThermalMap = new int[commodityCount];
	
	private boolean[] keySet = new boolean[commodityCount];

	private int modCount = 0;
	
	private static final Commodity[] allElectricalCommodities = {
			Commodity.ACTIVEPOWER,
			Commodity.REACTIVEPOWER,
	};
	
//	private static final Commodity[] allThermalCommodities = {
//			Commodity.COLDWATERPOWER,
//			Commodity.DOMESTICHOTWATERPOWER,
//			Commodity.HEATINGHOTWATERPOWER,
//			Commodity.LIQUIDGASPOWER,
//			Commodity.NATURALGASPOWER
//	};
	
	private static final List<Commodity> allElec = new ArrayList<Commodity>();
	
	static {
		for (Commodity c : allElectricalCommodities) {
			allElec.add(c);
		}
	}
	
	public LimitedCommodityStateMap() {
		
		powers = new double[commodityCount];
//		Arrays.fill(ordinalToPowerMap, -1);
//		Arrays.fill(ordinalToElecMap, -1);
//		Arrays.fill(ordinalToThermalMap, -1);
		for (int i = 0; i < commodityCount; i++) {
			ordinalToElecMap[i] = -1;
			ordinalToThermalMap[i] = -1;
		}
		int elecParts = allElec.size();
		addElectrical = new double[elecParts][1];
		addThermal = new double[commodityCount - elecParts][2];
		int elecCount = 0, thermalCount = 0;
		
		for (int i = 0; i < commodityCount; i++) {
			Commodity c = allCommodities[i];
			
			ordinalToPowerMap[c.ordinal()] = i;
			
			if (allElec.contains(c)) {
				ordinalToElecMap[c.ordinal()] = elecCount++;
			} else {
				ordinalToThermalMap[c.ordinal()] = thermalCount++;
			}
		}
	}
	
	public LimitedCommodityStateMap(Commodity[] allPossibleCommodities) {
		
		powers = new double[allPossibleCommodities.length];
		for (int i = 0; i < commodityCount; i++) {
			ordinalToPowerMap[i] = -1;
			ordinalToElecMap[i] = -1;
			ordinalToThermalMap[i] = -1;
		}
//		Arrays.fill(ordinalToPowerMap, -1);
//		Arrays.fill(ordinalToElecMap, -1);
//		Arrays.fill(ordinalToThermalMap, -1);
		int elecCount = 0, thermalCount = 0;
		
		for (int i = 0; i < allPossibleCommodities.length; i++) {
			Commodity c = allPossibleCommodities[i];
			
			ordinalToPowerMap[c.ordinal()] = i;
			
			if (allElec.contains(c)) {
				ordinalToElecMap[c.ordinal()] = elecCount++;
			} else {
				ordinalToThermalMap[c.ordinal()] = thermalCount++;
			}
		}
		
		addElectrical = new double[elecCount][1];
		addThermal = new double[thermalCount][2];
	}
	
	public LimitedCommodityStateMap(List<Commodity> allPossibleCommodities) {
		
		powers = new double[allPossibleCommodities.size()];
//		Arrays.fill(ordinalToPowerMap, -1);
//		Arrays.fill(ordinalToElecMap, -1);
//		Arrays.fill(ordinalToThermalMap, -1);
		for (int i = 0; i < commodityCount; i++) {
			ordinalToPowerMap[i] = -1;
			ordinalToElecMap[i] = -1;
			ordinalToThermalMap[i] = -1;
		}
		int elecCount = 0, thermalCount = 0;
		
		for (int i = 0; i < allPossibleCommodities.size(); i++) {
			Commodity c = allPossibleCommodities.get(i);
			
			ordinalToPowerMap[c.ordinal()] = i;
			
			if (allElec.contains(c)) {
				ordinalToElecMap[c.ordinal()] = elecCount++;
			} else {
				ordinalToThermalMap[c.ordinal()] = thermalCount++;
			}
		}
		
		addElectrical = new double[elecCount][1];
		addThermal = new double[thermalCount][2];
	}
	
	public double getPower(Commodity c) {
		return keySet[c.ordinal()] ? powers[ordinalToPowerMap[c.ordinal()]] : 0;
	}
	
	public double getPowerWithoutCheck(Commodity c) {
		return powers[ordinalToPowerMap[c.ordinal()]];
	}
	
	public void addPower(Commodity c, double power) {
		powers[ordinalToPowerMap[c.ordinal()]] += power;
	}
	
	public double[] getAdditionalElectrical(Commodity c) {
		return keySet[c.ordinal()] ? addElectrical[ordinalToElecMap[c.ordinal()]] : null;
	}
	
	public double[] getAdditionalThermal(Commodity c) {
		return keySet[c.ordinal()] ? addThermal[ordinalToThermalMap[c.ordinal()]] : null;
	}
	
	public double getTemperature(Commodity c) {
		return keySet[c.ordinal()] ? addThermal[ordinalToThermalMap[c.ordinal()]][thermalTempIndex] : 0;
	}
	
	public double getTemperatureWithoutCheck(Commodity c) {
		return addThermal[ordinalToThermalMap[c.ordinal()]][thermalTempIndex];
	}
	
	public void setPower(Commodity c, double power) {
		powers[ordinalToPowerMap[c.ordinal()]] = power;
		keySet[c.ordinal()] = true;
		modCount++;
	}
	
	public void setOrAddPower(Commodity c, double power) {
		if (keySet[c.ordinal()]) {
			powers[ordinalToPowerMap[c.ordinal()]] += power;
		} else {
			powers[ordinalToPowerMap[c.ordinal()]] = power;
			keySet[c.ordinal()] = true;
		}
		modCount++;
	}
	
	public void setAllThermal(Commodity c, double power, double[] addThermal) {
		powers[ordinalToPowerMap[c.ordinal()]] = power;
		this.addThermal[ordinalToThermalMap[c.ordinal()]] = addThermal;
		keySet[c.ordinal()] = true;
		modCount++;
	}
	
	public void setTemperature(Commodity c, double temp) {
		addThermal[ordinalToThermalMap[c.ordinal()]][0] = temp;
		keySet[c.ordinal()] = true;
		modCount++;
	}
	
	public boolean containsCommodity(Commodity c) {
		return keySet[c.ordinal()];
	}
	
	public void resetCommodity(Commodity c) {
		keySet[c.ordinal()] = false;
	}
	
	public boolean isEmpty() {
		return modCount == 0;
	}
	
	public void clear() {
//		Arrays.fill(powers, 0);
//		Arrays.fill(addElectrical, new double[1]);
//		Arrays.fill(addThermal, new double[2]);
//		Arrays.fill(keySet, false);
		ArrayUtils.fillArrayBoolean(keySet, false);
		modCount = 0;
	}
}
