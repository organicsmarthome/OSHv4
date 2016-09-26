package osh.esc.grid.carrier;

import java.io.Serializable;

import osh.datatypes.commodity.Commodity;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class Thermal extends RealConnectionType implements Serializable {

	/** Serial ID */
	private static final long serialVersionUID = -5019503350463452048L;

	public Thermal(Commodity commodity) {
		super(commodity);
	}
	
	/**
	 * only for serialization, do not use
	 */
	@Deprecated
	protected Thermal() {		
	}
	
}
