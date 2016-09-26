package osh.utils;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedSet;

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.power.LoadProfile;
import osh.datatypes.power.PowerInterval;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class CostCalculator {

	private static final double WsTOkWHDivisor = 3600000.0;


	/* ##########################
	 *  epsOptimizationObjective
	 * ##########################
	 * 
	 * 0: "ACTIVEPOWEREXTERNAL 
	 * 		+ NATURALGASPOWEREXTERNAL" : <br>
	 * > sum of all activePowers * ACTIVEPOWEREXTERNAL-Price<br>
	 * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
	 * 
	 * -------------------------------------------------------------------------------------------------------------
	 * 
	 * 1: "ACTIVEPOWEREXTERNAL 
	 * 		+ PVACTIVEPOWERFEEDIN
	 * 		+ NATURALGASPOWEREXTERNAL" : <br>
	 * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
	 * > if (sum of all activePowers < 0) -> Math.max(pvPower,(sum of all activePowers)) * PVACTIVEPOWERFEEDIN<br>
	 * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
	 * 
	 * -------------------------------------------------------------------------------------------------------------
	 * 
	 * 2: "ACTIVEPOWEREXTERNAL 
	 * 		+ PVACTIVEPOWERFEEDIN + PVACTIVEPOWERAUTOCONSUMPTION
	 * 		+ NATURALGASPOWEREXTERNAL"<br>
	 * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
	 * > pvPowerToGrid * PVACTIVEPOWERFEEDIN<br>
	 * > pvPowerAutoConsumption * PVACTIVEPOWERAUTOCONSUMPTION<br>
	 * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
	 * 
	 * -------------------------------------------------------------------------------------------------------------
	 * 
	 * 3: "ACTIVEPOWEREXTERNAL 
	 * 		+ PVACTIVEPOWERFEEDIN 
	 * 		+ CHPACTIVEPOWERFEEDIN
	 * 		+ NATURALGASPOWEREXTERNAL" : <br>
	 * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
	 * > pvPowerToGrid * PVACTIVEPOWERFEEDIN<br>
	 * > chpPowerToGrid * CHPACTIVEPOWERFEEDIN<br>
	 * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
	 * > IMPORTANT: PV and CHP to grid depending on their power proportionally!<br>
	 * 
	 * -------------------------------------------------------------------------------------------------------------
	 * 
	 * 4: "ACTIVEPOWEREXTERNAL 
	 * 		+ PVACTIVEPOWERFEEDIN + PVACTIVEPOWERAUTOCONSUMPTION
	 * 		+ CHPACTIVEPOWERFEEDIN + CHPACTIVEPOWERAUTOCONSUMPTION
	 * 		+ NATURALGASPOWEREXTERNAL"<br>
	 * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
	 * > pvPowerToGrid * PVACTIVEPOWERFEEDIN<br>
	 * > pvPowerAutoConsumption * PVACTIVEPOWERAUTOCONSUMPTION<br>
	 * > chpPowerToGrid * CHPACTIVEPOWERFEEDIN<br>
	 * > chpPowerAutoConsumption * CHPACTIVEPOWERAUTOCONSUMPTION<br>
	 * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
	 * > IMPORTANT: PV and CHP to grid depending on their power proportionally!<br>
	 * 
	 * 
	 * 
	 * ##########################
	 *  plsOptimizationObjective
	 * ##########################
	 * 
	 * 0: "NOTHING" <br>
	 * > no Costs<br>
	 * 
	 * -------------------------------------------------------------------------------------------------------------
	 * 
	 * 1: "ACTIVEPOWEREXTERNAL" <br>
	 * > if (sum of all activePowers > 0) && (sum of all activePowers > powerUpperLimit)
	 * 		 -> (sum of all activePowers above Limit) * (overLimitFactor * ACTIVEPOWEREXTERNAL-Price)<br>
	 * > if (sum of all activePowers < 0) && (sum of all activePowers < powerLowerLimit)
	 * 		 -> (|sum of all activePowers below Limit|) * (overLimitFactor * Proportional(CHP+PV)FeedIn-Price)<br>
	 * 
	 * -------------------------------------------------------------------------------------------------------------
	 * 
	 * 2: "ACTIVEPOWEREXTERNAL + REACTIVEPOWEREXTERNAL" <br>
	 * > if (sum of all activePowers > 0) && (sum of all activePowers > powerUpperLimit)
	 * 		 -> (sum of all activePowers above Limit) * (overLimitFactor * ACTIVEPOWEREXTERNAL-Price)<br>
	 * > if (sum of all activePowers < 0) && (sum of all activePowers < powerLowerLimit)
	 * 		 -> (sum of all activePowers below Limit) * (overLimitFactor * Proportional(CHP+PV)FeedIn-Price)<br>
	 * > if (sum of all reactivePowers > powerUpperLimit) || (sum of all reactivePowers < powerLowerLimit)
	 * 		 -> |(sum of all reactivePowers above Limit)| * (overLimitFactor * REACTIVEPOWEREXTERNAL-Price)<br>
	 * 
	 * 
	 * 
	 * ##########################
	 *  varOptimizationObjective
	 * ##########################
	 * 
	 * 0: "NOTHING" <br>
	 * > no Costs<br>
	 * 
	 * -------------------------------------------------------------------------------------------------------------
	 * 
	 * 1: "REACTIVEPOWEREXTERNAL" <br>
	 * > |reactivePower| * REACTIVEPOWEREXTERNAL-Price<br>
	 */


	public static double calcRangeCosts(
			int epsOptimizationObjective,
			int varOptimizationObjective,
			int plsOptimizationObjective,
			long startCalc,
			long endCalc,
			double upperOverlimitFactor,
			double lowerOverlimitFactor,
			AncillaryCommodityLoadProfile ancillaryMeter,
			EnumMap<AncillaryCommodity,PriceSignal> priceSignals,
			EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
			IGlobalLogger globalLogger) {

		double costs = 0;

		costs += (calcActivePowerCosts(
					ancillaryMeter.getIteratorForSubMap(AncillaryCommodity.ACTIVEPOWEREXTERNAL, startCalc, endCalc), 
					priceSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getIteratorForSubMap(startCalc, endCalc), 
					plsOptimizationObjective > 0 ? 
							powerLimitSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getIteratorForSubMap(startCalc, endCalc)
							: null, 
					ancillaryMeter.getFloorEntry(AncillaryCommodity.ACTIVEPOWEREXTERNAL, startCalc),
					priceSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getFloorEntry(startCalc),
					plsOptimizationObjective > 0 ? 
							powerLimitSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getFloorEntry(startCalc)
							: null,
					plsOptimizationObjective, 
					epsOptimizationObjective, 
					upperOverlimitFactor, 
					lowerOverlimitFactor, 
					startCalc, 
					endCalc) / WsTOkWHDivisor);

//		SortedSet<Long> gasPowerChanges = calcSingularLoadChanges(AncillaryCommodity.NATURALGASPOWEREXTERNAL,
//				ancillaryMeter, startCalc, endCalc);
//		SortedSet<Long> gasPriceChanges = calcPriceChanges(new AncillaryCommodity[]{AncillaryCommodity.NATURALGASPOWEREXTERNAL},
//				priceSignals, startCalc, endCalc);
//
//		costs += (calcGasPower(gasPowerChanges, gasPriceChanges, startCalc, endCalc, 
//				ancillaryMeter, priceSignals.get(AncillaryCommodity.NATURALGASPOWEREXTERNAL)) / WsTOkWHDivisor);
		
		costs += (calcGasCosts(
				ancillaryMeter.getIteratorForSubMap(AncillaryCommodity.NATURALGASPOWEREXTERNAL, startCalc, endCalc), 
				priceSignals.get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getIteratorForSubMap(startCalc, endCalc), 
				ancillaryMeter.getFloorEntry(AncillaryCommodity.NATURALGASPOWEREXTERNAL, startCalc),
				priceSignals.get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getFloorEntry(startCalc),
				plsOptimizationObjective, 
				epsOptimizationObjective, 
				upperOverlimitFactor, 
				lowerOverlimitFactor, 
				startCalc, 
				endCalc) / WsTOkWHDivisor);

		//eps > 0 --> We have to calculate FeedIn/AutoConsumption costs
		if (epsOptimizationObjective > 0) {
			//eps == 3 || eps == 4 calculate FeedIn costs for PV and CHP
			//eps == 1 || eps == 2 calculate FeedIn only for PV
			
			costs += (calcFeedInCosts(
					ancillaryMeter, 
					priceSignals, 
					powerLimitSignals, 
					plsOptimizationObjective, 
					epsOptimizationObjective, 
					lowerOverlimitFactor, 
					startCalc, 
					endCalc) / WsTOkWHDivisor);
			
			//eps == 2 || eps == 4 calculate AutoConsumption costs
			if (epsOptimizationObjective == 2 || epsOptimizationObjective == 4) {
				//eps == 4 calculate AutoConsumption costs for PV and CHP
				//eps == 2 calculate AutoConsumption only for PV
//				AncillaryCommodity[] relevantAutoConsumptCommodities = epsOptimizationObjective == 4 
//						? new AncillaryCommodity[]{AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION} 
//						: new AncillaryCommodity[]{AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION};
//
//				SortedSet<Long> autoConsumptPowerChanges = calcLoadChanges(relevantAutoConsumptCommodities,
//						ancillaryMeter, startCalc, endCalc);
//				SortedSet<Long> autoConsumptPriceChanges = calcPriceChanges(relevantAutoConsumptCommodities,
//						priceSignals, startCalc, endCalc);
//
//				costs += (calcAutoConsumptPower(autoConsumptPowerChanges, autoConsumptPriceChanges,
//						relevantAutoConsumptCommodities, startCalc, endCalc, ancillaryMeter, priceSignals) / WsTOkWHDivisor);
				
				costs += (calcAutoConsumptCosts(
						ancillaryMeter, 
						priceSignals, 
						epsOptimizationObjective, 
						startCalc, 
						endCalc) / WsTOkWHDivisor);
			}
		}

		//var == 1 --> calculate ReactivePower Costs
		if (varOptimizationObjective > 0) {

//			SortedSet<Long> varPowerChanges = calcLoadChanges(new AncillaryCommodity[]{AncillaryCommodity.REACTIVEPOWEREXTERNAL},
//					ancillaryMeter, startCalc, endCalc);
			SortedSet<Long> varPowerChanges = calcSingularLoadChanges(AncillaryCommodity.NATURALGASPOWEREXTERNAL,
					ancillaryMeter, startCalc, endCalc);
			SortedSet<Long> varPriceChanges = calcPriceChanges(new AncillaryCommodity[]{AncillaryCommodity.REACTIVEPOWEREXTERNAL},
					priceSignals, startCalc, endCalc);

			SortedSet<Long> varLimitChanges = null;

			//pls == 2 --> we need to respect PLS for ReactivePower, see PLS for ActivePower
			if (plsOptimizationObjective == 2)
				varLimitChanges = calcPowerIntervallChanges(new AncillaryCommodity[]{AncillaryCommodity.REACTIVEPOWEREXTERNAL}, 
						powerLimitSignals, startCalc, endCalc);

			costs += (calcVarPower(varPowerChanges, varPriceChanges, varLimitChanges, upperOverlimitFactor, lowerOverlimitFactor, 
					startCalc, endCalc, ancillaryMeter, priceSignals.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL), 
					powerLimitSignals.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL)) / WsTOkWHDivisor);
		}

		return costs;
	}

	public static double[] calcSingularCosts(
			int epsOptimizationObjective,
			int varOptimizationObjective,
			int plsOptimizationObjective,
			long time,
			long timeFactor,
			double upperOverLimitFactor,
			double lowerOverLimitFactor,
			AncillaryMeterState ancillaryMeterState,
			EnumMap<AncillaryCommodity,PriceSignal> priceSignals,
			EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals) {
		
		double epsCosts = 0, plsCosts = 0, gasCosts = 0, feedInCostsPV = 0, feedInCostsCHP = 0, autoConsumptionCosts = 0;
		
		//ACTIVEPOWER Costs
		double activePower = ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
		double activePrice = priceSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getPrice(time);
		PowerInterval activePI = null;
		
		if (activePower > 0)
			epsCosts += activePower * activePrice * timeFactor;
		
		if (plsOptimizationObjective > 0) {
			activePI = powerLimitSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getPowerLimitInterval(time);
			
			double upperLimit = activePI.getPowerUpperLimit();			
			
			if (activePower > upperLimit)
				plsCosts += timeFactor * upperOverLimitFactor * Math.abs(activePower - upperLimit) * activePrice;
			
			//If Feed-In costs, lowerLimit Violations will be calculated there
			if (epsOptimizationObjective < 1) {
				double lowerLimit = activePI.getPowerLowerLimit();
				
				if (activePower < lowerLimit)
					plsCosts += timeFactor * lowerOverLimitFactor * Math.abs(activePower - lowerLimit) * activePrice;
			}
		}

		//GASPOWER Costs
		double power = ancillaryMeterState.getPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL);


		double price = 0.0;
		if (priceSignals.get(AncillaryCommodity.NATURALGASPOWEREXTERNAL) != null) 
			price = priceSignals.get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(time);

		gasCosts += timeFactor * power * price;

		//FeedIn and/or AutoConsumption costs
		if (epsOptimizationObjective > 0) {
			
			//FEEDIN costs
			double feedInPower = 0;
			
			//FeedIn costs for PV
			double pvFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN);				
			
			if (pvFeedIn < 0) {
				feedInPower += pvFeedIn;
				feedInCostsPV += timeFactor * pvFeedIn * priceSignals.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPrice(time);
			}				
			
			//FeedIn costs for CHP if eps == 3 || eps == 4
			if (epsOptimizationObjective > 2) {
				double chpFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);
				
				
				if (chpFeedIn < 0) {
					feedInPower += chpFeedIn;
					feedInCostsCHP += timeFactor * chpFeedIn * priceSignals.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN).getPrice(time);
				}					
			}
			
			epsCosts += feedInCostsPV + feedInCostsCHP;
			
			//PLS costs for active Power --> negative FeedInCosts over the limit * lowerOverLimitFactor
			if (plsOptimizationObjective > 0) {
				double lowerLimit = activePI.getPowerLowerLimit();
				
				if (activePower < lowerLimit) {
					plsCosts -= lowerOverLimitFactor * Math.abs(Math.abs(activePower - lowerLimit) / feedInPower) * (feedInCostsPV + feedInCostsCHP);
				}				
			}
			
			
			//AUTOCONSUMPTION costs
			if (epsOptimizationObjective == 2 || epsOptimizationObjective == 4) {				
				
				//AUTOCONSUMPTION costs for PV
				double pvAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION);	
				if (pvAutoConsumption < 0)
					autoConsumptionCosts += timeFactor * pvAutoConsumption * priceSignals.get(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION).getPrice(time);
				
				
				//AUTOCONSUMPTION costs for CHP if eps == 4
				if (epsOptimizationObjective > 2) {
					double chpAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION);	
					if (chpAutoConsumption < 0)
						autoConsumptionCosts += timeFactor * chpAutoConsumption * priceSignals.get(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION).getPrice(time);
					
				}
				
				epsCosts += autoConsumptionCosts;
			}
		}
		
		//var == 1 --> calculate ReactivePower Costs
		if (varOptimizationObjective > 0) {

			double varPower = ancillaryMeterState.getPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL);
			PriceSignal varPrice = priceSignals.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL);
			
			//mosts EPS dont have a varPriceSignal, so we check to be on the safe side
			if (varPrice != null) {
				
				epsCosts += timeFactor * Math.abs(varPower) * varPrice.getPrice(time);
				
				//pls == 2 --> we need to respect PLS for ReactivePower
				if (plsOptimizationObjective == 2) {
					PowerInterval varPI = powerLimitSignals.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL).getPowerLimitInterval(time); 
					double lowerLimit = varPI.getPowerLowerLimit();
					double upperLimit = varPI.getPowerUpperLimit();
					
					if (varPower > upperLimit)
						plsCosts += timeFactor * upperOverLimitFactor * Math.abs(upperLimit - varPower) * varPrice.getPrice(time); 
					else if (varPower < lowerLimit)
						plsCosts += timeFactor * lowerOverLimitFactor * Math.abs(lowerLimit - varPower) * varPrice.getPrice(time); 
				}							
				
			} else {
				System.out.println("[CostCalculator] VarPowerCosts can't be calculated because VarPriceSignal is not provided");
			}			
		}

		/* array
		 * [0] = epsCosts
		 * [1] = plsCosts
		 * [2] = gasCosts
		 * [3] = feedInCompensationPV
		 * [4] = feedInCompensationCHP
		 * [5] = autoConsumptionCosts
		 */
		return new double[]{epsCosts / WsTOkWHDivisor, plsCosts / WsTOkWHDivisor, gasCosts / WsTOkWHDivisor, feedInCostsPV / WsTOkWHDivisor, feedInCostsCHP / WsTOkWHDivisor, autoConsumptionCosts / WsTOkWHDivisor};
	}

	private static SortedSet<Long> calcSingularLoadChanges(AncillaryCommodity ancillaryCommodity, AncillaryCommodityLoadProfile ancillaryMeter,
			long startTime, long endTime) {
		LongAVLTreeSet loadChanges = new LongAVLTreeSet(ancillaryMeter.getAllLoadChangesFor(ancillaryCommodity, startTime, endTime));
		loadChanges.add(endTime);
		loadChanges.add(Long.MAX_VALUE);

		return loadChanges;
	}

	private static SortedSet<Long> calcPriceChanges(AncillaryCommodity[] relevantCommodities, EnumMap<AncillaryCommodity, PriceSignal> prices,
			long startTime, long endTime) {
		SortedSet<Long> priceChanges = new LongAVLTreeSet();
		priceChanges.add(endTime);
		priceChanges.add(Long.MAX_VALUE);


		for (AncillaryCommodity vc : relevantCommodities) {
			PriceSignal ps = prices.get(vc);

			if (ps != null) {
				long currentTime = startTime;
				Long time = ps.getNextPriceChange(currentTime);
				while(time != null && time < endTime) {
					priceChanges.add(time);
					time = ps.getNextPriceChange(time);					
				}
			}
		}
		return priceChanges;
	}

	private static SortedSet<Long> calcPowerIntervallChanges(AncillaryCommodity[] relevantCommodities, 
			EnumMap<AncillaryCommodity, PowerLimitSignal> limits,
			long startTime, long endTime) {
		SortedSet<Long> limitChanges = new LongAVLTreeSet();
		limitChanges.add(endTime);
		limitChanges.add(Long.MAX_VALUE);


		for (AncillaryCommodity vc : relevantCommodities) {
			PowerLimitSignal pls = limits.get(vc);

			if (pls != null) {
				long currentTime = startTime;
				Long time = pls.getNextPowerLimitChange(currentTime);
				while(time != null && time < endTime) {
					limitChanges.add(time);
					time = pls.getNextPowerLimitChange(time);					
				}
			}
		}
		return limitChanges;
	}

	private static double calcActivePowerCosts(
			Iterator<Entry<Long, LoadProfile<AncillaryCommodity>.Tick>> activePowerIterator, 
			Iterator<Entry<Long, Double>> activePowerPriceIterator, 
			Iterator<Entry<Long, PowerInterval>> activePowerLimitIterator,
			Entry<Long, LoadProfile<AncillaryCommodity>.Tick> initialActivePower,
			Entry<Long, Double> initialActivePowerPrice,
			Entry<Long, PowerInterval> initialActivePowerLimit,
			int plsOptimizationObjective,
			int epsOptimizationObjective,
			double upperOverLimitFactor,
			double lowerOverLimitFactor,
			long startCalc,
			long endCalc) {

		Entry<Long, LoadProfile<AncillaryCommodity>.Tick> currentLoadChange = initialActivePower;
		Entry<Long, Double> currentPriceChange = initialActivePowerPrice;
		Entry<Long, PowerInterval> currentLimitChange = initialActivePowerLimit;
		
		Entry<Long, LoadProfile<AncillaryCommodity>.Tick> nextLoadChange = activePowerIterator.hasNext() ? activePowerIterator.next() : null;
		Entry<Long, Double> nextPriceChange = activePowerPriceIterator.hasNext() ? activePowerPriceIterator.next() : null;
		Entry<Long, PowerInterval> nextLimitChange = plsOptimizationObjective == 0 ? null : (activePowerLimitIterator.hasNext() ? activePowerLimitIterator.next() : null);

		Long nextLoadChangeKey = nextLoadChange == null ? Long.MAX_VALUE : nextLoadChange.getKey();
		Long nextPriceChangeKey = nextPriceChange == null ? Long.MAX_VALUE : nextPriceChange.getKey();
		Long nextLimitChangeKey = nextLimitChange == null ? Long.MAX_VALUE : nextLimitChange.getKey();
		
		long currentTime = startCalc;
		double costs = 0;

		while (nextLoadChange != null || nextPriceChange != null || nextLimitChange != null) { //iterate over load changes			

			long minNextChange = Math.min(nextLoadChangeKey, Math.min(nextPriceChangeKey, nextLimitChangeKey));

			if (minNextChange >= endCalc) {
				minNextChange = endCalc;
			}

			// get time factor (constant time in [s] and price is [cents/kWh], whereas load/power is in [W])
			double timeFactor = minNextChange - currentTime;

			int power = currentLoadChange.getValue().value;
			double price =  currentPriceChange.getValue();

			if (power > 0)
				costs += timeFactor * power * price;

			if (plsOptimizationObjective > 0) {
				double upperLimit = currentLimitChange.getValue().getPowerUpperLimit();
				

				if (power > upperLimit)
					costs += timeFactor * Math.abs(power - upperLimit) * price * upperOverLimitFactor;
				
				//If FeedIn-costs lowerLimitViolations will be calculated in calcFeedInPower
				if (epsOptimizationObjective < 1) {
					double lowerLimit = currentLimitChange.getValue().getPowerLowerLimit();
		
					if (power < lowerLimit) {
						costs += timeFactor * Math.abs(power - lowerLimit) * price * lowerOverLimitFactor;						
					}
				}
			}
			
			if (nextLoadChangeKey <= minNextChange) {
				currentLoadChange = nextLoadChange;
				if (activePowerIterator.hasNext()) {
					nextLoadChange = activePowerIterator.next();
					nextLoadChangeKey = nextLoadChange.getKey();
				} else {
					nextLoadChange = null;
					nextLoadChangeKey = Long.MAX_VALUE;
				}
			}
			if (nextPriceChangeKey <= minNextChange) {
				currentPriceChange = nextPriceChange;
				if (activePowerPriceIterator.hasNext()) {
					nextPriceChange = activePowerPriceIterator.next();
					nextPriceChangeKey = nextPriceChange.getKey();
				} else {
					nextPriceChange = null;
					nextPriceChangeKey = Long.MAX_VALUE;
				}	
			}
			if (nextLimitChangeKey <= minNextChange) {
				currentLimitChange = nextLimitChange;
				if (activePowerLimitIterator.hasNext()) {
					nextLimitChange = activePowerLimitIterator.next();
					nextLimitChangeKey = nextLimitChange.getKey();
				} else {
					nextLimitChange = null;
					nextLimitChangeKey = Long.MAX_VALUE;
				}
			}

			currentTime = minNextChange;			
		}
		
		if (currentTime < endCalc) {
			double timeFactor = endCalc - currentTime;

			int power = currentLoadChange.getValue().value;
			double price =  currentPriceChange.getValue();

			if (power > 0)
				costs += timeFactor * power * price;

			if (plsOptimizationObjective > 0) {
				double upperLimit = currentLimitChange.getValue().getPowerUpperLimit();
				

				if (power > upperLimit)
					costs += timeFactor * Math.abs(power - upperLimit) * price * upperOverLimitFactor;
				
				//If FeedIn-costs lowerLimitViolations will be calculated in calcFeedInPower
				if (epsOptimizationObjective < 1) {
					double lowerLimit = currentLimitChange.getValue().getPowerLowerLimit();
		
					if (power < lowerLimit) {
						costs += timeFactor * Math.abs(power - lowerLimit) * price * lowerOverLimitFactor;						
					}
				}
			}
		}
		

		return costs;
	}
	
	private static double calcGasCosts(
			Iterator<Entry<Long, LoadProfile<AncillaryCommodity>.Tick>> gasPowerIterator, 
			Iterator<Entry<Long, Double>> gasPowerPriceIterator, 
			Entry<Long, LoadProfile<AncillaryCommodity>.Tick> initialGasPower,
			Entry<Long, Double> initialGasPowerPrice,
			int plsOptimizationObjective,
			int epsOptimizationObjective,
			double upperOverLimitFactor,
			double lowerOverLimitFactor,
			long startCalc,
			long endCalc) {

		Entry<Long, LoadProfile<AncillaryCommodity>.Tick> currentLoad = initialGasPower;
		Entry<Long, Double> currentPrice = initialGasPowerPrice;
		
		Entry<Long, LoadProfile<AncillaryCommodity>.Tick> nextLoadChange = gasPowerIterator.hasNext() ? gasPowerIterator.next() : null;
		Entry<Long, Double> nextPriceChange = gasPowerPriceIterator.hasNext() ? gasPowerPriceIterator.next() : null;

		Long nextLoadChangeKey = nextLoadChange == null ? Long.MAX_VALUE : nextLoadChange.getKey();
		Long nextPriceChangeKey = nextPriceChange == null ? Long.MAX_VALUE : nextPriceChange.getKey();
		
		long currentTime = startCalc;
		double costs = 0;

		while (nextLoadChange != null || nextPriceChange != null) { //iterate over load changes			

			long minNextChange = Math.min(nextLoadChangeKey, nextPriceChangeKey);

			if (minNextChange >= endCalc) {
				minNextChange = endCalc;
			}

			// get time factor (constant time in [s] and price is [cents/kWh], whereas load/power is in [W])
			double timeFactor = minNextChange - currentTime;

			int power = currentLoad.getValue().value;
			double price =  currentPrice.getValue();

			if (power > 0)
				costs += timeFactor * power * price;

			if (nextLoadChangeKey <= minNextChange) {
				currentLoad = nextLoadChange;
				if (gasPowerIterator.hasNext()) {
					nextLoadChange = gasPowerIterator.next();
					nextLoadChangeKey = nextLoadChange.getKey();
				} else {
					nextLoadChange = null;
					nextLoadChangeKey = Long.MAX_VALUE;
				}
			}
			if (nextPriceChangeKey <= minNextChange) {
				currentPrice = nextPriceChange;
				if (gasPowerPriceIterator.hasNext()) {
					nextPriceChange = gasPowerPriceIterator.next();
					nextPriceChangeKey = nextPriceChange.getKey();
				} else {
					nextPriceChange = null;
					nextPriceChangeKey = Long.MAX_VALUE;
				}	
			}

			currentTime = minNextChange;			
		}
		
		if (currentTime < endCalc) {
			double timeFactor = endCalc - currentTime;

			int power = currentLoad.getValue().value;
			double price =  currentPrice.getValue();

			if (power > 0)
				costs += timeFactor * power * price;
		}

		return costs;
	}
	
	@SuppressWarnings("unchecked")
	private static double calcFeedInCosts(
			AncillaryCommodityLoadProfile ancillaryMeter,
			EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
			EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
			int plsOptimizationObjective,
			int epsOptimizationObjective,
			double lowerOverlimitFactor,
			long startCalc,
			long endCalc
			) {
		AncillaryCommodity[] relevantFeedInCommodities = epsOptimizationObjective > 2 
				? new AncillaryCommodity[]{AncillaryCommodity.PVACTIVEPOWERFEEDIN, AncillaryCommodity.CHPACTIVEPOWERFEEDIN} 
				: new AncillaryCommodity[]{AncillaryCommodity.PVACTIVEPOWERFEEDIN};
				
		Iterator<Entry<Long, LoadProfile<AncillaryCommodity>.Tick>>[] feedInIterators 
			= (Iterator<Entry<Long, LoadProfile<AncillaryCommodity>.Tick>>[]) new Iterator[relevantFeedInCommodities.length];
		Iterator<Entry<Long, Double>>[] feedInPriceIterator 
			= (Iterator<Entry<Long, Double>>[]) new Iterator[relevantFeedInCommodities.length];
		Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[] initialFeedInPower 
			= (Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[]) new Entry<?, ?>[relevantFeedInCommodities.length];
		Entry<Long, Double>[] initialFeedInPrice 
			= (Entry<Long, Double>[] )new Entry<?, ?>[relevantFeedInCommodities.length];
		
		for (int i = 0; i < relevantFeedInCommodities.length; i++) {
			feedInIterators[i] = ancillaryMeter.getIteratorForSubMap(relevantFeedInCommodities[i], startCalc, endCalc);
			initialFeedInPower[i] = ancillaryMeter.getFloorEntry(relevantFeedInCommodities[i], startCalc);
			
			feedInPriceIterator[i] = priceSignals.get(relevantFeedInCommodities[i]).getIteratorForSubMap(startCalc, endCalc);
			initialFeedInPrice[i] = priceSignals.get(relevantFeedInCommodities[i]).getFloorEntry(startCalc);
		}
		
		return calcFeedInCosts(
				feedInIterators, 
				feedInPriceIterator, 
				plsOptimizationObjective > 0 ? 
						powerLimitSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getIteratorForSubMap(startCalc, endCalc)
						: null, 
				initialFeedInPower,
				initialFeedInPrice,
				plsOptimizationObjective > 0 ? 
						powerLimitSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getFloorEntry(startCalc)
						: null,
				plsOptimizationObjective, 
				lowerOverlimitFactor, 
				startCalc, 
				endCalc);
	}
	
	@SuppressWarnings("unchecked")
	private static double calcFeedInCosts(
			Iterator<Entry<Long, LoadProfile<AncillaryCommodity>.Tick>>[] feedInIterators, 
			Iterator<Entry<Long, Double>>[] feedInPriceIterator, 
			Iterator<Entry<Long, PowerInterval>> activePowerLimitIterator,
			Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[] initialFeedInPower,
			Entry<Long, Double>[] initialFeedInPrice,
			Entry<Long, PowerInterval> initialFeedInLimit,
			int plsOptimizationObjective,
			double lowerOverLimitFactor,
			long startCalc,
			long endCalc) {
		
		int typeCount = feedInIterators.length;
		int nonNullCount = typeCount * 2 + 1; //typeCount times loads and prices and the powerlimit

		Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[] currentLoad = initialFeedInPower;
		Entry<Long, Double>[] currentPrice = initialFeedInPrice;
		Entry<Long, PowerInterval> currentLimit = initialFeedInLimit;
		
		Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[] nextLoadChange = (Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[]) new Entry<?, ?>[typeCount];
		Entry<Long, Double>[] nextPriceChange = (Entry<Long, Double>[] )new Entry<?, ?>[typeCount];
		
		Long[] nextLoadChangeKey = new Long[typeCount];
		Long[] nextPriceChangeKey = new Long[typeCount];
		
		for (int i = 0; i < typeCount; i++) {
			if (feedInIterators[i].hasNext()) {
				nextLoadChange[i] = feedInIterators[i].next();
				nextLoadChangeKey[i] = nextLoadChange[i].getKey();
			} else {
				nextLoadChange[i] = null;
				nextLoadChangeKey[i] = Long.MAX_VALUE;
				nonNullCount--;
			}
			
			if (feedInPriceIterator[i].hasNext()) {
				nextPriceChange[i] = feedInPriceIterator[i].next();
				nextPriceChangeKey[i] = nextPriceChange[i].getKey();
			} else {
				nextPriceChange[i] = null;
				nextPriceChangeKey[i] = Long.MAX_VALUE;
				nonNullCount--;
			}
		}
		
		Entry<Long, PowerInterval> nextLimitChange = plsOptimizationObjective == 0 ? null : (activePowerLimitIterator.hasNext() ? activePowerLimitIterator.next() : null);
		Long nextLimitChangeKey = nextLimitChange == null ? Long.MAX_VALUE : nextLimitChange.getKey();
		
		if (nextLimitChange == null) {
			nonNullCount--;
		}
		
		long currentTime = startCalc;
		double costs = 0;

		while (nonNullCount > 0) { //iterate over load changes			

			long minNextChange = nextLimitChangeKey;
			
			for (int i = 0; i < typeCount; i++) {
				minNextChange = Math.min(minNextChange, Math.min(nextLoadChangeKey[i], nextPriceChangeKey[i]));
			}

			if (minNextChange >= endCalc) {
				minNextChange = endCalc;
			}

			// get time factor (constant time in [s] and price is [cents/kWh], whereas load/power is in [W])
			double timeFactor = minNextChange - currentTime;
			
			double totalFeedInCosts = 0;
			int totalFeedInPower = 0;
			
			for (int i = 0; i < typeCount; i++) {
				int power = currentLoad[i].getValue().value;
				
				if (power < 0) {
					totalFeedInCosts += timeFactor * power * currentPrice[i].getValue();
					totalFeedInPower += power;
				}
			}
			
			costs += totalFeedInCosts;

			if (plsOptimizationObjective > 0) {
				double lowerLimit = currentLimit.getValue().getPowerLowerLimit();
				if (totalFeedInPower < lowerLimit)
					costs -= lowerOverLimitFactor * Math.abs(Math.abs(totalFeedInPower - lowerLimit) / totalFeedInPower) * totalFeedInCosts;
			}	
			
			for (int i = 0; i < typeCount; i++) {
				
				if (nextLoadChangeKey[i] <= minNextChange) {
					currentLoad[i] = nextLoadChange[i];
					if (feedInIterators[i].hasNext()) {
						nextLoadChange[i] = feedInIterators[i].next();
						nextLoadChangeKey[i] = nextLoadChange[i].getKey();
					} else {
						nextLoadChange[i] = null;
						nextLoadChangeKey[i] = Long.MAX_VALUE;
						nonNullCount--;
					}
				}
				
				if (nextPriceChangeKey[i] <= minNextChange) {
					currentPrice[i] = nextPriceChange[i];
					if (feedInPriceIterator[i].hasNext()) {
						nextPriceChange[i] = feedInPriceIterator[i].next();
						nextPriceChangeKey[i] = nextPriceChange[i].getKey();
					} else {
						nextPriceChange[i] = null;
						nextPriceChangeKey[i] = Long.MAX_VALUE;
						nonNullCount--;
					}
				}
			}
			
			if (nextLimitChangeKey <= minNextChange) {
				currentLimit = nextLimitChange;
				if (activePowerLimitIterator.hasNext()) {
					nextLimitChange = activePowerLimitIterator.next();
					nextLimitChangeKey = nextLimitChange.getKey();
				} else {
					nextLimitChange = null;
					nextLimitChangeKey = Long.MAX_VALUE;
					nonNullCount--;
				}
			}

			currentTime = minNextChange;			
		}
		
		if (currentTime < endCalc) {
			double timeFactor = endCalc - currentTime;

			double totalFeedInCosts = 0;
			int totalFeedInPower = 0;
			
			for (int i = 0; i < typeCount; i++) {
				int power = currentLoad[i].getValue().value;
				
				if (power < 0) {
					double price =  currentPrice[i].getValue();
					
					totalFeedInCosts += timeFactor * power * price;
					totalFeedInPower += power;
				}
			}
			
			costs += totalFeedInCosts;

			if (plsOptimizationObjective > 0) {
				double lowerLimit = currentLimit.getValue().getPowerLowerLimit();
				if (totalFeedInPower < lowerLimit)
					costs -= lowerOverLimitFactor * Math.abs(Math.abs(totalFeedInPower - lowerLimit) / totalFeedInPower) * totalFeedInCosts;
			}	
		}

		return costs;
	}

	@SuppressWarnings("unchecked")
	private static double calcAutoConsumptCosts(
			AncillaryCommodityLoadProfile ancillaryMeter,
			EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
			int epsOptimizationObjective,
			long startCalc,
			long endCalc
			) {
		AncillaryCommodity[] relevantAutoConsumptCommodities = epsOptimizationObjective == 4 
				? new AncillaryCommodity[]{AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION} 
				: new AncillaryCommodity[]{AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION};
				
		Iterator<Entry<Long, LoadProfile<AncillaryCommodity>.Tick>>[] autoConsIterators 
			= (Iterator<Entry<Long, LoadProfile<AncillaryCommodity>.Tick>>[]) new Iterator[relevantAutoConsumptCommodities.length];
		Iterator<Entry<Long, Double>>[] autoConsPriceIterator 
			= (Iterator<Entry<Long, Double>>[]) new Iterator[relevantAutoConsumptCommodities.length];
		Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[] initialAutoConsPower 
			= (Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[]) new Entry<?, ?>[relevantAutoConsumptCommodities.length];
		Entry<Long, Double>[] initialAutoConsPrice 
			= (Entry<Long, Double>[] )new Entry<?, ?>[relevantAutoConsumptCommodities.length];
		
		for (int i = 0; i < relevantAutoConsumptCommodities.length; i++) {
			autoConsIterators[i] = ancillaryMeter.getIteratorForSubMap(relevantAutoConsumptCommodities[i], startCalc, endCalc);
			initialAutoConsPower[i] = ancillaryMeter.getFloorEntry(relevantAutoConsumptCommodities[i], startCalc);
			
			autoConsPriceIterator[i] = priceSignals.get(relevantAutoConsumptCommodities[i]).getIteratorForSubMap(startCalc, endCalc);
			initialAutoConsPrice[i] = priceSignals.get(relevantAutoConsumptCommodities[i]).getFloorEntry(startCalc);
		}
		
		return calcAutoConsumptCosts(
				autoConsIterators, 
				autoConsPriceIterator, 
				initialAutoConsPower,
				initialAutoConsPrice,
				startCalc, 
				endCalc);
	}
	
	@SuppressWarnings("unchecked")
	private static double calcAutoConsumptCosts(
			Iterator<Entry<Long, LoadProfile<AncillaryCommodity>.Tick>>[] autoConsIterators, 
			Iterator<Entry<Long, Double>>[] autoConsPriceIterator, 
			Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[] initialAutoConsPower,
			Entry<Long, Double>[] initialAutoConsPrice,
			long startCalc,
			long endCalc) {
		
		int typeCount = autoConsIterators.length;
		int nonNullCount = typeCount * 2; //typeCount times loads and prices

		Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[] currentLoad = initialAutoConsPower;
		Entry<Long, Double>[] currentPrice = initialAutoConsPrice;
		
		Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[] nextLoadChange = (Entry<Long, LoadProfile<AncillaryCommodity>.Tick>[]) new Entry<?, ?>[typeCount];
		Entry<Long, Double>[] nextPriceChange = (Entry<Long, Double>[] )new Entry<?, ?>[typeCount];
		
		Long[] nextLoadChangeKey = new Long[typeCount];
		Long[] nextPriceChangeKey = new Long[typeCount];
		
		for (int i = 0; i < typeCount; i++) {
			if (autoConsIterators[i].hasNext()) {
				nextLoadChange[i] = autoConsIterators[i].next();
				nextLoadChangeKey[i] = nextLoadChange[i].getKey();
			} else {
				nextLoadChange[i] = null;
				nextLoadChangeKey[i] = Long.MAX_VALUE;
				nonNullCount--;
			}
			
			if (autoConsPriceIterator[i].hasNext()) {
				nextPriceChange[i] = autoConsPriceIterator[i].next();
				nextPriceChangeKey[i] = nextPriceChange[i].getKey();
			} else {
				nextPriceChange[i] = null;
				nextPriceChangeKey[i] = Long.MAX_VALUE;
				nonNullCount--;
			}
		}
		
		long currentTime = startCalc;
		double costs = 0;

		while (nonNullCount > 0) { //iterate over load changes			

			long minNextChange = Long.MAX_VALUE;
			
			for (int i = 0; i < typeCount; i++) {
				minNextChange = Math.min(minNextChange, Math.min(nextLoadChangeKey[i], nextPriceChangeKey[i]));
			}

			if (minNextChange >= endCalc) {
				minNextChange = endCalc;
			}

			// get time factor (constant time in [s] and price is [cents/kWh], whereas load/power is in [W])
			double timeFactor = minNextChange - currentTime;
			
			for (int i = 0; i < typeCount; i++) {
				int power = currentLoad[i].getValue().value;
				
				if (power < 0) {
					costs += timeFactor * power * currentPrice[i].getValue();
				}
			}
			
			for (int i = 0; i < typeCount; i++) {
				
				if (nextLoadChangeKey[i] <= minNextChange) {
					currentLoad[i] = nextLoadChange[i];
					if (autoConsIterators[i].hasNext()) {
						nextLoadChange[i] = autoConsIterators[i].next();
						nextLoadChangeKey[i] = nextLoadChange[i].getKey();
					} else {
						nextLoadChange[i] = null;
						nextLoadChangeKey[i] = Long.MAX_VALUE;
						nonNullCount--;
					}
				}
				
				if (nextPriceChangeKey[i] <= minNextChange) {
					currentPrice[i] = nextPriceChange[i];
					if (autoConsPriceIterator[i].hasNext()) {
						nextPriceChange[i] = autoConsPriceIterator[i].next();
						nextPriceChangeKey[i] = nextPriceChange[i].getKey();
					} else {
						nextPriceChange[i] = null;
						nextPriceChangeKey[i] = Long.MAX_VALUE;
						nonNullCount--;
					}
				}
			}

			currentTime = minNextChange;			
		}
		
		if (currentTime < endCalc) {
			double timeFactor = endCalc - currentTime;

			for (int i = 0; i < typeCount; i++) {
				int power = currentLoad[i].getValue().value;
				
				if (power < 0) {
					costs += timeFactor * power * currentPrice[i].getValue();
				}
			}
		}

		return costs;
	}

	private static double calcVarPower(SortedSet<Long> varPowerChanges, SortedSet<Long> varPriceChanges,
			SortedSet<Long> varLimitChanges, double upperOverLimitFactor, double lowerOverLimitFactor,
			long startCalc, long endCalc, AncillaryCommodityLoadProfile ancillaryMeter, PriceSignal varPriceSignal,
			PowerLimitSignal varLimit) {

		Long nextLoadChange = null;
		Long nextPriceChange = null;
		Long nextLimitChange = varLimitChanges == null ? Long.MAX_VALUE : null;

		Iterator<Long> loadCh = varPowerChanges.iterator();
		Iterator<Long> priceCh = varPriceChanges.iterator();
		Iterator<Long> limitCh = varLimitChanges == null ? null : varLimitChanges.iterator();

		long currentTime = startCalc;
		double costs = 0;

		while (currentTime < endCalc) { //iterate over load changes			

			//look for the next change in price or load
			if (nextLoadChange == null)
				nextLoadChange = loadCh.next();
			if (nextPriceChange == null)
				nextPriceChange = priceCh.next();
			if (nextLimitChange == null)
				nextLimitChange = limitCh.next();
			long minNextChange;

			if (nextLoadChange < nextPriceChange && nextLoadChange < nextLimitChange) {
				minNextChange = nextLoadChange;
				nextLoadChange = null;
			} else if (nextPriceChange < nextLoadChange && nextPriceChange < nextLimitChange) {
				minNextChange = nextPriceChange;
				nextPriceChange = null;
			} else if (nextLimitChange < nextLoadChange && nextLimitChange < nextPriceChange) {
				minNextChange = nextLimitChange;
				nextLimitChange = null;
			} else {
				//all are the same
				if (nextLoadChange == nextPriceChange && nextLoadChange == nextPriceChange) {
					minNextChange = nextLoadChange;
					nextLoadChange = null;
					nextPriceChange = null;
					nextLimitChange = limitCh == null ? Long.MAX_VALUE : null;
				//nextLoad == nextLimit < nextPrice
				} else if (nextLoadChange < nextPriceChange) {
					minNextChange = nextLoadChange;
					nextLoadChange = null;
					nextLimitChange = limitCh == null ? Long.MAX_VALUE : null;
				//nextPrice == nextLimit < nextLoad
				} else if (nextPriceChange < nextLoadChange) {
					minNextChange = nextPriceChange;
					nextPriceChange = null;
					nextLimitChange = limitCh == null ? Long.MAX_VALUE : null;
				//nextPrice == nextLoad < nextLimit
				} else {
					minNextChange = nextLoadChange;
					nextLoadChange = null;
					nextPriceChange = null;
				}
			}

			if (minNextChange >= endCalc) {
				minNextChange = endCalc;
			}

			// get time factor (constant time in [s] and price is [cents/kWh], whereas load/power is in [W])
			double timeFactor = minNextChange - currentTime;
			int power = ancillaryMeter.getLoadAt(AncillaryCommodity.REACTIVEPOWEREXTERNAL, currentTime);
			double price = varPriceSignal.getPrice(currentTime);

			costs += timeFactor * Math.abs(power) * price; 

			if (varLimitChanges != null) {
				double upperLimit = varLimit.getPowerLimitInterval(currentTime).getPowerUpperLimit();
				double lowerLimit = varLimit.getPowerLimitInterval(currentTime).getPowerLowerLimit();

				if (power > upperLimit) {
					costs += timeFactor * upperOverLimitFactor * Math.abs(power - upperLimit) * price;
				} else if (power < lowerLimit) {
					costs += timeFactor * lowerOverLimitFactor * Math.abs(power - lowerLimit) * price;
				}
			}			

			currentTime = minNextChange;			
		}

		return costs;

	}
}
