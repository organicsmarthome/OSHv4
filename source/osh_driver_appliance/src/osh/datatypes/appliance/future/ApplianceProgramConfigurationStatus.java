package osh.datatypes.appliance.future;

import java.io.Serializable;
import java.util.UUID;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.ILoadProfile;
import osh.datatypes.power.SparseLoadProfile;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ApplianceProgramConfigurationStatus implements Cloneable, Serializable {
	
	private static final long serialVersionUID = -2752978492500340593L;

	private long acpReferenceTime;

	/** ID for every update (with changes) */
	private UUID acpID;
	
	/**
	 * Dynamic Profiles using relative times (!)<br>
	 * dim 0: profiles<br>
	 * dim 1: segments<br>
	 */
	private SparseLoadProfile[][] dynamicLoadProfiles;
	
	private int[][][] minMaxDurations;
	
	private boolean doNotReschedule = false;
	
	
	public ApplianceProgramConfigurationStatus(
			UUID acpID,
			SparseLoadProfile[][] dynamicLoadProfiles,
			int[][][] minMaxDurations,
			long acpReferenceTime) {
		
		this.acpID = acpID;
		this.dynamicLoadProfiles = dynamicLoadProfiles;
		this.minMaxDurations = minMaxDurations;
		this.acpReferenceTime = acpReferenceTime;
	}
	
	public ApplianceProgramConfigurationStatus(
			UUID acpID,
			SparseLoadProfile[][] dynamicLoadProfiles,
			int[][][] minMaxTimes,
			long acpReferenceTime,
			boolean doNotReschedule) {
		
		this.acpID = acpID;
		this.dynamicLoadProfiles = dynamicLoadProfiles;
		this.minMaxDurations = minMaxTimes;
		this.acpReferenceTime = acpReferenceTime;
		this.doNotReschedule = doNotReschedule;
	}
	

	public UUID getAcpID() {
		return acpID;
	}
	
	public SparseLoadProfile[][] getDynamicLoadProfiles() {
		return dynamicLoadProfiles;
	}
	
	public int[][][] getMinMaxDurations() {
		return minMaxDurations;
	}
	
	public ILoadProfile<Commodity> getFinishedProfile(int profileNo) {
		return dynamicLoadProfiles[profileNo][dynamicLoadProfiles[profileNo].length - 1];
	}
	
	public long getAcpReferenceTime() {
		return acpReferenceTime;
	}

	public boolean isDoNotReschedule() {
		return doNotReschedule;
	}

	public void setDoNotReschedule(boolean doNotReschedule) {
		this.doNotReschedule = doNotReschedule;
	}
	
	
	// HELPER METHODS
	
	/**
	 * @return Maximum duration (longest profile, with minimum times)
	 */
	public static long getTotalMaxDuration(ApplianceProgramConfigurationStatus acp) {
		long val = 0;
		for (int d0 = 0; d0 < acp.dynamicLoadProfiles.length; d0++) {
			long temp = 0;
			for (int d1 = 0; d1 < acp.dynamicLoadProfiles[d0].length; d1++) {
				//OLD: use length of phases
//				temp = temp + acp.dynamicLoadProfiles[d0][d1].getEndingTimeOfProfile();
				//NEW: use minimum times
				temp = temp + acp.getMinMaxDurations()[d0][d1][0];
			}
			val = Math.max(val, temp);
		}
		return val;
	}
	
	@Override
	public Object clone() {
		int[][][] clonedMinMaxTimes = new int[minMaxDurations.length][][];
		for (int d0 = 0; d0 < minMaxDurations.length; d0++) {
			clonedMinMaxTimes[d0] = new int[minMaxDurations[d0].length][];
			for (int d1 = 0; d1 < minMaxDurations[d0].length; d1++) {
				clonedMinMaxTimes[d0][d1] = new int[minMaxDurations[d0][d1].length];
				for (int d2 = 0; d2 < minMaxDurations[d0][d1].length; d2++) {
					clonedMinMaxTimes[d0][d1][d2] = minMaxDurations[d0][d1][d2];
				}
			}
		}
		ApplianceProgramConfigurationStatus clonedAcp = new ApplianceProgramConfigurationStatus(acpID, dynamicLoadProfiles, clonedMinMaxTimes, acpReferenceTime, doNotReschedule);
		return clonedAcp;
	}

}
