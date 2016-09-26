package osh.mgmt.localobserver.ipp;

import osh.datatypes.ea.interfaces.ISolution;


/**
 * 
 * @author Florian Allerding, Till Schuberth
 *
 */

public class MieleSolution implements ISolution {
	
	public long startTime;
	public boolean isPredicted;
	
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private MieleSolution() {}
	
	/**
	 * CONSTRUCTOR
	 * @param startTime
	 * @param isPredicted
	 */
	public MieleSolution(long startTime, boolean isPredicted) {
		super();
		
		this.startTime = startTime;
		this.isPredicted = isPredicted;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isPredicted ? 1231 : 1237);
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
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
		MieleSolution other = (MieleSolution) obj;
		if (isPredicted != other.isPredicted)
			return false;
		if (startTime != other.startTime)
			return false;
		return true;
	}
	
	@Override
	public MieleSolution clone() throws CloneNotSupportedException {
		MieleSolution clonedSolution = new MieleSolution(this.startTime, this.isPredicted);
		return clonedSolution;
	}

}