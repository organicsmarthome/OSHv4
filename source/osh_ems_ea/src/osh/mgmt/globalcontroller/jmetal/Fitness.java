package osh.mgmt.globalcontroller.jmetal;

import java.util.EnumMap;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.utils.CostCalculator;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class Fitness implements IFitness {
	
	private IGlobalLogger globalLogger;

	private int epsOptimizationObjective;
	
	private int plsOptimizationObjective;
	private int varOptimizationObjective;
	private double upperOverlimitFactor;
	private double lowerOverlimitFactor;
	
	/**
	 * CONSTRUCTOR
	 * @param epsOptimizationObjective
	 * @param plsOptimizationObjective
	 * @param overlimitfactor
	 */
	public Fitness(
			IGlobalLogger globalLogger,
			int epsOptimizationObjective, 
			int plsOptimizationObjective,
			int varOptimizationObjective,
			double upperOverlimitFactor,
			double lowerOverlimitFactor) {
		
		this.globalLogger = globalLogger;
		
		// Energy Price Signals
		this.epsOptimizationObjective = epsOptimizationObjective;
		
		// Power Limit Signal
		this.plsOptimizationObjective = plsOptimizationObjective;
		this.varOptimizationObjective = varOptimizationObjective;
		this.upperOverlimitFactor = upperOverlimitFactor;
		this.lowerOverlimitFactor = lowerOverlimitFactor;
	}
	
	public double getFitnessValue(
			long beginAt,
			long endAt,
			AncillaryCommodityLoadProfile ancillaryMeter,
			EnumMap<AncillaryCommodity,PriceSignal> priceSignals,
			EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals
			) {
		
//		double oldCosts = getFitnessValueOld(beginAt, endAt, ancillaryMeter, priceSignals, powerLimitSignals);
				
		double costs2 = CostCalculator.calcRangeCosts(
				epsOptimizationObjective, 
				varOptimizationObjective,
				plsOptimizationObjective, 
				beginAt, 
				endAt, 
				upperOverlimitFactor, 
				lowerOverlimitFactor, 
				ancillaryMeter, 
				priceSignals, 
				powerLimitSignals, 
				globalLogger);
		
//		if (Math.abs(oldCosts - costs2) > 0.000001) {
//			globalLogger.logError("WARNING WARNING WARNING");
//		}
		
		return costs2;
	}
	
	
//	private double getFitnessValueOld(
//			long beginAt,
//			long endAt,
//			ancillaryLoadProfile ancillaryMeter,
//			EnumMap<AncillaryCommodity,PriceSignal> priceSignals,
//			EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals
//			) {
//		
//		double fitnessValue = 0;
//		
//		Commodity[] pricedCommodities = {
//			Commodity.ACTIVEPOWER,
//			Commodity.REACTIVEPOWER,
//			Commodity.NATURALGASPOWER
//		};
//		
//		for (Commodity c : pricedCommodities) {
//			Long nextLoadChange = null;
//			long currenttime = beginAt;
//			Long nextPriceChange = null;
//			
//			ancillaryCommodity[] relevantancillaryCommodities = {};
//			
//			// For every Commodity there is a different set of ancillaryCommodities relevant
//			if (c.equals(Commodity.ACTIVEPOWER)) {
//				ancillaryCommodity[] tempRelevantancillaryCommodities = { 
//						ancillaryCommodity.ACTIVEPOWEREXTERNAL,
//						ancillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION,
//						ancillaryCommodity.CHPACTIVEPOWERFEEDIN,
//						ancillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION,
//						ancillaryCommodity.PVACTIVEPOWERFEEDIN
//						};
//				
//				relevantancillaryCommodities = tempRelevantancillaryCommodities;
//			}
//			else if (c.equals(Commodity.REACTIVEPOWER)) {
//				ancillaryCommodity[] tempRelevantancillaryCommodities = { 
//						ancillaryCommodity.REACTIVEPOWEREXTERNAL
//						};
//				
//				relevantancillaryCommodities = tempRelevantancillaryCommodities;
//			}
//			else if (c.equals(Commodity.NATURALGASPOWER)) {
//				ancillaryCommodity[] tempRelevantancillaryCommodities = { 
//						ancillaryCommodity.NATURALGASPOWEREXTERNAL
//						};
//				
//				relevantancillaryCommodities = tempRelevantancillaryCommodities;
//			}
//			
//			//look for all future load changes
//			SortedSet<Long> loadChanges = new TreeSet<Long>();
//			loadChanges.add(Long.MAX_VALUE);
//			
//			
//			for (ancillaryCommodity vc : relevantancillaryCommodities) {
//				Long time = ancillaryMeter.getNextLoadChange(vc, currenttime);
//				while(time != null) {
//					loadChanges.add(time);
//					time = ancillaryMeter.getNextLoadChange(vc, time);					
//				}
//			}
//			Iterator<Long> loadCh = loadChanges.iterator();
//			
//			//look for all future price changes
//			SortedSet<Long> priceChanges = new TreeSet<Long>();
//			priceChanges.add(Long.MAX_VALUE);
//			for (ancillaryCommodity vc : relevantancillaryCommodities) {
//				long tmpTime = currenttime;
//				PriceSignal tempPriceSignal = priceSignals.get(vc);
//				if (tempPriceSignal != null) {
//					while (tmpTime <= endAt) {
//						Long tempNextPriceChange = tempPriceSignal.getNextPriceChange(tmpTime);
//						if (tempNextPriceChange != null) {
//							tmpTime = tempPriceSignal.getNextPriceChange(tmpTime);
//							priceChanges.add(tmpTime);
//						} else {
//							break;
//						}
//					}					
//				}
//			}
//			Iterator<Long> priceCh = priceChanges.iterator();
//			
//			do { //iterate over load changes
//				
//				
//				//look for the next change in price or load
//				if (nextLoadChange == null)
//					nextLoadChange = loadCh.next();
//				if (nextPriceChange == null)
//					nextPriceChange = priceCh.next();
//				long minNextChange;
//				
//				if (nextLoadChange < nextPriceChange) {
//					minNextChange = nextLoadChange;
//					nextLoadChange = null;
//				} else if (nextLoadChange > nextPriceChange) {
//					minNextChange = nextPriceChange;
//					nextPriceChange = null;
//				} else {
//					minNextChange = nextLoadChange;
//					nextLoadChange = null;
//					nextPriceChange = null;
//				}
//				
//				if ( minNextChange == Long.MAX_VALUE ) { 
//					break;
//				}
//				else if (minNextChange >= endAt) {
//					minNextChange = endAt;
//				}
//				
//				// get time factor (constant time in [s] and price is [cents/kWh], whereas load/power is in [W])
//				double timeFactor = (minNextChange - currenttime) / (3600000.0);
//				
//				// EPS
//				double fitnessNew = EPSCostCalculator.calcEpsFitnessValue(
//						epsOptimizationObjective, 
//						currenttime, 
//						timeFactor, 
//						c, 
//						ancillaryMeter, 
//						priceSignals, 
//						globalLogger);
//				
//				fitnessValue = fitnessValue + fitnessNew;
//				
//				// PLS
//				double additionalPLS = PLSCostCalculator.calcPlsFitnessValue(
//						plsOptimizationObjective,
//						upperOverlimitFactor,
//						lowerOverlimitFactor,
//						currenttime,
//						timeFactor,
//						c,
//						ancillaryMeter,
//						priceSignals,
//						powerLimitSignals,
//						globalLogger);
//				
//				fitnessValue = fitnessValue + additionalPLS;
//				
//				currenttime = minNextChange;
//				if (minNextChange >= endAt) {
//					break;
//				}
//				
//			} while (true); //it's not what it seems to be...
//		}
//		
//		return fitnessValue;		
//	}
}
