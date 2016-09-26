package osh.comdriver.interaction.datatypes;

import java.util.Collection;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="electricityDetails")
public class RestElectricityDetails extends RestStateDetail {
	
	protected UUID meterUuid;

	protected double voltage;
	
	protected double current;
	
	protected double activePower;
	
	protected double reactivePower;
	
	protected double totalEnergyConsumption;
	
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestElectricityDetails() {
		this(null, 0);
	}
	
	public RestElectricityDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	
	public UUID getMeterUuid() {
		return meterUuid;
	}
	
	public void setMeterUuid(UUID meterUuid) {
		this.meterUuid = meterUuid;
	}
	
	public double getVoltage() {
		return voltage;
	}

	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}

	public double getCurrent() {
		return current;
	}

	public void setCurrent(double current) {
		this.current = current;
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
	
	public double getTotalEnergyConsumption() {
		return totalEnergyConsumption;
	}

	public void setTotalEnergyConsumption(double totalEnergyConsumption) {
		this.totalEnergyConsumption = totalEnergyConsumption;
	}

	/**
	 * Calculates a pseudo distance between two measurements
	 * A value >= 1.0 is considered significant ( which is approx. 5 W difference )
	 * 
	 * @param other
	 * @return
	 */
	public double distance(RestElectricityDetails other) {
		if( other == null )
			return Double.MAX_VALUE;
		
		double dist = 0.0;

		dist += Math.abs( this.activePower - other.activePower ) / 10;
		dist += Math.abs( this.voltage - other.voltage ) / 230;
		dist += Math.abs( this.current - other.current ) / 16;
		
		return dist*2.0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(activePower);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(current);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((meterUuid == null) ? 0 : meterUuid.hashCode());
		temp = Double.doubleToLongBits(reactivePower);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(totalEnergyConsumption);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(voltage);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestElectricityDetails other = (RestElectricityDetails) obj;
		if (Double.doubleToLongBits(activePower) != Double
				.doubleToLongBits(other.activePower))
			return false;
		if (Double.doubleToLongBits(current) != Double
				.doubleToLongBits(other.current))
			return false;
		if (meterUuid == null) {
			if (other.meterUuid != null)
				return false;
		} else if (!meterUuid.equals(other.meterUuid))
			return false;
		if (Double.doubleToLongBits(reactivePower) != Double
				.doubleToLongBits(other.reactivePower))
			return false;
		if (Double.doubleToLongBits(totalEnergyConsumption) != Double
				.doubleToLongBits(other.totalEnergyConsumption))
			return false;
		if (Double.doubleToLongBits(voltage) != Double
				.doubleToLongBits(other.voltage))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Power: { " +
				"U=" + getVoltage() + "V, " +
				"I=" + getCurrent() + "A, " +
				"P=" + getActivePower() + "W " +
				"Q=" + getReactivePower() + "var " +
				"E=" + getTotalEnergyConsumption() + "Wh" +
				"}" ;
	}
	
	static public RestElectricityDetails aggregatePowerDetails(Collection<RestElectricityDetails> details) {
		int _pdCount = 0;
		RestElectricityDetails _pd = new RestElectricityDetails(null, 0);
		
		for( RestElectricityDetails p : details ) {
			_pd.setActivePower( _pd.getActivePower() + p.getActivePower() );
			_pd.setCurrent( _pd.getCurrent() + p.getCurrent() );
			_pd.setTotalEnergyConsumption( _pd.getTotalEnergyConsumption() + p.getTotalEnergyConsumption() );
			_pd.setReactivePower( _pd.getReactivePower() + p.getReactivePower() );
			_pd.setVoltage( _pd.getVoltage() + p.getVoltage() );
			
			_pdCount++;
		}
		
		if( _pdCount == details.size() && _pdCount > 0 ) {
			// only voltage is averaged
			_pd.setVoltage( _pd.getVoltage() / _pdCount );

			return _pd;
		}
		else // ERROR: undefined state due to missing data
			return null;
	}

	
}
