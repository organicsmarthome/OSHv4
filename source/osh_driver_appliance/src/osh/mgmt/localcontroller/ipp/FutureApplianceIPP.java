package osh.mgmt.localcontroller.ipp;

import java.util.BitSet;
import java.util.HashSet;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.ILoadProfile;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.esc.LimitedCommodityStateMap;
import osh.utils.BitSetConverter;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class FutureApplianceIPP 
					extends ControllableIPP<ISolution, IPrediction> {
	
	
	private static final long serialVersionUID = 3070293649618474988L;
	
	// NOTE: tDoF = latestStartingTime - earliestStartingTime
	
	/** earliest starting time for the device */
	private long earliestStartingTime; // to be update when rescheduled
	/** latest starting time for the device */
	private long latestStartingTime;
	
	private double cervisiaDofUsedFactor = 0.01;
	
	private ApplianceProgramConfigurationStatus acp;
	private SparseLoadProfile[][] compressedDLProfiles;
	private SparseLoadProfile[] initializedLoadProfiles;
	private long[] initializedStartingTimes;
	
	/**
	 * [0] = # bits for alternative profiles (gray encoded binary string)<br>
	 * [1] = # bits for tDoF (gray encoded binary string)<br>
	 * [2] = max # of phases
	 */
	private int[] header;
	
	/**
	 * max value of every phase (depending on tDoF and minMaxValues)<br>
	 * dim0 : profile<br>
	 * dim1 : phase<br>
	 */
	private int[][] maxValues; // to be updated when rescheduled
	
	/**
	 * sum of dim0 of maxValues
	 */
	private int[] sumOfAllMaxValues; // to be updated when rescheduled
	
	
	// ### IPP STUFF ###
	/** used for iteration in interdependent calculation */
	private long interdependentTime;
	
	private SparseLoadProfile lp = null;
	
	private double cervisia = 0;
	
	private Commodity[] usedCommodities;
	private int lastUsedIndex;
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected FutureApplianceIPP() {
		super();
	}	
	
	/**
	 * CONSTRUCTOR
	 */
	public FutureApplianceIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long timestamp, 
			int bitcount, 
			boolean toBeScheduled,
			long optimizationHorizon, 
			DeviceTypes deviceType, 
			long referenceTime,
			long earliestStartingTime, 
			long latestStartingTime,
			ApplianceProgramConfigurationStatus acp,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		
		super(
				deviceId, 
				logger, 
				timestamp, 
				bitcount, 
				toBeScheduled, 
				false, //does not need ancillary meter state as Input State
				false, //does not react to input states
				optimizationHorizon, 
				referenceTime, 
				deviceType,
				new Commodity[]{}, //we will calculate this and set in our constructor so this is a dummy value
				compressionType,
				compressionValue);
		
		this.earliestStartingTime = earliestStartingTime;
		this.latestStartingTime = latestStartingTime;
		
		if (acp != null) {
			this.acp = (ApplianceProgramConfigurationStatus) acp.clone();
		}
		
		this.compressedDLProfiles = SparseLoadProfile.getCompressedProfile(compressionType, this.acp.getDynamicLoadProfiles(), compressionValue, compressionValue);
		
		/*
		 * here we check which commodities are existant in the load profiles, so we can have better priming of our ESC
		 */
		HashSet<Commodity> tempUsedComm = new HashSet<Commodity>();
		for (int i = 0; i < compressedDLProfiles.length; i++) {
			for (int j = 0; j < compressedDLProfiles[i].length; j++) {
				if (!tempUsedComm.contains(Commodity.ACTIVEPOWER) && 
						compressedDLProfiles[i][j].getFloorEntry(Commodity.ACTIVEPOWER, referenceTime) != null) {
					tempUsedComm.add(Commodity.ACTIVEPOWER);
				}
				if (!tempUsedComm.contains(Commodity.REACTIVEPOWER) &&
						compressedDLProfiles[i][j].getFloorEntry(Commodity.REACTIVEPOWER, referenceTime) != null) {
					tempUsedComm.add(Commodity.REACTIVEPOWER);
				}
				if (!tempUsedComm.contains(Commodity.HEATINGHOTWATERPOWER) &&
						compressedDLProfiles[i][j].getFloorEntry(Commodity.HEATINGHOTWATERPOWER, referenceTime) != null) {
					tempUsedComm.add(Commodity.HEATINGHOTWATERPOWER);
				}
				if (!tempUsedComm.contains(Commodity.NATURALGASPOWER) &&
						compressedDLProfiles[i][j].getFloorEntry(Commodity.NATURALGASPOWER, referenceTime) != null) {
					tempUsedComm.add(Commodity.NATURALGASPOWER);
				}
			}
		}
		
		this.allOutputCommodities = new Commodity[tempUsedComm.size()];
		this.allOutputCommodities = tempUsedComm.toArray(allOutputCommodities);
		
		// recalculate partition of solution ("header")
		this.header = calculateHeader(
						earliestStartingTime, 
						latestStartingTime, 
						acp);
		
		int[][][] minMaxTimes = acp.getMinMaxDurations();
		
//		getGlobalLogger().logDebug("earliestStartingTime=" + earliestStartingTime);
//		getGlobalLogger().logDebug("latestStartingTime" + latestStartingTime);
		
		// recalculate max values (max lengths of phases)
		this.maxValues = calculateMaxValuesArray(
				earliestStartingTime, 
				latestStartingTime, 
				minMaxTimes);
		
		// calculate sum of all max values (for determination of how to distribute tdof)
		this.sumOfAllMaxValues = new int[maxValues.length];
		for (int i = 0; i < maxValues.length; i++) {
			sumOfAllMaxValues[i] = 0;
			for (int j = 0; j < maxValues[i].length; j++) {
				sumOfAllMaxValues[i] = sumOfAllMaxValues[i] + maxValues[i][j];
//				getGlobalLogger().logDebug("maxValues["+i+"]["+j+"]" + maxValues[i][j]);
//				getGlobalLogger().logDebug("sumOfAllMaxValues[" + i + "]=" + sumOfAllMaxValues[i]);
			}
		}
	}

	
	@Override
	public void initializeInterdependentCalculation(
			long maxReferenceTime,
			BitSet solution,
			int stepSize,
			boolean createLoadProfile,
			boolean keepPrediction) {
		
		this.stepSize = stepSize;
		
		// INFO: maxReferenceTime = starting point of interdependent calculation
				
		// TYPICAL OLD STUFF
		this.cervisia = 0;
		this.interdependentTime = maxReferenceTime;
		setOutputStates(null);
		
		// build final load profile
//		SparseLoadProfile returnProfile = new SparseLoadProfile();
		
		// get eDoF values
		int selectedProfile = 
				getSelectedProfileFromSolution(
						solution,
						header,
						acp.getDynamicLoadProfiles());
		
		// tDoF has been distributed to available phases
		long availableTDoF = latestStartingTime - earliestStartingTime;
		int[] selectedTDOF = 
				getSelectedTimeFromTDOFFromSolution(
						solution,
						header,
						availableTDoF,
						maxValues[selectedProfile],
						sumOfAllMaxValues[selectedProfile]);
		
		if (selectedTDOF.length > 0 && availableTDoF > 0) {
			if (selectedTDOF[0] > 0) {			
				//starting later is better
				cervisia -= (((double) selectedTDOF[0]) / ((double) availableTDoF)) * cervisiaDofUsedFactor;
			}
		}			
		
//		getGlobalLogger().logDebug("availableTDoF=" +  availableTDoF);
//		getGlobalLogger().logDebug(maxValues[selectedProfile][0] + " . " + maxValues[selectedProfile][1] + " . " + maxValues[selectedProfile][2]); 
//		getGlobalLogger().logDebug(selectedTDOF[0] + " . " + selectedTDOF[1] + " . " + selectedTDOF[2]); 
		
		// get stuff of the selected profile
//		SparseLoadProfile[] selectedDlp = acp.getDynamicLoadProfiles()[selectedProfile];
		SparseLoadProfile[] selectedDlp = this.compressedDLProfiles[selectedProfile];
		initializedLoadProfiles = new SparseLoadProfile[selectedDlp.length];
		
		
		int[][] selectedMinMaxTimes = acp.getMinMaxDurations()[selectedProfile];
		
		// convert to selected starting times
		long[] selectedStartingTimes = getStartingTimes(
				acp.getAcpReferenceTime(),
				selectedTDOF,
				selectedMinMaxTimes);
		
		initializedStartingTimes = new long[selectedStartingTimes.length + 1];
		for (int i = 0; i < selectedStartingTimes.length; i++) {
			initializedStartingTimes[i] = selectedStartingTimes[i];
		}
		initializedStartingTimes[selectedStartingTimes.length] = Long.MAX_VALUE;
		
		// merge phases to profile
		for (int i = 0; i < selectedMinMaxTimes.length; i++) {
			try {
				long availableLength = selectedDlp[i].getEndingTimeOfProfile(); // is has relative times
				if (availableLength == selectedMinMaxTimes[i][0] + selectedTDOF[i]) {
					// shortcut
//					returnProfile = (SparseLoadProfile) returnProfile.merge(
//							selectedDlp[i], 
//							selectedStartingTimes[i]);
					initializedLoadProfiles[i] = selectedDlp[i];
				}
				else {
					// profile has to be stripped-down or enlarged...
					if (availableLength > selectedMinMaxTimes[i][0] + selectedTDOF[i]) {
						// strip-down...(shorten)...
						SparseLoadProfile tempLP = selectedDlp[i].clone();
						tempLP.setEndingTimeOfProfile(selectedMinMaxTimes[i][0] - selectedTDOF[i]);
						initializedLoadProfiles[i] = tempLP;
//						returnProfile = returnProfile.merge(
//								tempLP,
//								selectedStartingTimes[i]);
					}
					else {
						// enlarge...
						int number = (int) ((selectedMinMaxTimes[i][0] + selectedTDOF[i]) / availableLength);
						
						if (selectedDlp[i].getEndingTimeOfProfile() == 1) {
							SparseLoadProfile longerProfile = (SparseLoadProfile) selectedDlp[i].clone();
							longerProfile.setEndingTimeOfProfile(number); 
							initializedLoadProfiles[i] = longerProfile;
//							returnProfile = returnProfile.merge(
//									longerProfile,
//									selectedStartingTimes[i]);
						}
						else {
							SparseLoadProfile lengthened = selectedDlp[i].clone();
							for (int j = 1; j < number; j++) {
								lengthened = lengthened.merge(
										selectedDlp[i], 
										selectedStartingTimes[i] + j * availableLength);
							}
							// n-times the full profile
//							for (int j = 0; j < number; j++) {
//								returnProfile = returnProfile.merge(
//										selectedDlp[i], 
//										selectedStartingTimes[i] + j * availableLength);
//							}
							int remainingPartial = (int) ((selectedMinMaxTimes[i][0] + selectedTDOF[i]) % availableLength);
							// plus partial stripped-down profile...
							SparseLoadProfile tempLP = selectedDlp[i].clone();
							tempLP.setEndingTimeOfProfile(remainingPartial);
							lengthened = lengthened.merge(
									tempLP,
									selectedStartingTimes[i] + number * availableLength);
							initializedLoadProfiles[i] = lengthened;
//							returnProfile = returnProfile.merge(
//									tempLP,
//									selectedStartingTimes[i]  + number * availableLength);
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		this.lp = returnProfile;
		this.lp = new SparseLoadProfile();
		
		HashSet<Commodity> tempUsedComm = new HashSet<Commodity>(4);
		
		for (int i = 0; i < initializedLoadProfiles.length; i++) {
			for (Commodity c : allOutputCommodities) {
				if (!tempUsedComm.contains(c) && 
						initializedLoadProfiles[i].getFloorEntry(c, maxReferenceTime) != null) {
					tempUsedComm.add(c);
				}
			}
			
			long relativeStart = Math.abs(Math.min(initializedStartingTimes[i] - maxReferenceTime, 0));
			initializedLoadProfiles[i].initSequentialAverageLoad(relativeStart);
		}
		
		usedCommodities = new Commodity[tempUsedComm.size()];
		usedCommodities = tempUsedComm.toArray(usedCommodities);
		
		this.internalInterdependentOutputStates = new LimitedCommodityStateMap(usedCommodities);
		
		lastUsedIndex = 0;
		
//		lp.initSequentialAverageLoad(maxReferenceTime);
		
	}
	
	/*
	 * the method for sequential averages in our load profiles uses entrys and iterators which 
	 * cannot be serialised, so they have to be destroyed prior to deep copying
	 */
	@Override
	public void prepareForDeepCopy() {
		if (lp != null) {
			lp.removeSequentialPriming();
		}
		if (initializedLoadProfiles != null) {
			for (int i = 0; i < initializedLoadProfiles.length; i++) {
				initializedLoadProfiles[i].removeSequentialPriming();
			}
		}
	}
	

	@Override
	public void calculateNextStep() {
		// no next step...
		// ...but give power		
		
		long end = interdependentTime + stepSize;
		
		int index = lastUsedIndex;
		long currentTime = interdependentTime;
		
		double[] powers = new double[usedCommodities.length];
		
		boolean hasValues = false;
		
		//all power values in one profile
		if (end < initializedStartingTimes[index]) {
			long subtractionFactor = initializedStartingTimes[index - 1];
			for (int j = 0; j < usedCommodities.length; j++) {
				powers[j] = Math.round(initializedLoadProfiles[index - 1]
						.getAverageLoadFromTillSequentialNotRounded(usedCommodities[j], (currentTime - subtractionFactor), (end - subtractionFactor)));
				
				if (powers[j] != 0) {
					internalInterdependentOutputStates.setPower(usedCommodities[j], powers[j]);
					hasValues = true;
				} else {
					internalInterdependentOutputStates.resetCommodity(usedCommodities[j]);
				}
			}
		} 
		//power values in multiple profiles, iterate
		else {
			while (currentTime < end) {
				
				if (currentTime > initializedStartingTimes[index]) {
					index++;
					lastUsedIndex++;
				} else {
					
					long currentEnd = Math.min(initializedStartingTimes[index], end);
					double factor = (currentEnd - currentTime);
					long subtractionFactor = initializedStartingTimes[index - 1];
					
					for (int j = 0; j < usedCommodities.length; j++) {
						powers[j] += (initializedLoadProfiles[index - 1]
								.getAverageLoadFromTillSequentialNotRounded(usedCommodities[j], (currentTime - subtractionFactor), (currentEnd - subtractionFactor)) * factor);					
					}
					
					currentTime = initializedStartingTimes[index];
					index++;
				}
			}
			
			for (int j = 0; j < usedCommodities.length; j++) {
				powers[j] = Math.round(powers[j] / ((double) stepSize));
				if (powers[j] != 0) {
					internalInterdependentOutputStates.setPower(usedCommodities[j], powers[j]);
					hasValues = true;
				} else {
					internalInterdependentOutputStates.resetCommodity(usedCommodities[j]);
				}
			}
		}
		
		if (hasValues) {
			setOutputStates(internalInterdependentOutputStates);
		} else {
			setOutputStates(null);
		}
		
		this.interdependentTime += stepSize;
	}
	

	@Override
	public Schedule getFinalInterdependentSchedule() {
		// IMPORTANT: cervisia currently not in use and unchecked
		
		//we dont merge the phases anymore (it's faster that way) so for a schedule we have to do it here now
		if (initializedLoadProfiles != null) {
			this.lp = new SparseLoadProfile();
	 		for (int i = 0; i < initializedLoadProfiles.length; i++) {
				lp.merge(initializedLoadProfiles[i], initializedStartingTimes[i]);
			}
		}
		
		return new Schedule(this.lp, cervisia, this.getDeviceType().toString());
	}

	
	@Override
	public ISolution transformToPhenotype(BitSet solution) {
		int selectedProfile = getSelectedProfileFromSolution(
				solution,
				header,
				acp.getDynamicLoadProfiles());
		long availableTDoF = latestStartingTime - earliestStartingTime;
		int[] selectedTimeOfTDOF = getSelectedTimeFromTDOFFromSolution(
				solution,
				header,
				availableTDoF,
				maxValues[selectedProfile],
				sumOfAllMaxValues[selectedProfile]);
		return new GenericApplianceSolution(
				acp.getAcpID(), 
				getStartingTimes(
						earliestStartingTime, 
						selectedTimeOfTDOF, 
						acp.getMinMaxDurations()[selectedProfile]), 
				selectedProfile);
	}
	

	@Override
	public ISolution transformToFinalInterdependetPhenotype(
			BitSet solution) {
		int selectedProfile = getSelectedProfileFromSolution(
				solution,
				header,
				acp.getDynamicLoadProfiles());
		long availableTDoF = latestStartingTime - earliestStartingTime;
		int[] selectedTimeOfTDOF = getSelectedTimeFromTDOFFromSolution(
				solution,
				header,
				availableTDoF,
				maxValues[selectedProfile],
				sumOfAllMaxValues[selectedProfile]);
		
		return new GenericApplianceSolution(
				acp.getAcpID(), 
				getStartingTimes(
						acp.getAcpReferenceTime(), 
						selectedTimeOfTDOF, 
						acp.getMinMaxDurations()[selectedProfile]), 
				selectedProfile);
	}
	
	
	// ### HEADER CALCULATION ###
	
	/**
	 * [0] = # of profiles<br>
	 * [1] = tDoF in Bits (gray encoded binary string)<br>
	 * [2] = max # of phases
	 */
	private static int[] calculateHeader(
			long earliestStarttime, 
			long latestStarttime, 
			ApplianceProgramConfigurationStatus acp) {
		int[] header = new int[3];
		header[0] = getProfilesBitCount(
							acp);
		header[1] = getTDOFLengthBitCount(
							earliestStarttime, 
							latestStarttime);
		header[2] = getMaxNumberOfPhases(
							acp);
		return header;
	}
	
	private static int getProfilesBitCount(
			ApplianceProgramConfigurationStatus acp) {
		
		int length = acp.getDynamicLoadProfiles().length;
		
		// only one profile available -> no DoF
		if (length < 2) {
			return 0;
		}
		else {
			return (int) Math.ceil(Math.log(length) / Math.log(2));
		}
	}
	
	/**
	 * Necessary bits for tDoF (bits per pause)
	 */
	private static int getTDOFLengthBitCount(
			long earliestStarttime, 
			long latestStarttime) {
		if (earliestStarttime > latestStarttime) {
			return 0;
		}
		return (int) Math.ceil(Math.log(latestStarttime - earliestStarttime + 1) / Math.log(2));
	}
	
	/**
	 * Number of phases (maximum of all profiles)
	 */
	private static int getMaxNumberOfPhases(
			ApplianceProgramConfigurationStatus acp) {
		int max = 0;
		for (int i = 0; i < acp.getDynamicLoadProfiles().length; i++) {
			max = Math.max(max, acp.getDynamicLoadProfiles()[i].length);
		}
		return max;
	}
	
	
	// ### CALCULATE BIT COUNT ###
	
	/**
	 * returns the needed amount of bits for the EA
	 * @param earliestStarttime
	 * @param latestStarttime
	 */
	public static int calculateBitCount(
			long earliestStarttime,
			long latestStarttime,
			ApplianceProgramConfigurationStatus acp) {
		/**
		 * header (3-dim)<br>
		 * [0] = # bits for alternative profiles (gray encoded binary string)<br>
		 * [1] = # bits for tDoF (gray encoded binary string)<br>
		 * [2] = max # of phases
		 */
		int[] header = calculateHeader(
				earliestStarttime, 
				latestStarttime, 
				acp);
		int bitCount = header[0] + header[1] * header[2];

		return bitCount;
	}
	
	
	// ### INTERPRET SOLUTION ###
	
	/**
	 * Get selected tDoF times for phases<br>
	 * [...] = time in ticks
	 */
	private static int[] getSelectedTimeFromTDOFFromSolution(
			BitSet solution,
			int[] header,
			long availableTDoF,
			int[] maxValues,
			int sumOfAllMaxValues) {
		
		/*
		 * header (3-dim)<br>
		 * [0] = # bits for alternative profiles (gray encoded binary string)<br>
		 * [1] = # bits for tDoF (gray encoded binary string)<br>
		 * [2] = max # of phases
		 */

		int noProfileBits = header[0];
		int noTDOFBits = header[1];
		int maxNoOfPhases = header[2];
		
		// is there a tDoF?
		if ( maxNoOfPhases > 0 ) {
			// calculate sum of total bit value of all tDoF partitions bit values
			int sumOfAllBitValues = 0;
			int currentBit = noProfileBits;
			for (int i = 0; i < maxNoOfPhases; i++) {
				BitSet subset = solution.get(currentBit, currentBit + noTDOFBits);
				if ( maxValues[i] > 0 ) {
					sumOfAllBitValues = (int) (sumOfAllBitValues + BitSetConverter.gray2long(subset));
				}
				currentBit = currentBit + noTDOFBits;
			}
			
			// calculate partition
			int[] returnValue = new int[maxNoOfPhases];
			
			currentBit = noProfileBits;
			for ( int i = 0; i < maxNoOfPhases; i++ ) {
				BitSet subset = solution.get(currentBit, currentBit + noTDOFBits);
				// IMPORTANT: remember the following!
				// IMPORTANT: this could lead to rounding errors -> program may end sooner than expected
				// IMPORTANT: keep cast to double (totalBitValue)
//				int value = Math.min(
//						(int) (((double) gray2long(subset) * availableTDoF) / (double) sumOfAllBitValues),
//						maxValues[i]);
				
				int firstVal = (int) BitSetConverter.gray2long(subset);
				//TODO minValues[]
				int value = (int) Math.min(Math.round(((double) (firstVal * availableTDoF)) / ((double) sumOfAllBitValues)), maxValues[i]);
				returnValue[i] = value;
				// move to next bunch of bits...
				currentBit = currentBit + noTDOFBits;
			}
			
			return returnValue;
		}
		else {
			return null;
		}
	}
	
	
	// HELPER METHODS
	
	/**
	 * [0] = starting time of phase 0
	 * conversion: starting time of phase + phase time + selected tDoF -> starting time of next phase
	 */
	private static long[] getStartingTimes(
			long referenceTime,
			int[] selectedTimeOfTDOF,
			int[][] selectedMinMaxTimes) {
		// allocate array
		long[] selectedStartTimes = new long[selectedTimeOfTDOF.length]; 
		// calculate starting times for phases
		long endTimeOfPreviousPhase = referenceTime;
		for (int i = 0; i < selectedStartTimes.length; i++) {
			// calculate start time for this phase
			selectedStartTimes[i] = endTimeOfPreviousPhase;
					
			// calculate end time for this phase (for next iteration) with minimum length of phase plus selected tDoF
			endTimeOfPreviousPhase = endTimeOfPreviousPhase + selectedMinMaxTimes[i][0] + selectedTimeOfTDOF[i];
		}
		return selectedStartTimes;
	}
	
	//TODo: find better solution (for mapping from 2^x to real length)
	/**
	 * returns [NUMBER]
	 */
	private static int getSelectedProfileFromSolution(
			BitSet solution,
			int[] header,
			ILoadProfile<Commodity>[][] dlp) {
		// there is only one profile that can be selected
		if (header[0] == 0) {
			return 0;
		}
		int profilesBits = header[0];
		BitSet subset = solution.get(0, profilesBits);
		return (int) Math.floor(BitSetConverter.gray2long(subset) / Math.pow(2, profilesBits) * dlp.length);
	}
	
	private static int[][] calculateMaxValuesArray(
			long earliestStartingTime,
			long latestStartingTime,
			int[][][] minMaxTimes) {
		// get available tdof
		int tdof = (int) (latestStartingTime - earliestStartingTime);
		int[][] maxValues = new int[minMaxTimes.length][];
		for (int d0 = 0; d0 < minMaxTimes.length; d0++) {
			maxValues[d0] = new int[minMaxTimes[d0].length];
			for (int d1 = 0; d1 < minMaxTimes[d0].length; d1++) {
				// max time from a to b = min(tdof and max - min length)
				maxValues[d0][d1] = Math.min(
						minMaxTimes[d0][d1][1] - minMaxTimes[d0][d1][0], 
						tdof);
			}
		}
		return maxValues;
	}
	
	// MISC
	
	@Override
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		// RECALCULATE
		// (from recalculate encoding)
		// earliest starting time has been reached...
		
		this.setReferenceTime(currentTime);
		
		if (earliestStartingTime < currentTime) {
			
			// shorten tDoF
			
			// if time is running out...
			if (currentTime > latestStartingTime) {
				// tDoF = 0;
				earliestStartingTime = latestStartingTime;
			} 
			else {
				// adjust minMaxValues in ACP
				int[][][] minMaxTimes = acp.getMinMaxDurations();
				for (int i = 0; i < minMaxTimes.length; i++) {
					minMaxTimes[i][0][0] = (int) (minMaxTimes[i][0][0] + (currentTime - earliestStartingTime));
				}
				
				// adjust time frame from optimization
				earliestStartingTime = currentTime;
			}
					
			this.setBitCount(
					calculateBitCount(
							earliestStartingTime, 
							latestStartingTime, 
							acp));
			this.header = calculateHeader(
							earliestStartingTime, 
							latestStartingTime, 
							acp);
			this.maxValues = calculateMaxValuesArray(
					earliestStartingTime, 
					latestStartingTime, 
					acp.getMinMaxDurations());
			// calculate sum of all max values (for determination of how to distribute tdof)
			this.sumOfAllMaxValues = new int[maxValues.length];
			for (int i = 0; i < maxValues.length; i++) {
				sumOfAllMaxValues[i] = 0;
				for (int j = 0; j < maxValues[i].length; j++) {
					sumOfAllMaxValues[i] = sumOfAllMaxValues[i] + maxValues[i][j];
				}
			}
		}
	}
	
	// ### to string ###
	
	@Override
	public String problemToString() {
		return "[" + getReferenceTime() + "] [" + getOptimizationHorizon() + "] FutureApplianceIPP : EST=" + earliestStartingTime + " LST=" + latestStartingTime;// + " minMaxValues=" + StringToArray.arrayToString(array);
	}

	@Override
	public String solutionToString(BitSet bits) {
		return "FutureApplianceIPP solution";
	}

}
