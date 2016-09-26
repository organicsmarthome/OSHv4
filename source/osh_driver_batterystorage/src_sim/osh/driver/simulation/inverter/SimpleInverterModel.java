package osh.driver.simulation.inverter;

import java.io.Serializable;

/**
 * SimpleInverter <br>
 * 
 * @author Matthias Maerz
 *
 */
public class SimpleInverterModel extends InverterModel implements Serializable {

	private static final long serialVersionUID = 8297923914394199191L;

	public SimpleInverterModel( ) {
		super();
	}
	
	public SimpleInverterModel( 
			int minComplexPower,
			int maxComplexPower,
			int minPower,
			int maxPower) {
		super(	
				minComplexPower,
				maxComplexPower,
				minPower,
				maxPower);
		
	}
	
	@Override
	public double getinverterEfficiency() {
		return 0.85;
	}

}
