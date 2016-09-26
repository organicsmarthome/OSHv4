package osh.datatypes.registry.driver.details.appliance;

import java.util.UUID;



import osh.datatypes.registry.StateExchange;


/**
 * StateExchange for communication of DoF details
 * (from device)
 * @author Ingo Mauser
 *
 */

public class GenericApplianceDofDriverDetails extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1441502884455073409L;

	/** DoF for initial scheduling */
	protected int appliance1stDof = 0;
	
	/** DoF for rescheduling */
	protected int appliance2ndDof = 0;
	
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private GenericApplianceDofDriverDetails() {
		this(null, 0);
	};


	/**
	 * CONSTRUCTOR
	 */
	public GenericApplianceDofDriverDetails(
			UUID sender, 
			long timestamp) {
		super(sender, timestamp);
	}


	public int getAppliance1stDof() {
		return appliance1stDof;
	}

	public void setAppliance1stDof(int appliance1stDof) {
		this.appliance1stDof = appliance1stDof;
	}

	public int getAppliance2ndDof() {
		return appliance2ndDof;
	}

	public void setAppliance2ndDof(int appliance2ndDof) {
		this.appliance2ndDof = appliance2ndDof;
	}
	
}
