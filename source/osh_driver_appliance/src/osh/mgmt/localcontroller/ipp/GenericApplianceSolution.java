package osh.mgmt.localcontroller.ipp;

import java.util.Arrays;
import java.util.UUID;

import osh.datatypes.ea.interfaces.ISolution;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class GenericApplianceSolution implements ISolution {
	
	public UUID acpUUID;
	public long[] startingTimes;
	public int profileId;
	
	
	/**
	 * CONSTRUCTOR
	 * @param startTime
	 * @param isPredicted
	 */
	public GenericApplianceSolution(
			UUID acpUUID, 
			long[] startingTimes, 
			int profileId) {
		super();
		
		this.acpUUID = acpUUID;
		this.startingTimes = startingTimes;
		this.profileId = profileId;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((acpUUID == null) ? 0 : acpUUID.hashCode());
		result = prime * result + profileId;
		result = prime * result + Arrays.hashCode(startingTimes);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericApplianceSolution other = (GenericApplianceSolution) obj;
		if (acpUUID == null) {
			if (other.acpUUID != null)
				return false;
		} else if (!acpUUID.equals(other.acpUUID))
			return false;
		if (profileId != other.profileId)
			return false;
		if (!Arrays.equals(startingTimes, other.startingTimes))
			return false;
		return true;
	}
	
	
	@Override
	public GenericApplianceSolution clone() throws CloneNotSupportedException {
		long[] startingTimes = new long[this.startingTimes.length];
		for (int i = 0; i < this.startingTimes.length; i++) {
			startingTimes[i] = this.startingTimes[i];
		}
		
		GenericApplianceSolution clonedSolution = new GenericApplianceSolution(
				this.acpUUID, 
				startingTimes, 
				this.profileId);
		return clonedSolution;
	}
	
	
	@Override
	public String toString() {
		String pausesString = "[";
		if (startingTimes != null) {
			for (int i = 0; i < startingTimes.length; i++) {
				if (i > 0) {
					pausesString = pausesString + ",";
				}
				pausesString = pausesString + startingTimes[i];
			}
		}
		pausesString = pausesString + "]";
		return "referenceTime=" + acpUUID 
				+ " | profileId=" + profileId
				+ " | pauses=" + pausesString;
	}
}