package osh.esc.grid.carrier;

import java.io.Serializable;

import osh.datatypes.commodity.Commodity;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class Electrical extends RealConnectionType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8016297442193124095L;

	public Electrical(Commodity commodity) {
		super(commodity);
	}
	
	/**
	 * only for serialisation, do not use normally
	 */
	@Deprecated
	protected Electrical() {
		
	}

}
