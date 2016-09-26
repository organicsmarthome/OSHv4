package osh.datatypes.registry.driver.details.energy;

import java.util.Collection;
import java.util.UUID;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public class ElectricPowerDriverDetails extends StateExchange {
	
	/**
	 * Electrical Power Details for Logging
	 */
	private static final long serialVersionUID = 4799921746955434387L;

	protected UUID meterUuid;

	protected double activePower;
	
	protected double reactivePower;
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public ElectricPowerDriverDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	
	public UUID getMeterUuid() {
		return meterUuid;
	}
	
	public void setMeterUuid(UUID meterUuid) {
		this.meterUuid = meterUuid;
	}
	
	
	public double getActivePower() {
		return activePower;
	}

	public void setActivePower(double activePower) {
		this.activePower = activePower;
	}

	
	public double getReactivePower() {
		return reactivePower;
	}

	public void setReactivePower(double reactivePower) {
		this.reactivePower = reactivePower;
	}
	
	@Override
	public String toString() {
		return "Electric Power: { " +
				"MeterUUID=" + getMeterUuid() + ", " +
				"P=" + getActivePower() + "W " +
				"Q=" + getReactivePower() + "var, " +
				"}" ;
	}
	
	
	static public ElectricPowerDriverDetails aggregatePowerDetails(UUID sender, Collection<ElectricPowerDriverDetails> details) {
		int _pdCount = 0;
		long timestamp = 0;
		double activesum = 0, reactivesum = 0;
		
		for ( ElectricPowerDriverDetails p : details ) {
			activesum = activesum + p.getActivePower();
			reactivesum = reactivesum + p.getReactivePower();
			timestamp = p.getTimestamp(); //why?
			_pdCount++;
		}
		
		ElectricPowerDriverDetails _pd = new ElectricPowerDriverDetails(sender, timestamp);
		_pd.setActivePower( activesum );
		_pd.setReactivePower( reactivesum );

		if ( _pdCount == details.size() && _pdCount > 0 ) {
			return _pd;
		}
		else {
			// ERROR: undefined state due to missing data
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if( obj == null ) {
			return false;
		}
		if( !(obj instanceof ElectricPowerDriverDetails) ) {
			return false;
		}
		
		ElectricPowerDriverDetails other = (ElectricPowerDriverDetails) obj;
		
		return  (this.meterUuid.equals(other.meterUuid)) &&
				(this.activePower == other.activePower) &&
				(this.reactivePower == other.reactivePower);
	}
	
}
